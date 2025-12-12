Docs Structure

- spec/: Product and technical specs (canonical plan, build order, next milestones)
- research/: Background notes and design research (Datomic, indexing, envelope, etc.)
- status/: Short status and sprint notes (what’s done, what’s left, current focus)
- decisions/: ADRs for durable architectural decisions

Quick links
- Plan/spec: spec/EDB-PLAN.md
- Build order: spec/BUILD_ORDER.md
- Research index: research/
- Status: status/README.md

API examples

- POST `/q` — minimal JSON query with value predicates and a ref join.
  Body:
  {
    "where": [
      {"a": ":user/name", "op": "eq", "value": "Alice", "var": "?u"},
      {"a": ":post/author", "op": "eq", "var": "?p", "ref_var": "?u"}
    ]
  }
  Response shape: {"rows": [[u_eid, p_eid], ...]} for joins; {"rows": [eid, ...]} for single-var filters.

- POST `/pull` — per-spec limits and component defaults.
  Body:
  {"eid": 1000, "specs": [
    {"type":"Attr","data":":user/name"},
    {"type":"ReverseNestedLimit","data":{"attr":":user/friend","specs":[{"type":"Attr","data":":user/name"}],"limit":2}},
    {"type":"NestedLimit","data":{"attr":":user/address","specs":[{"type":"Attr","data":":addr/city"}],"max_depth":1}}
  ]}
  Notes:
  - ReverseNestedLimit/NestedLimit add per-branch limits and optional per-branch max_depth.
  - Component refs (isComponent=true) expand to nested maps by default.
