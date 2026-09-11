//! Streaming Merkle difference of two authenticated canonical history trees.
//! Equal directories/leaves are skipped before any contained datom is decoded.
//! Unequal leaf streams are merged globally, including across changed physical
//! boundaries; a split/repack alone therefore creates no document mutations.
use crate::persistent_tree::{ChildRef, RootNode, TreeReadStats};
use crate::tree_cursor::{LoadedDirectory, LoadedLeaf};
use crate::{Datom, Digest, ErrorCategory, IndexOrder, SemanticError};
use std::cmp::Ordering as Cmp;
use std::sync::Arc;

fn fault(code: &'static str, message: &'static str) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

pub(crate) struct HistoryDifference<S> {
    before: Side<S>,
    after: Side<S>,
    failed: bool,
    pub(crate) references_examined: u64,
    pub(crate) datoms_examined: u64,
}

struct Side<S> {
    source: S,
    root: Arc<RootNode>,
    directory_index: usize,
    directory: Option<LoadedDirectory>,
    leaf_index: usize,
    leaf: Option<LoadedLeaf>,
    datom_index: usize,
    head: Option<Datom>,
    reads: TreeReadStats,
}

pub(crate) trait HistorySource {
    fn directory(
        &self,
        reference: &ChildRef,
        reads: &mut TreeReadStats,
    ) -> Result<LoadedDirectory, SemanticError>;
    fn leaf(
        &self,
        reference: &ChildRef,
        reads: &mut TreeReadStats,
    ) -> Result<LoadedLeaf, SemanticError>;
}

impl<S: HistorySource> Side<S> {
    fn from_root(source: S, root: Arc<RootNode>) -> Self {
        Self {
            source,
            root,
            directory_index: 0,
            directory: None,
            leaf_index: 0,
            leaf: None,
            datom_index: 0,
            head: None,
            reads: TreeReadStats::default(),
        }
    }

    fn exhausted(&self) -> bool {
        self.directory_index == self.root.directories.len()
    }

    fn directory_hash(&self) -> Option<Digest> {
        self.directory
            .is_none()
            .then(|| {
                self.root
                    .directories
                    .get(self.directory_index)
                    .map(|r| r.hash)
            })
            .flatten()
    }

    fn load_directory(&mut self) -> Result<(), SemanticError> {
        if self.directory.is_none() && !self.exhausted() {
            self.directory = Some(self.source.directory(
                &self.root.directories[self.directory_index],
                &mut self.reads,
            )?);
            self.leaf_index = 0;
        }
        Ok(())
    }

    fn leaf_hash(&self) -> Option<Digest> {
        self.leaf
            .is_none()
            .then(|| {
                self.directory
                    .as_ref()
                    .and_then(|d| d.leaves.get(self.leaf_index))
                    .map(|r| r.hash)
            })
            .flatten()
    }

    fn advance_leaf(&mut self) {
        self.leaf = None;
        self.head = None;
        self.datom_index = 0;
        self.leaf_index += 1;
        if self
            .directory
            .as_ref()
            .is_some_and(|d| self.leaf_index == d.leaves.len())
        {
            self.directory = None;
            self.directory_index += 1;
        }
    }

    fn fill_head(&mut self) -> Result<bool, SemanticError> {
        if self.head.is_some() || self.exhausted() {
            return Ok(false);
        }
        if self.leaf.is_none() {
            let directory = self
                .directory
                .as_ref()
                .expect("directory loaded before leaf");
            self.leaf = Some(
                self.source
                    .leaf(&directory.leaves[self.leaf_index], &mut self.reads)?,
            );
        }
        self.head = self
            .leaf
            .as_ref()
            .and_then(|leaf| leaf.datom(self.datom_index));
        if self.head.is_none() {
            return Err(fault(
                "fulltext/empty-history-leaf",
                "canonical history leaf is empty",
            ));
        }
        Ok(true)
    }

    fn take(&mut self) -> Datom {
        let datom = self.head.take().expect("loaded history head");
        self.datom_index += 1;
        if self
            .leaf
            .as_ref()
            .is_some_and(|leaf| self.datom_index == leaf.len())
        {
            self.advance_leaf();
        }
        datom
    }
}

