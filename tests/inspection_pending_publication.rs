use atomic_core::persistent_tree::{TreeConfig, build_tree};
use atomic_core::{
    Attribute, Cardinality, Database, Digest, IndexOrder, IntegrityReport, Keyword, ManifestTree,
    PersistentTreeManifest, PostgresMigrator, PostgresOperator, PostgresStore, PostgresTreeStore,
    Schema, TreeManifestRecord, TreePublicationDelta, TreeRootBinding, ValueType, View, sha256,
};
use postgres::{Client, NoTls};
use std::collections::{BTreeMap, BTreeSet};
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

struct Fixture {
    id: String,
    database: Database,
    raw: Client,
    trees: PostgresTreeStore,
    operator: PostgresOperator,
}

impl Fixture {
    fn new() -> Option<Self> {
        let url = std::env::var("ATOMIC_POSTGRES_URL").ok()?;
        PostgresMigrator::connect(&url).unwrap().migrate().unwrap();
        let id = format!(
            "inspection_pending_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1000,
                Keyword::new("item", "value"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        let database = PostgresStore::connect(&url)
            .unwrap()
            .create_database(&id, schema)
            .unwrap();
        Some(Self {
            id,
            database,
            raw: Client::connect(&url, NoTls).unwrap(),
            trees: PostgresTreeStore::connect(&url).unwrap(),
            operator: PostgresOperator::connect(&url).unwrap(),
        })
    }

    fn publish(
        &mut self,
        leaf_datoms: usize,
        prior: Option<(Digest, &BTreeSet<Digest>)>,
        incremental: bool,
    ) -> (Digest, BTreeSet<Digest>) {
        let head = self.raw.query_one(
            "SELECT h.basis_t,h.tx_hash,h.log_generation,t.state_hash, \
                    COALESCE((SELECT max(publication_revision) FROM atomic_tree_publications WHERE database_id=$1),0) \
               FROM atomic_heads h JOIN atomic_generation_transactions t \
                 ON t.database_id=h.database_id AND t.generation=h.log_generation AND t.basis_t=h.basis_t \
              WHERE h.database_id=$1", &[&self.id]).unwrap();
        let basis_t = head.get::<_, i64>(0) as u64;
        let tx_hash: Digest = head.get::<_, Vec<u8>>(1).try_into().unwrap();
        let generation = head.get::<_, i64>(2) as u64;
        let state_hash: Digest = head.get::<_, Vec<u8>>(3).try_into().unwrap();
        let revision = head.get::<_, i64>(4) as u64 + 1;
        let config = TreeConfig {
            max_leaf_datoms: leaf_datoms,
            ..TreeConfig::default()
        };
        let mut trees = Vec::new();
        let mut nodes = BTreeMap::new();
        for history in [false, true] {
            for order in [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ] {
                let datoms = self.database.datoms(
                    if history {
                        View::History
                    } else {
                        View::Current
                    },
                    order,
                );
                let build = build_tree(order, history, datoms, &config).unwrap();
                trees.push(ManifestTree {
                    descriptor: build.descriptor,
                    root_bytes: build.stats.root_bytes,
                });
                nodes.extend(build.nodes.into_nodes());
            }
        }
        let manifest = PersistentTreeManifest {
            database_id: self.id.clone(),
            publication_revision: revision,
            index_basis_t: basis_t,
            basis_t,
            tx_hash,
            state_hash,
            excision_generation: generation,
            eidx_frontier: self.database.eidx_frontier(),
            trees,
            pending_avet: Vec::new(),
        };
        let payload = manifest.encode().unwrap();
        let hash = sha256(&payload);
        let all = nodes.keys().copied().collect::<BTreeSet<_>>();
        let delta = if incremental {
            let (predecessor, previous) = prior.unwrap();
            TreePublicationDelta::Incremental {
                predecessor_manifest_hash: predecessor,
                added_nodes: all.difference(previous).copied().collect(),
                retired_nodes: previous.difference(&all).copied().collect(),
            }
        } else {
            TreePublicationDelta::Replace {
                live_nodes: all.clone(),
            }
        };
        let additions = match &delta {
            TreePublicationDelta::Replace { live_nodes } => live_nodes,
            TreePublicationDelta::Incremental { added_nodes, .. } => added_nodes,
            TreePublicationDelta::Unknown => unreachable!(),
        };
        self.trees
            .begin_build_intent(&self.id, generation, revision - 1, hash, additions)
            .unwrap();
        for (hash, payload) in nodes {
            self.trees.insert_node(hash, &payload).unwrap();
        }
        let record = TreeManifestRecord {
            database_id: self.id.clone(),
            publication_revision: revision,
            basis_t,
            index_basis_t: basis_t,
            tx_hash,
            state_hash,
            excision_generation: generation,
            eidx_frontier: self.database.eidx_frontier(),
            manifest_hash: hash,
            payload,
            roots: manifest
                .trees
                .iter()
                .map(|tree| TreeRootBinding {
                    order: tree.descriptor.order,
                    history: tree.descriptor.history,
                    root_hash: tree.descriptor.root_hash,
                    datom_count: tree.descriptor.count,
                    encoded_bytes: tree.root_bytes,
                })
                .collect(),
        };
        self.trees
            .stage_manifest_with_delta(&record, revision - 1, &delta)
            .unwrap();
        // Exercise the actual SQL publication guards/atomic state transition,
        // deliberately pausing before the caller's opportunistic fold.
        self.raw
            .query_one(
                "SELECT atomic_publish_tree($1,$2,$3,$4,$5)",
                &[
                    &self.id,
                    &(revision as i64),
                    &(basis_t as i64),
                    &&tx_hash[..],
                    &&hash[..],
                ],
            )
            .unwrap();
        self.trees.release_build_intent().unwrap();
        (hash, all)
    }

    fn inspect(&mut self, pending: bool, deep: bool) -> IntegrityReport {
        let report = self.operator.inspect_database(&self.id, deep).unwrap();
        assert!(report.healthy(), "{:?}", report.problems);
        assert_eq!(report.metrics.pending_tree_publications, u64::from(pending));
        if !pending {
            assert_eq!(report.metrics.pending_tree_membership_nodes, 0);
        }
        report
    }

    fn fold(&mut self, hash: Digest, limit: usize) -> bool {
        self.raw
            .query_one(
                "SELECT atomic_apply_tree_publication_work($1,$2)",
                &[&&hash[..], &(limit as i64)],
            )
            .unwrap()
            .get(0)
    }

    fn mismatch(&mut self) {
        let report = self.operator.inspect_database(&self.id, false).unwrap();
        assert!(
            report
                .problems
                .iter()
                .any(|problem| problem.code == "integrity/tree-live-membership-mismatch"),
            "{:?}",
            report.problems
        );
        assert_eq!(report.metrics.pending_tree_publications, 0);
    }

    fn change(&mut self, sql: &str, params: &[&(dyn postgres::types::ToSql + Sync)]) {
        common::with_replica_triggers_disabled(&mut self.raw, |raw| raw.execute(sql, params))
            .unwrap();
    }
}

#[test]
fn initial_replacement_and_incremental_folds_are_authenticated_maintenance() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let (first, first_nodes) = fixture.publish(16, None, false);
    assert_eq!(
        fixture
            .inspect(true, true)
            .metrics
            .pending_tree_membership_nodes,
        first_nodes.len() as u64
    );
    assert!(!fixture.fold(first, 1));
    assert_eq!(
        fixture
            .inspect(true, false)
            .metrics
            .pending_tree_membership_nodes,
        first_nodes.len() as u64 - 1
    );
    assert!(fixture.fold(first, 4096));
    fixture.inspect(false, true);

    let (second, second_nodes) = fixture.publish(7, Some((first, &first_nodes)), false);
    let removals = first_nodes.difference(&second_nodes).count();
    assert!(removals > 1);
    fixture.inspect(true, true);
    assert!(!fixture.fold(second, second_nodes.len()));
    assert_eq!(
        fixture
            .inspect(true, false)
            .metrics
            .pending_tree_membership_nodes,
        removals as u64
    );
    assert!(!fixture.fold(second, 1));
    fixture.inspect(true, false);
    assert!(fixture.fold(second, 4096));
    fixture.inspect(false, true);

    let (third, third_nodes) = fixture.publish(11, Some((second, &second_nodes)), true);
    let additions = third_nodes.difference(&second_nodes).count();
    let removals = second_nodes.difference(&third_nodes).count();
    assert!(additions > 1 && removals > 1);
    fixture.inspect(true, true);
    assert!(!fixture.fold(third, additions + 1));
    assert_eq!(
        fixture
            .inspect(true, false)
            .metrics
            .pending_tree_membership_nodes,
        removals as u64 - 1
    );
    assert!(fixture.fold(third, 4096));
    fixture.inspect(false, true);

    // A zero-edit publication still needs its atomic metadata seal.
    let (fourth, _) = fixture.publish(11, Some((third, &third_nodes)), true);
    assert_eq!(
        fixture
            .inspect(true, false)
            .metrics
            .pending_tree_membership_nodes,
        0
    );
    assert!(fixture.fold(fourth, 4096));
    fixture.inspect(false, true);
}

#[test]
fn pending_headers_do_not_hide_missing_or_corrupt_fold_witnesses() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let (hash, nodes) = fixture.publish(16, None, false);
    fixture.inspect(true, false);
    // The inspector proves safe resumability, not a forensic batch schedule:
    // a protected non-prefix consumed subset has exactly the same endpoint.
    let last = *nodes.last().unwrap();
    fixture.change(
        "INSERT INTO atomic_tree_live_nodes(database_id,node_hash) VALUES($1,$2)",
        &[&fixture.id.clone(), &&last[..]],
    );
    fixture.change(
        "DELETE FROM atomic_tree_delta_nodes WHERE manifest_hash=$1 AND node_hash=$2",
        &[&&hash[..], &&last[..]],
    );
    fixture.inspect(true, false);
    fixture.change(
        "INSERT INTO atomic_tree_delta_nodes(manifest_hash,node_hash,direction) VALUES($1,$2,1)",
        &[&&hash[..], &&last[..]],
    );
    fixture.change(
        "DELETE FROM atomic_tree_live_nodes WHERE database_id=$1 AND node_hash=$2",
        &[&fixture.id.clone(), &&last[..]],
    );
    let node = *nodes.first().unwrap();
    fixture.change(
        "DELETE FROM atomic_tree_delta_nodes WHERE manifest_hash=$1 AND node_hash=$2",
        &[&&hash[..], &&node[..]],
    );
    fixture.mismatch();
    fixture.change(
        "INSERT INTO atomic_tree_delta_nodes(manifest_hash,node_hash,direction) VALUES($1,$2,1)",
        &[&&hash[..], &&node[..]],
    );
    fixture.inspect(true, false);

