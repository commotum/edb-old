# Clean-Room Protocol Prompts for Module Reimplementation

---

## Module Context Header (prepend to every stage prompt)

MODULE CONTEXT (fill in before running):
- Module name: <MODULE_NAME>
- Module type: <library | CLI | service | protocol | file format | etc.>
- Intended platform/runtime constraints: <language, OS, WASM, no network, etc.>
- What counts as “public interface” for this module:
  - APIs (functions/types) exposed to other modules
  - CLI commands/flags
  - network endpoints/protocol messages
  - file formats / serialized payloads
  - environment variables / config files

ARTIFACTS PROVIDED (stage-dependent; list exactly what you are attaching):
- <Artifact 1: ...>
- <Artifact 2: ...>

DELIVERABLE NAMING:
- Output folder/name should be: <e.g., prd/<MODULE_NAME>/stage-1 or similar>

---

## Stage 1 Prompt — Analysis / Spec Team (Team A)

ROLE: Team A — Analysis & Specification (Clean-room behavior spec)

GOAL:
Create a complete Product Requirements Document (PRD) / functional specification for the module described in MODULE CONTEXT.
You MUST describe behavior and externally-observable semantics, NOT implementation. Write the spec as if the original never existed.

YOU ARE ALLOWED TO USE (only what is provided in ARTIFACTS PROVIDED):
- Black-box testing (running binaries/services, calling APIs, probing inputs/outputs)
- Public documentation (if provided)
- Protocol traces / logs / I/O captures / performance observations
- Source code ONLY as a behavior reference (you may read it, but you must NOT reuse structure, identifiers, comments, or design)

STRICT RULES (anti-contamination within Stage 1):
- Do NOT copy code, comments, or docstrings.
- Do NOT reproduce internal architecture, module layout, or private types as “requirements”.
- Do NOT use original identifiers (function/type/field names, error names, constants) unless they are part of the public interface contract (e.g., wire protocol field names).
- If the public interface has names that must be preserved (e.g., CLI flags, JSON keys, protocol field names), list them explicitly under “Public Surface (Canonical Names)”.
- Everything else must be renamed generically in the spec (e.g., “InputHandle”, “QueryObject”, “ValidationError”).
- Prefer describing semantics via examples and invariants rather than describing algorithms.

TASKS:
1) Establish Module Boundary
   - Identify the public surface: entry points, inputs, outputs, side effects.
   - Identify what state exists (if any) as a black box: persistent state, caches, sessions, config.

2) Specify Inputs → Outputs
   - For each public entry point:
     - Inputs: type/shape/ranges, required vs optional fields, defaults
     - Outputs: type/shape, ordering, determinism, streaming behavior
     - Side effects: I/O, persistence, network, filesystem, logs

3) Error Model
   - Enumerate error categories and conditions.
   - For each error: triggering conditions, how it is represented (status code, error object), and whether it is recoverable.
   - Include “invalid input” vs “internal failure” vs “resource exhaustion” vs “timeout/cancellation”.

4) State Machine / Lifecycle (if applicable)
   - Describe states and transitions: initialization, open/closed, running/stopped, transaction boundaries, etc.
   - Include concurrency semantics (thread safety, reentrancy, ordering guarantees).

5) Constraints & Invariants
   - Data invariants (e.g., uniqueness, referential integrity, monotonic timestamps).
   - Consistency rules (eventual vs strong).
   - Performance constraints stated as observable requirements (latency expectations, big-O where externally relevant, memory ceilings if observed).

6) Edge Cases & Compatibility
   - Null/empty/zero behaviors
   - Large inputs
   - Unicode/encoding rules
   - Floating point and numeric corner cases
   - Backward/forward compatibility expectations for formats/protocols

7) Observability Requirements
   - What logs/metrics/traces exist or are required for operators/integrators.
   - Deterministic debug hooks if any.

8) Conformance Test Plan (spec-derived)
   - Provide a suite of test cases derived ONLY from this spec:
     - Must-pass examples (input + expected output shape)
     - Property tests (invariants)
     - Negative tests (error conditions)
     - Fuzzing strategy (what to fuzz and what must not happen)
   - Do NOT include any output values copied from the original system; expected results must be described from the spec in an implementation-agnostic way.