impl<S: HistorySource> HistoryDifference<S> {
    pub(crate) fn from_roots(
        before: S,
        before_root: Arc<RootNode>,
        after: S,
        after_root: Arc<RootNode>,
        equal: bool,
    ) -> Self {
        let mut result = Self {
            before: Side::from_root(before, before_root),
            after: Side::from_root(after, after_root),
            failed: false,
            references_examined: 0,
            datoms_examined: 0,
        };
        if equal {
            result.before.directory_index = result.before.root.directories.len();
            result.after.directory_index = result.after.root.directories.len();
        }
        result
    }
    fn next_difference(&mut self) -> Result<Option<(Datom, bool)>, SemanticError> {
        loop {
            if self.before.exhausted() && self.after.exhausted() {
                return Ok(None);
            }
            let directory_hashes = (self.before.directory_hash(), self.after.directory_hash());
            self.references_examined +=
                u64::from(directory_hashes.0.is_some()) + u64::from(directory_hashes.1.is_some());
            if let (Some(before), Some(after)) = directory_hashes
                && before == after
            {
                self.before.directory_index += 1;
                self.after.directory_index += 1;
                continue;
            }
            self.before.load_directory()?;
            self.after.load_directory()?;
            let leaf_hashes = (self.before.leaf_hash(), self.after.leaf_hash());
            self.references_examined +=
                u64::from(leaf_hashes.0.is_some()) + u64::from(leaf_hashes.1.is_some());
            if let (Some(before), Some(after)) = leaf_hashes
                && before == after
            {
                self.before.advance_leaf();
                self.after.advance_leaf();
                continue;
            }
            self.datoms_examined += u64::from(self.before.fill_head()?);
            self.datoms_examined += u64::from(self.after.fill_head()?);
            match (&self.before.head, &self.after.head) {
                (Some(before), Some(after)) => match before.cmp_in(after, IndexOrder::Eavt) {
                    Cmp::Less => return Ok(Some((self.before.take(), false))),
                    Cmp::Greater => return Ok(Some((self.after.take(), true))),
                    Cmp::Equal => {
                        self.before.take();
                        self.after.take();
                    }
                },
                (Some(_), None) => return Ok(Some((self.before.take(), false))),
                (None, Some(_)) => return Ok(Some((self.after.take(), true))),
                (None, None) => return Ok(None),
            }
        }
    }
}

