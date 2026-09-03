//! Private persistent memory-index tree used by `recent`.
//!
//! The shape deliberately follows Datomic's recovered `datomic.btset`: small
//! immutable leaves and branches, a fanout of sixteen, path-copy insertion,
//! and a lower-bound cursor.  It is specialized to recent datom references;
//! this is not a storage abstraction.

use crate::{Datom, DurableTransaction, IndexOrder};
use std::cmp::Ordering;
use std::sync::Arc;

#[cfg(test)]
use std::collections::BTreeSet;

const NODE_WIDTH: usize = 16;

#[derive(Clone, Debug)]
pub(crate) struct RecentDatomRef {
    transaction: Arc<DurableTransaction>,
    datom: u32,
}

impl RecentDatomRef {
    pub(crate) fn new(transaction: Arc<DurableTransaction>, datom: usize) -> Result<Self, ()> {
        Ok(Self {
            transaction,
            datom: u32::try_from(datom).map_err(|_| ())?,
        })
    }

    pub(crate) fn datom(&self) -> &Datom {
        &self.transaction.tx_data[self.datom as usize]
    }
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct BtWork {
    pub(crate) comparisons: u64,
    pub(crate) node_visits: u64,
    pub(crate) nodes_copied: u64,
    pub(crate) node_splits: u64,
}

impl BtWork {
    fn compared(&mut self, ordering: Ordering) -> Ordering {
        self.comparisons = self.comparisons.saturating_add(1);
        ordering
    }
}

#[derive(Debug)]
enum Node {
    Leaf {
        items: Vec<RecentDatomRef>,
    },
    Branch {
        first: RecentDatomRef,
        separators: Vec<RecentDatomRef>,
        children: Vec<Arc<Node>>,
    },
}

impl Node {
    fn first(&self) -> &RecentDatomRef {
        match self {
            Self::Leaf { items } => &items[0],
            Self::Branch { first, .. } => first,
        }
    }
}

#[derive(Clone, Debug)]
pub(crate) struct RecentBtSet {
    order: IndexOrder,
    len: u64,
    root: Option<Arc<Node>>,
}

enum Inserted {
    Duplicate,
    One(Arc<Node>),
    Split(Arc<Node>, Arc<Node>),
}

impl RecentBtSet {
    pub(crate) fn empty(order: IndexOrder) -> Self {
        Self {
            order,
            len: 0,
            root: None,
        }
    }

    pub(crate) fn len(&self) -> u64 {
        self.len
    }

    pub(crate) fn height(&self) -> u32 {
        fn height(node: &Node) -> u32 {
            match node {
                Node::Leaf { .. } => 1,
                Node::Branch { children, .. } => 1 + height(&children[0]),
            }
        }
        self.root.as_deref().map_or(0, height)
    }

    pub(crate) fn node_count(&self) -> u64 {
        fn count(node: &Node) -> u64 {
            match node {
                Node::Leaf { .. } => 1,
                Node::Branch { children, .. } => {
                    1 + children.iter().map(|child| count(child)).sum::<u64>()
                }
            }
        }
        self.root.as_deref().map_or(0, count)
    }

    pub(crate) fn insert(&self, item: RecentDatomRef, work: &mut BtWork) -> (Self, bool) {
        let Some(root) = &self.root else {
            work.nodes_copied = work.nodes_copied.saturating_add(1);
            return (
                Self {
                    order: self.order,
                    len: 1,
                    root: Some(Arc::new(Node::Leaf { items: vec![item] })),
                },
                true,
            );
        };

        match insert_node(root, item, self.order, work) {
            Inserted::Duplicate => (self.clone(), false),
            Inserted::One(root) => (
                Self {
                    order: self.order,
                    len: self.len + 1,
                    root: Some(root),
                },
                true,
            ),
            Inserted::Split(left, right) => {
                work.nodes_copied = work.nodes_copied.saturating_add(1);
                let root = branch(vec![left, right]);
                (
                    Self {
                        order: self.order,
                        len: self.len + 1,
                        root: Some(root),
                    },
                    true,
                )
            }
        }
    }