OUTPUT FORMAT (deliver exactly these sections, in this order):
A. Overview
B. Glossary & Terminology (generic names)
C. Public Surface (Canonical Names that must be preserved, if any)
D. Inputs & Outputs (by entry point)
E. Data Model (observable structures only)
F. State Machine / Lifecycle
G. Error Model
H. Constraints, Invariants, and Determinism
I. Edge Cases & Undefined/Implementation-Choice Areas (explicitly mark ambiguity)
J. Security/Robustness Considerations (generic)
K. Conformance Test Plan (spec-derived)
L. Open Questions / Decisions Needed (if any)

QUALITY BAR:
- Someone who has never seen the original can implement a compatible module using only this PRD.
- The PRD must not contain “how it works internally”, only “what it must do”.
- Use clear normative language: MUST / MUST NOT / SHOULD / MAY.

---

## Stage 2 Prompt — Legal / Compliance Review (Spec Cleanliness)

ROLE: Stage 2 — Legal/Compliance Reviewer for Clean-room Spec

GOAL:
Audit and sanitize the Stage 1 PRD to ensure it is “clean”:
- No mirrored phrasing from original docs/comments
- No traceable identifiers or unique strings from the original (except unavoidable public interface tokens)
- Spec describes what, not how
Produce a cleaned PRD suitable to hand to Team B (implementation).

YOU WILL BE GIVEN:
- The Stage 1 PRD (draft)
- The original sources used by Team A (code/docs/traces), solely for contamination detection and rephrasing guidance

STRICT RULES:
- Do NOT add new functional requirements beyond what is already in the PRD unless needed to resolve an ambiguity clearly present in the PRD.
- Do NOT introduce any implementation details.
- Do NOT preserve internal names/identifiers from the original system.
- The only identifiers allowed to match the original are those that are externally mandated contracts (e.g., on-the-wire fields, file format keys, CLI flags). Everything else must be generic.

TASKS:
1) Contamination Scan
   - Identify phrases that look copied or too-close paraphrases (especially uncommon wording).
   - Identify any original identifiers that appear (types, internal modules, function names, error names).
   - Identify any suspicious constants (magic numbers, exact wording of errors) that could be traceable.

2) Sanitization Pass
   - Rewrite contaminated portions using fresh phrasing.
   - Replace internal identifiers with generic names.
   - Ensure the spec is framed as requirements, not as a description of the original.

3) “What not how” Enforcement
   - Remove algorithmic descriptions (“it uses X algorithm…”, “it stores in Y index…”) unless it is unavoidable as part of an external guarantee.
   - Convert internal mechanics into externally testable behaviors and invariants.

4) Produce Compliance Artifacts
   - A cleaned PRD (final) ready for Team B
   - A change log summarizing what was altered and why
   - A “Reserved Public Tokens List”: the ONLY names/strings that must match externally (protocol keys, CLI flags, etc.)
   - A short “Clean-room Attestation” statement describing that the resulting PRD is implementation-agnostic and scrubbed

OUTPUTS (deliver all of the following):
1) CLEANED PRD (full document)
2) CHANGE LOG (bulleted list: section → change → reason)
3) RESERVED PUBLIC TOKENS LIST (explicit allowlist)
4) CLEAN-ROOM ATTESTATION (short paragraph)

QUALITY BAR:
- Team B could read the cleaned PRD and there is no obvious fingerprint of the original codebase’s internal structure or naming.
- The PRD is still complete and implementable.

---

## Stage 3 Prompt — Implementation Team (Team B)

ROLE: Team B — Clean-room Implementation

GOAL:
Implement the module from scratch using ONLY the cleaned PRD and any explicitly allowed constraints in MODULE CONTEXT.
Assume the PRD is the single source of truth.

YOU WILL BE GIVEN:
- Cleaned PRD (from Stage 2)
- Platform/runtime constraints (language/toolchain/targets)
- OPTIONAL: Any generic dependencies allowed (e.g., standard libraries), but NO original code, NO original docs, NO traces, NO outputs from Team A testing.