    fixture.change(
        "DELETE FROM atomic_tree_build_intent_nodes WHERE manifest_hash=$1 AND node_hash=$2",
        &[&&hash[..], &&node[..]],
    );
    fixture.mismatch();
    fixture.change(
        "INSERT INTO atomic_tree_build_intent_nodes(manifest_hash,node_hash) VALUES($1,$2)",
        &[&&hash[..], &&node[..]],
    );
    fixture.inspect(true, false);

    let original: Vec<u8> = fixture
        .raw
        .query_one(
            "SELECT delta_set_hash FROM atomic_tree_delta_headers WHERE manifest_hash=$1",
            &[&&hash[..]],
        )
        .unwrap()
        .get(0);
    fixture.change(
        "UPDATE atomic_tree_delta_headers SET delta_set_hash=$2 WHERE manifest_hash=$1",
        &[&&hash[..], &&[0_u8; 32][..]],
    );
    fixture.mismatch();
    fixture.change(
        "UPDATE atomic_tree_delta_headers SET delta_set_hash=$2 WHERE manifest_hash=$1",
        &[&&hash[..], &original],
    );
    fixture.change(
        "UPDATE atomic_tree_delta_headers SET delta_state=1 WHERE manifest_hash=$1",
        &[&&hash[..]],
    );
    fixture.mismatch();
    fixture.change(
        "UPDATE atomic_tree_delta_headers SET delta_state=2 WHERE manifest_hash=$1",
        &[&&hash[..]],
    );
    fixture.change(
        "DELETE FROM atomic_tree_publication_states WHERE manifest_hash=$1",
        &[&&hash[..]],
    );
    fixture.mismatch();
    fixture.change("INSERT INTO atomic_tree_publication_states(manifest_hash,predecessor_manifest_hash,delta_mode) VALUES($1,NULL,1)", &[&&hash[..]]);
    fixture.inspect(true, false);

