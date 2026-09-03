ALTER TABLE atomic_programs
    DROP CONSTRAINT IF EXISTS atomic_programs_kind_check;

ALTER TABLE atomic_programs
    ADD CONSTRAINT atomic_programs_kind_check CHECK (kind BETWEEN 0 AND 3);