    pub(crate) fn cursor(&self) -> BtCursor {
        BtCursor::first(self.root.clone())
    }

    pub(crate) fn seek(&self, key: &Datom) -> BtCursor {
        BtCursor::seek(self.root.clone(), |candidate| {
            candidate.cmp_in(key, self.order)
        })
    }

    /// Lower-bound seek using a monotonic comparison against a virtual key.
    /// This supports primary-key and AEVT-attribute seeks without manufacturing
    /// sentinel `Value`s that are not part of the data model.
    pub(crate) fn seek_by(&self, compare: impl Fn(&Datom) -> Ordering) -> BtCursor {
        BtCursor::seek(self.root.clone(), compare)
    }

    #[cfg(test)]
    pub(crate) fn node_ids(&self) -> BTreeSet<usize> {
        fn collect(node: &Arc<Node>, result: &mut BTreeSet<usize>) {
            let id = Arc::as_ptr(node) as usize;
            if !result.insert(id) {
                return;
            }
            if let Node::Branch { children, .. } = node.as_ref() {
                for child in children {
                    collect(child, result);
                }
            }
        }
        let mut result = BTreeSet::new();
        if let Some(root) = &self.root {
            collect(root, &mut result);
        }
        result
    }
}

fn compare(
    left: &RecentDatomRef,
    right: &RecentDatomRef,
    order: IndexOrder,
    work: &mut BtWork,
) -> Ordering {
    work.compared(left.datom().cmp_in(right.datom(), order))
}

fn branch(children: Vec<Arc<Node>>) -> Arc<Node> {
    debug_assert!(children.len() >= 2 && children.len() <= NODE_WIDTH);
    let first = children[0].first().clone();
    let separators = children[1..]
        .iter()
        .map(|child| child.first().clone())
        .collect();
    Arc::new(Node::Branch {
        first,
        separators,
        children,
    })
}

fn insert_node(
    node: &Arc<Node>,
    item: RecentDatomRef,
    order: IndexOrder,
    work: &mut BtWork,
) -> Inserted {
    work.node_visits = work.node_visits.saturating_add(1);
    match node.as_ref() {
        Node::Leaf { items } => {
            let position =
                items.binary_search_by(|candidate| compare(candidate, &item, order, work));
            let Err(position) = position else {
                return Inserted::Duplicate;
            };
            let mut next = Vec::with_capacity(items.len() + 1);
            next.extend_from_slice(&items[..position]);
            next.push(item);
            next.extend_from_slice(&items[position..]);
            if next.len() <= NODE_WIDTH {
                work.nodes_copied = work.nodes_copied.saturating_add(1);
                Inserted::One(Arc::new(Node::Leaf { items: next }))
            } else {
                let right_items = next.split_off(next.len() / 2);
                work.nodes_copied = work.nodes_copied.saturating_add(2);
                work.node_splits = work.node_splits.saturating_add(1);
                Inserted::Split(
                    Arc::new(Node::Leaf { items: next }),
                    Arc::new(Node::Leaf { items: right_items }),
                )
            }
        }
        Node::Branch {
            separators,
            children,
            ..
        } => {
            let child_index = separators.partition_point(|separator| {
                compare(separator, &item, order, work) != Ordering::Greater
            });
            match insert_node(&children[child_index], item, order, work) {
                Inserted::Duplicate => Inserted::Duplicate,
                Inserted::One(child) => {
                    let mut next = children.clone();
                    next[child_index] = child;
                    work.nodes_copied = work.nodes_copied.saturating_add(1);
                    Inserted::One(branch(next))
                }
                Inserted::Split(left, right) => {
                    let mut next = Vec::with_capacity(children.len() + 1);
                    next.extend_from_slice(&children[..child_index]);
                    next.push(left);
                    next.push(right);
                    next.extend_from_slice(&children[child_index + 1..]);
                    if next.len() <= NODE_WIDTH {
                        work.nodes_copied = work.nodes_copied.saturating_add(1);
                        Inserted::One(branch(next))
                    } else {
                        let right_children = next.split_off(next.len() / 2);
                        work.nodes_copied = work.nodes_copied.saturating_add(2);
                        work.node_splits = work.node_splits.saturating_add(1);
                        Inserted::Split(branch(next), branch(right_children))
                    }
                }
            }
        }
    }
}

#[derive(Clone, Debug)]
struct PathFrame {
    branch: Arc<Node>,
    child: usize,
}

#[derive(Clone, Debug)]
pub(crate) struct BtCursor {
    path: Vec<PathFrame>,
    leaf: Option<Arc<Node>>,
    position: usize,
    work: BtWork,
}

impl BtCursor {
    fn first(root: Option<Arc<Node>>) -> Self {
        let mut cursor = Self {
            path: Vec::new(),
            leaf: None,
            position: 0,
            work: BtWork::default(),
        };
        if let Some(root) = root {
            cursor.descend_left(root);
        }
        cursor
    }

