// Fixed, reviewed SQL identifiers only. Seeds and victims are bounded by the
// caller; canonical headers stay present until the complete DAG is discovered.
pub(super) const SEEDS: &[(i16, &str)] = &[
    (
        7,
        "SELECT manifest_hash AS hash FROM atomic_tree_manifests WHERE database_id=$1 UNION SELECT manifest_hash AS hash FROM atomic_tree_build_intents WHERE database_id=$1",
    ),
    (
        8,
        "SELECT manifest_hash AS hash FROM atomic_request_base_archives WHERE database_id=$1",
    ),
    (
        6,
        "SELECT manifest_hash AS hash FROM atomic_index_manifests WHERE database_id=$1",
    ),
    (
        1,
        "SELECT root_hash AS hash FROM atomic_tree_manifest_roots WHERE manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        1,
        "SELECT root_hash AS hash FROM atomic_request_base_archive_roots WHERE manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=8)",
    ),
    (
        1,
        "SELECT node_hash AS hash FROM atomic_tree_live_nodes WHERE database_id=$1",
    ),
    (
        1,
        "SELECT node_hash AS hash FROM atomic_tree_retired_nodes WHERE database_id=$1",
    ),
    (
        1,
        "SELECT i.node_hash AS hash FROM atomic_tree_build_intent_nodes i JOIN atomic_tree_nodes n USING(node_hash) WHERE i.manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        1,
        "SELECT node_hash AS hash FROM atomic_tree_delta_nodes WHERE manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        1,
        "SELECT node_hash AS hash FROM atomic_request_base_archive_nodes WHERE manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=8)",
    ),
    (
        2,
        "SELECT current_root AS hash FROM atomic_semantic_commitment_roots WHERE database_id=$1 AND current_root IS NOT NULL",
    ),
    (
        3,
        "SELECT root_hash AS hash FROM atomic_fulltext_page_roots WHERE manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        3,
        "SELECT block_hash AS hash FROM atomic_fulltext_pages WHERE created_for IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        5,
        "SELECT program_hash AS hash FROM atomic_program_generation_refs WHERE database_id=$1",
    ),
];
// Explicitly drain FK children before parents, including ON DELETE CASCADE
// children. No corpus-sized cascade is hidden behind a one-row header delete.
pub(super) const DETACH: &[(&str, &str)] = &[
    ("atomic_remote_writer_endpoints", "database_id=$1"),
    ("atomic_change_checkpoints", "database_id=$1"),
    ("atomic_generation_request_bases", "database_id=$1"),
    ("atomic_generation_request_tempids", "database_id=$1"),
    ("atomic_generation_requests", "database_id=$1"),
    (
        "atomic_receipt_archive_frontier",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind IN(7,8))",
    ),
    ("atomic_receipt_archive_conversions", "database_id=$1"),
    (
        "atomic_request_base_archive_completions",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=8)",
    ),
    (
        "atomic_request_base_archive_nodes",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=8)",
    ),
    (
        "atomic_request_base_archive_roots",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=8)",
    ),
    ("atomic_request_base_archives", "database_id=$1"),
    (
        "atomic_tree_build_intent_nodes",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        "atomic_tree_delta_nodes",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    ("atomic_tree_build_intents", "database_id=$1"),
    ("atomic_tree_retired_nodes", "database_id=$1"),
    ("atomic_tree_retirement_progress", "database_id=$1"),
    ("atomic_tree_retirements", "database_id=$1"),
    ("atomic_tree_publications", "database_id=$1"),
    ("atomic_tree_live_nodes", "database_id=$1"),
    ("atomic_tree_live_sets", "database_id=$1"),
    (
        "atomic_tree_publication_states",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        "atomic_tree_delta_headers",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        "atomic_tree_manifest_roots",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        "atomic_fulltext_blocks",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        "atomic_fulltext_page_roots",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        "atomic_fulltext_page_builds",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        "atomic_fulltext_projections",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    (
        "atomic_fulltext_garbage",
        "manifest_hash IN(SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=7)",
    ),
    ("atomic_tree_manifests", "database_id=$1"),
    ("atomic_index_publications", "database_id=$1"),
    ("atomic_index_manifests", "database_id=$1"),
    ("atomic_completed_excision_requests", "database_id=$1"),
    ("atomic_generation_excision_predicates", "database_id=$1"),
    ("atomic_semantic_commitment_roots", "database_id=$1"),
    ("atomic_program_generation_refs", "database_id=$1"),
    ("atomic_heads", "database_id=$1"),
    (
        "atomic_log_generation_collection_progress",
        "database_id=$1",
    ),
    ("atomic_log_generation_retirements", "database_id=$1"),
    ("atomic_log_generation_completions", "database_id=$1"),
    ("atomic_log_generation_completion_stages", "database_id=$1"),
    ("atomic_log_generation_checkpoints", "database_id=$1"),
    (
        "atomic_log_generation_abandonment_progress",
        "database_id=$1",
    ),
    ("atomic_log_generation_garbage_contents", "database_id=$1"),
    ("atomic_log_generation_activations", "database_id=$1"),
    ("atomic_generation_transactions", "database_id=$1"),
    ("atomic_log_generation_builds", "database_id=$1"),
    ("atomic_log_generations", "database_id=$1"),
    ("atomic_requests", "database_id=$1"),
    ("atomic_transactions", "database_id=$1"),
    ("atomic_transactor_leases", "lease_scope=$1"),
];