ABSOLUTE RULES:
- Do NOT reference or consult the original system, source code, documentation, or outputs.
- Do NOT recreate naming/structures from the original. Use your own architecture and abstractions.
- If the PRD is ambiguous, choose a reasonable interpretation and document it in a “Decision Log”. Do not seek original behavior.

DELIVERABLES:
1) Architecture & Design (original)
   - Define your internal modules, data structures, and responsibilities.
   - Include rationale for key design decisions (performance, correctness, simplicity).

2) Implementation
   - Implement all public entry points specified by the PRD.
   - Implement error handling, lifecycle/state model, and constraints.
   - Provide clear documentation of the public API.

3) Spec-derived Test Suite
   - Create tests derived ONLY from the PRD:
     - Unit tests for each entry point
     - Property tests for invariants
     - Negative tests for error model
     - Fuzz tests where appropriate
   - Do NOT use any tests copied/adapted from original repositories.

4) Developer Documentation
   - How to build, run, and test
   - Examples of usage consistent with PRD (with invented sample data, not copied examples)

OUTPUT FORMAT:
- /src implementation
- /tests spec-derived tests
- /docs short developer docs
- DECISION_LOG.md (list ambiguous spec points + your chosen resolution)
- SPEC_TRACEABILITY.md mapping PRD sections → code/tests that satisfy them

QUALITY BAR:
- Another engineer can validate conformance against the PRD using your tests and docs.
- The implementation is clearly original in structure and naming.

---

## Stage 4 Prompt — Verification / Validation (Team C / Firewall)

ROLE: Team C — Firewall Verification & Validation

GOAL:
Confirm behavioral equivalence between:
- The original system (reference)
- The clean-room implementation (candidate)
WITHOUT leaking original outputs, internal details, or proprietary identifiers back to Team B.

YOU WILL BE GIVEN:
- The cleaned PRD (Stage 2)
- Access to the original system strictly as a black box (binary/service/API)
- Access to the clean-room implementation (binary/service/API)
- A controlled test harness environment to run both

FIREWALL RULES (critical):
- Do NOT share original outputs, traces, or exact error strings with Team B.
- Do NOT share input cases that are derived from reading the original codebase.
- All test cases must be derived from the PRD, plus generic fuzzing/metamorphic strategies.
- Reports back to Team B must be: PASS/FAIL plus sanitized delta categories and reproduction steps that do NOT include original outputs.
  - If you need to share “expected vs actual”, use:
    - structural diffs (shape mismatch, missing field, ordering difference)
    - hashes of outputs (e.g., SHA-256) instead of raw values where feasible
    - or “oracle-free” properties (metamorphic relations) that don’t require revealing reference values

TASKS:
1) Build a Spec-derived Conformance Suite
   - Convert PRD requirements into executable tests:
     - Deterministic examples (where PRD defines exact behavior)
     - Property tests for invariants
     - Negative tests for error categories
     - Metamorphic tests (input transformations that must preserve or predictably change outputs)
   - Add fuzzing to explore edge cases described in PRD.

2) Differential Execution
   - For each test input:
     - Run against original black box
     - Run against clean implementation
     - Compare results using a comparison policy derived from the PRD:
       - exact match when PRD requires determinism
       - equivalence class match when PRD allows variability (ordering, formatting)
   - Record mismatches in a sanitized manner.

3) Triage & Categorize Deltas
   - Categorize failures by PRD section:
     - output shape mismatch
     - lifecycle/state mismatch
     - error category mismatch
     - determinism/order mismatch
     - performance constraint violation (if specified)
   - Provide minimal reproduction steps referencing only the PRD-derived inputs.

4) Produce a Firewall-safe Report
   - A summary: pass rate, failing requirement IDs, categories
   - For each failure: PRD requirement reference + sanitized symptom + reproduction input + candidate output (allowed) + reference output hash (not raw)
   - Recommendations: whether PRD needs clarification or candidate needs change

OUTPUTS:
- TEST_SUITE/ (spec-derived)
- RUN_REPORT.md (firewall-safe)
- DELTA_SUMMARY.csv (requirement_id, category, reproducible, severity)
- If all pass: CLEAN_EQUIVALENCE_ATTESTATION.md

QUALITY BAR:
- Team B can fix issues using the report without learning proprietary internals or copying reference behavior beyond the PRD.
- Verification is repeatable and automated.
