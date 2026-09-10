-- Compatibility fence for portable query-template 4 / program ABI 11 and
-- explicitly declared native predicate deployments. No canonical data, program
-- bytes or derived reference meaning is rewritten. Older binaries must not
-- resume authoritative work after this quiesced upgrade.
SELECT 1;
