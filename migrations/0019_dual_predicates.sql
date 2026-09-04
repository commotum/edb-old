-- A single immutable :db/fn binding may serve both documented predicate
-- positions. Kind 4 is the explicit ABI-5 dual-predicate representation;
-- existing kinds 0..3 and their payloads remain unchanged.
ALTER TABLE atomic_programs
    DROP CONSTRAINT IF EXISTS atomic_programs_kind_check;

ALTER TABLE atomic_programs
    ADD CONSTRAINT atomic_programs_kind_check CHECK (kind BETWEEN 0 AND 4);