    fn seek(root: Option<Arc<Node>>, compare: impl Fn(&Datom) -> Ordering) -> Self {
        let mut cursor = Self {
            path: Vec::new(),
            leaf: None,
            position: 0,
            work: BtWork::default(),
        };
        let Some(mut node) = root else {
            return cursor;
        };
        loop {
            cursor.work.node_visits = cursor.work.node_visits.saturating_add(1);
            match node.as_ref() {
                Node::Leaf { items } => {
                    let position = items.partition_point(|item| {
                        cursor.work.comparisons = cursor.work.comparisons.saturating_add(1);
                        compare(item.datom()) == Ordering::Less
                    });
                    cursor.leaf = Some(Arc::clone(&node));
                    cursor.position = position;
                    if position == items.len() {
                        cursor.advance_leaf();
                    }
                    return cursor;
                }
                Node::Branch {
                    separators,
                    children,
                    ..
                } => {
                    let child = separators.partition_point(|separator| {
                        cursor.work.comparisons = cursor.work.comparisons.saturating_add(1);
                        compare(separator.datom()) == Ordering::Less
                    });
                    cursor.path.push(PathFrame {
                        branch: Arc::clone(&node),
                        child,
                    });
                    node = Arc::clone(&children[child]);
                }
            }
        }
    }

    fn descend_left(&mut self, mut node: Arc<Node>) {
        loop {
            self.work.node_visits = self.work.node_visits.saturating_add(1);
            match node.as_ref() {
                Node::Leaf { .. } => {
                    self.leaf = Some(node);
                    self.position = 0;
                    return;
                }
                Node::Branch { children, .. } => {
                    self.path.push(PathFrame {
                        branch: Arc::clone(&node),
                        child: 0,
                    });
                    node = Arc::clone(&children[0]);
                }
            }
        }
    }

    fn advance_leaf(&mut self) {
        self.leaf = None;
        while let Some(frame) = self.path.last_mut() {
            let Node::Branch { children, .. } = frame.branch.as_ref() else {
                unreachable!("cursor path contains only branches")
            };
            if frame.child + 1 < children.len() {
                frame.child += 1;
                let child = Arc::clone(&children[frame.child]);
                self.descend_left(child);
                return;
            }
            self.path.pop();
        }
    }

    pub(crate) fn next(&mut self) -> Option<RecentDatomRef> {
        loop {
            let leaf = self.leaf.as_ref()?;
            let Node::Leaf { items } = leaf.as_ref() else {
                unreachable!("cursor leaf points to a leaf")
            };
            if let Some(item) = items.get(self.position) {
                self.position += 1;
                return Some(item.clone());
            }
            self.advance_leaf();
        }
    }

