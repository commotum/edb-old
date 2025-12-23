.
├── edb-edn
│   ├── Cargo.toml
│   ├── src
│   │   ├── entities.rs
│   │   ├── intern_set.rs
│   │   ├── lib.rs
│   │   ├── matcher.rs
│   │   ├── namespaceable_name.rs
│   │   ├── parse.rs
│   │   ├── pretty_print.rs
│   │   ├── query.rs
│   │   ├── symbols.rs
│   │   ├── types.rs
│   │   ├── utils.rs
│   │   └── value_rc.rs
│   └── tests
│       └── parse.rs
├── edb-encoding
│   ├── Cargo.toml
│   ├── src
│   │   ├── bin
│   │   │   └── edb-enc.rs
│   │   ├── lib.rs
│   │   ├── scalar.rs
│   │   └── tuple.rs
│   └── tests
│       └── vectors.rs
├── edb-envelope
│   ├── Cargo.toml
│   └── src
│       └── lib.rs
├── edb-index
│   ├── Cargo.toml
│   ├── src
│   │   └── lib.rs
│   ├── target
│   │   ├── aevt_1765516202294135.sqlite
│   │   ... (many more rows)
│   │   └── vaet_1766472863318329.sqlite
│   └── tests
│       ├── aevt_compact.rs
│       ├── aevt.rs
│       ├── avet_ranges_types.rs
│       └── avet_vaet.rs
├── edb-pull
│   ├── Cargo.toml
│   ├── src
│   │   └── lib.rs
│   └── tests
│       ├── pull_basic.rs
│       └── pull_limits.rs
├── edb-query
│   ├── Cargo.toml
│   ├── src
│   │   └── lib.rs
│   └── tests
│       └── simple_query.rs
├── edb-schema
│   ├── Cargo.toml
│   ├── src
│   │   ├── lib.rs
│   │   ├── schema.rs
│   │   ├── sqlite.rs
│   │   └── value_type_set.rs
│   └── tests
│       ├── schema_load.rs
│       └── value_type_set.rs
├── edb-server
│   ├── Cargo.toml
│   ├── README.md
│   ├── src
│   │   └── main.rs
│   └── tests
├── edb-store-sqlite
│   ├── Cargo.toml
│   └── src
│       └── lib.rs
├── edb-transactor
│   ├── Cargo.toml
│   ├── src
│   │   └── lib.rs
│   ├── target
│   │   ├── env_add_1765511896633988.sqlite
│   │   ... (many more rows)
│   │   └── tx_meta_asof_1766472876131686.sqlite
│   └── tests
│       ├── alias.rs
│       ├── db_views.rs
│       ├── envelope_neg.rs
│       ├── envelope.rs
│       ├── replay.rs
│       ├── smoke.rs
│       ├── time_views.rs
│       └── tx_meta_and_asof.rs
└── edb-tx
    ├── Cargo.toml
    ├── src
    │   ├── allocator.rs
    │   ├── edn.rs
    │   ├── grammar.rs
    │   ├── lib.rs
    │   ├── model.rs
    │   ├── traits.rs
    │   ├── txfn.rs
    │   └── validate.rs
    └── tests
        ├── tx_basic.rs
        ├── tx_cas.rs
        ├── tx_edn.rs
        └── tx_grammar.rs

35 directories, 430 files