    assert!(!fixture.fold(hash, 1));
    fixture.change(
        "DELETE FROM atomic_tree_live_nodes WHERE database_id=$1 AND node_hash=$2",
        &[&fixture.id.clone(), &&node[..]],
    );
    fixture.mismatch();
    fixture.change(
        "INSERT INTO atomic_tree_live_nodes(database_id,node_hash) VALUES($1,$2)",
        &[&fixture.id.clone(), &&node[..]],
    );
    fixture.inspect(true, false);
    assert!(fixture.fold(hash, 4096));
    fixture.inspect(false, true);
}

#[test]
fn incremental_pending_work_rejects_stale_identity_and_missing_retirement_protection() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let (first, first_nodes) = fixture.publish(16, None, false);
    assert!(fixture.fold(first, 4096));
    let (second, second_nodes) = fixture.publish(7, Some((first, &first_nodes)), true);
    fixture.inspect(true, false);
    fixture.change(
        "UPDATE atomic_tree_delta_headers SET predecessor_manifest_hash=$2 WHERE manifest_hash=$1",
        &[&&second[..], &&second[..]],
    );
    fixture.mismatch();
    fixture.change(
        "UPDATE atomic_tree_delta_headers SET predecessor_manifest_hash=$2 WHERE manifest_hash=$1",
        &[&&second[..], &&first[..]],
    );
    fixture.change("UPDATE atomic_tree_build_intents SET log_generation=log_generation+1 WHERE manifest_hash=$1", &[&&second[..]]);
    fixture.mismatch();
    fixture.change("UPDATE atomic_tree_build_intents SET log_generation=log_generation-1 WHERE manifest_hash=$1", &[&&second[..]]);
    fixture.change(
        "UPDATE atomic_tree_live_sets SET manifest_hash=$2 WHERE database_id=$1",
        &[&fixture.id.clone(), &&second[..]],
    );
    fixture.mismatch();
    fixture.change(
        "UPDATE atomic_tree_live_sets SET manifest_hash=$2 WHERE database_id=$1",
        &[&fixture.id.clone(), &&first[..]],
    );
    fixture.inspect(true, false);
    let additions = second_nodes.difference(&first_nodes).count();
    assert!(!fixture.fold(second, additions + 1));
    fixture.inspect(true, false);
    let removed = first_nodes
        .difference(&second_nodes)
        .copied()
        .collect::<Vec<_>>();
    assert!(removed.len() > 1);
    let consumed = removed[0];
    let remaining = removed[1];
    fixture.change("DELETE FROM atomic_tree_retired_nodes WHERE database_id=$1 AND publication_revision=1 AND node_hash=$2", &[&fixture.id.clone(), &&consumed[..]]);
    fixture.mismatch();
    fixture.change("INSERT INTO atomic_tree_retired_nodes(database_id,publication_revision,node_hash) VALUES($1,1,$2)", &[&fixture.id.clone(), &&consumed[..]]);
    fixture.change(
        "DELETE FROM atomic_tree_delta_nodes WHERE manifest_hash=$1 AND node_hash=$2",
        &[&&second[..], &&remaining[..]],
    );
    fixture.mismatch();
    fixture.change(
        "INSERT INTO atomic_tree_delta_nodes(manifest_hash,node_hash,direction) VALUES($1,$2,-1)",
        &[&&second[..], &&remaining[..]],
    );
    fixture.change(
        "INSERT INTO atomic_tree_live_nodes(database_id,node_hash) VALUES($1,$2)",
        &[&fixture.id.clone(), &&consumed[..]],
    );
    fixture.mismatch();
    fixture.change(
        "DELETE FROM atomic_tree_live_nodes WHERE database_id=$1 AND node_hash=$2",
        &[&fixture.id.clone(), &&consumed[..]],
    );
    fixture.inspect(true, false);
    assert!(fixture.fold(second, 4096));
    fixture.inspect(false, true);
}