    pub(crate) fn work(&self) -> BtWork {
        self.work
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{Value, t_to_tx};
    use std::collections::BTreeMap;

    fn item(value: i64) -> RecentDatomRef {
        RecentDatomRef::new(
            Arc::new(DurableTransaction {
                database_id: "btset".into(),
                basis_t: 1,
                previous_hash: [0; 32],
                eidx_frontier: 1,
                tempids: BTreeMap::new(),
                tx_data: vec![Datom {
                    entity: value as u64 + 1,
                    attribute: 1,
                    value: Value::Long(value),
                    tx: t_to_tx(1).unwrap(),
                    added: true,
                }],
            }),
            0,
        )
        .unwrap()
    }

    #[test]
    fn persistent_insert_seek_and_snapshot_sharing() {
        let mut tree = RecentBtSet::empty(IndexOrder::Eavt);
        let mut before = None;
        for value in (0..257).rev() {
            let mut work = BtWork::default();
            let (next, inserted) = tree.insert(item(value), &mut work);
            assert!(inserted);
            assert!(work.node_visits <= u64::from(tree.height()) + 1);
            tree = next;
            if value == 32 {
                before = Some(tree.clone());
            }
        }
        assert_eq!(tree.len(), 257);
        let values = {
            let mut cursor = tree.cursor();
            let mut values = Vec::new();
            while let Some(item) = cursor.next() {
                values.push(item.datom().entity);
            }
            values
        };
        assert!(values.windows(2).all(|pair| pair[0] < pair[1]));

        let key = item(128).datom().clone();
        let mut cursor = tree.seek(&key);
        assert_eq!(cursor.next().unwrap().datom().value, Value::Long(128));
        assert!(cursor.work().node_visits <= u64::from(tree.height()) + 1);

        let before = before.unwrap();
        assert!(
            before
                .node_ids()
                .intersection(&tree.node_ids())
                .next()
                .is_some()
        );
    }

    #[test]
    fn insertion_orders_match_a_sorted_set_oracle_and_preserve_predecessors() {
        let width = 257_u64;
        let orders = [
            (0..width).collect::<Vec<_>>(),
            (0..width).rev().collect::<Vec<_>>(),
            (0..width).map(|value| (value * 73) % width).collect(),
        ];
        for insertion_order in orders {
            let mut tree = RecentBtSet::empty(IndexOrder::Eavt);
            let mut oracle = BTreeSet::new();
            let mut predecessor = None;
            let mut predecessor_oracle = Vec::new();
            for (offset, value) in insertion_order.into_iter().enumerate() {
                let mut work = BtWork::default();
                let (next, inserted) = tree.insert(item(value as i64), &mut work);
                assert_eq!(inserted, oracle.insert(value));
                assert!(work.node_visits <= u64::from(tree.height()) + 1);
                tree = next;
                if offset == 127 {
                    predecessor = Some(tree.clone());
                    predecessor_oracle = oracle.iter().copied().collect();
                }
            }
            let collect = |tree: &RecentBtSet| {
                let mut cursor = tree.cursor();
                let mut result = Vec::new();
                while let Some(reference) = cursor.next() {
                    result.push(reference.datom().entity - 1);
                }
                result
            };
            assert_eq!(collect(&tree), oracle.iter().copied().collect::<Vec<_>>());
            assert_eq!(collect(&predecessor.unwrap()), predecessor_oracle);

            let mut duplicate_work = BtWork::default();
            let (same, inserted) = tree.insert(item(128), &mut duplicate_work);
            assert!(!inserted);
            assert_eq!(same.len(), tree.len());
            assert_eq!(collect(&same), collect(&tree));
        }
    }

    #[test]
    fn one_insert_replaces_only_a_root_to_leaf_path() {
        let mut tree = RecentBtSet::empty(IndexOrder::Eavt);
        for value in 0..4096 {
            tree = tree.insert(item(value), &mut BtWork::default()).0;
        }
        let before_ids = tree.node_ids();
        let before_height = tree.height();
        let mut work = BtWork::default();
        let (successor, inserted) = tree.insert(item(4096), &mut work);
        assert!(inserted);
        let after_ids = successor.node_ids();
        let new_nodes = after_ids.difference(&before_ids).count() as u64;
        let shared_nodes = after_ids.intersection(&before_ids).count() as u64;

        assert!(work.node_visits <= u64::from(before_height) + 1);
        assert!(new_nodes <= 2 * u64::from(successor.height()) + 2);
        assert!(shared_nodes * 4 > tree.node_count() * 3);
        assert_eq!(tree.len(), 4096);
        assert_eq!(successor.len(), 4097);
    }
}