impl<S: HistorySource> Iterator for HistoryDifference<S> {
    type Item = Result<(Datom, bool), SemanticError>;
    fn next(&mut self) -> Option<Self::Item> {
        if self.failed {
            return None;
        }
        match self.next_difference() {
            Ok(Some(change)) => Some(Ok(change)),
            Ok(None) => None,
            Err(error) => {
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::persistent_tree::{TreeConfig, TreeNode, TreeNodeSet, build_tree, decode_tree_node};
    use crate::tree_cursor::{validate_loaded_child_datom, validate_loaded_child_key};
    use crate::{Value, make_eid, t_to_tx};

    #[derive(Clone)]
    struct Memory(Arc<TreeNodeSet>);
    impl Memory {
        fn node(
            &self,
            reference: &ChildRef,
            reads: &mut TreeReadStats,
        ) -> Result<Arc<TreeNode>, SemanticError> {
            let bytes = self
                .0
                .get(&reference.hash)
                .ok_or_else(|| fault("fulltext/test-missing-node", "missing fixture node"))?;
            reads.decoded_bytes += bytes.len() as u64;
            Ok(Arc::new(decode_tree_node(&reference.hash, bytes)?))
        }
    }
    impl HistorySource for Memory {
        fn directory(
            &self,
            reference: &ChildRef,
            reads: &mut TreeReadStats,
        ) -> Result<LoadedDirectory, SemanticError> {
            let node = self.node(reference, reads)?;
            let TreeNode::Directory(directory) = node.as_ref() else {
                panic!("fixture directory")
            };
            validate_loaded_child_key(
                reference,
                directory.order,
                directory.history,
                directory.count,
                directory.leaves.first().map(|r| &r.key),
                IndexOrder::Eavt,
                true,
            )?;
            reads.directory_reads += 1;
            Ok(LoadedDirectory(node))
        }
        fn leaf(
            &self,
            reference: &ChildRef,
            reads: &mut TreeReadStats,
        ) -> Result<LoadedLeaf, SemanticError> {
            let node = self.node(reference, reads)?;
            let TreeNode::Leaf(leaf) = node.as_ref() else {
                panic!("fixture leaf")
            };
            validate_loaded_child_datom(
                reference,
                leaf.order,
                leaf.history,
                leaf.len() as u64,
                leaf.datom(0).as_ref(),
                IndexOrder::Eavt,
                true,
            )?;
            reads.leaf_reads += 1;
            Ok(LoadedLeaf(node))
        }
    }
    fn fact(n: u64) -> Datom {
        Datom {
            entity: make_eid(crate::USER_PARTITION, 1000 + n).unwrap(),
            attribute: 1000,
            value: Value::String(format!("document {n}")),
            tx: t_to_tx(1).unwrap(),
            added: true,
        }
    }
    fn side(mut datoms: Vec<Datom>, leaves: usize, directories: usize) -> Side<Memory> {
        datoms.sort_by(|a, b| a.cmp_in(b, IndexOrder::Eavt));
        let tree = build_tree(
            IndexOrder::Eavt,
            true,
            datoms,
            &TreeConfig {
                max_leaf_datoms: leaves,
                max_leaves_per_directory: directories,
                ..TreeConfig::default()
            },
        )
        .unwrap();
        let TreeNode::Root(root) = decode_tree_node(
            &tree.descriptor.root_hash,
            tree.nodes.get(&tree.descriptor.root_hash).unwrap(),
        )
        .unwrap() else {
            panic!("fixture root")
        };
        Side::from_root(Memory(Arc::new(tree.nodes)), Arc::new(root))
    }
    fn difference(before: Side<Memory>, after: Side<Memory>) -> HistoryDifference<Memory> {
        HistoryDifference {
            before,
            after,
            failed: false,
            references_examined: 0,
            datoms_examined: 0,
        }
    }

    #[test]
    fn physical_splits_and_repacking_do_not_change_logical_history() {
        let history = (0..79).map(fact).collect::<Vec<_>>();
        for old_leaf in [2, 7, 19] {
            for new_leaf in [3, 11, 23] {
                let mut cursor = difference(
                    side(history.clone(), old_leaf, 3),
                    side(history.clone(), new_leaf, 5),
                );
                assert_eq!(
                    cursor.by_ref().collect::<Result<Vec<_>, _>>().unwrap(),
                    vec![]
                );
                assert!(cursor.datoms_examined <= 2 * history.len() as u64);
                assert!(cursor.next().is_none());
            }
        }
    }

    #[test]
    fn changed_leaf_diff_matches_independent_add_remove_oracle_across_layouts() {
        let before = (0..57).map(fact).collect::<Vec<_>>();
        let mut after = before.clone();
        // Physical noHistory removal, new assertions, and explicit retractions
        // are all compared as exact facts; the record layer selects assertions.
        after.retain(|d| ![fact(4), fact(29), fact(56)].contains(d));
        after.push(fact(90));
        let mut retraction = fact(8);
        retraction.tx = t_to_tx(2).unwrap();
        retraction.added = false;
        after.push(retraction);
        let mut expected = before
            .iter()
            .filter(|d| !after.contains(d))
            .cloned()
            .map(|d| (d, false))
            .chain(
                after
                    .iter()
                    .filter(|d| !before.contains(d))
                    .cloned()
                    .map(|d| (d, true)),
            )
            .collect::<Vec<_>>();
        expected.sort_by(|a, b| a.0.cmp_in(&b.0, IndexOrder::Eavt));
        for (old_leaf, new_leaf) in [(2, 3), (8, 8), (13, 4)] {
            let actual = difference(
                side(before.clone(), old_leaf, 4),
                side(after.clone(), new_leaf, 7),
            )
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
            assert_eq!(actual, expected);
        }
        for (before, after) in [
            (vec![], before.clone()),
            (before.clone(), vec![]),
            (vec![], vec![]),
        ] {
            let expected = before.len() + after.len();
            assert_eq!(
                difference(side(before, 3, 4), side(after, 7, 2))
                    .collect::<Result<Vec<_>, _>>()
                    .unwrap()
                    .len(),
                expected
            );
        }
    }

    #[test]
    fn fixed_change_skips_unchanged_tree_content_as_history_grows() {
        for size in [128, 512, 2048] {
            let before = (0..size).map(fact).collect::<Vec<_>>();
            let mut after = before.clone();
            after[size as usize / 2].value = Value::String("replacement text".into());
            let mut cursor = difference(side(before, 8, 4), side(after, 8, 4));
            assert_eq!(
                cursor
                    .by_ref()
                    .collect::<Result<Vec<_>, _>>()
                    .unwrap()
                    .len(),
                2
            );
            assert!(cursor.datoms_examined <= 16);
            assert_eq!(
                cursor.before.reads.leaf_reads + cursor.after.reads.leaf_reads,
                2
            );
            eprintln!(
                "fulltext_history_delta size={size} datoms={} references={} directories={} leaves={}",
                cursor.datoms_examined,
                cursor.references_examined,
                cursor.before.reads.directory_reads + cursor.after.reads.directory_reads,
                cursor.before.reads.leaf_reads + cursor.after.reads.leaf_reads
            );
        }
    }

    #[test]
    fn source_error_fuses_instead_of_guessing_an_empty_difference() {
        let before = (0..16).map(fact).collect::<Vec<_>>();
        let mut after = before.clone();
        after[0].value = Value::String("changed".into());
        let mut cursor = difference(side(before, 4, 4), side(after, 4, 4));
        cursor.before.source = Memory(Arc::new(TreeNodeSet::default()));
        assert_eq!(
            cursor.next().unwrap().unwrap_err().code,
            "fulltext/test-missing-node"
        );
        assert!(cursor.next().is_none());
        assert_eq!(cursor.datoms_examined, 0);
    }
}
