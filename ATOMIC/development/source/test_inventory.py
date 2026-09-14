"""Small negative checks for manual ledger accounting; no database required."""

import io
from pathlib import Path
import unittest
from unittest.mock import patch

from inventory import verify_ledger


class LedgerChecks(unittest.TestCase):
    def check_rows(self, rows, expected=("one",)):
        data = "source_path\tcurrent_rust_owner\ttrace_path\tdisposition\n" + rows
        with patch.object(Path, "open", return_value=io.StringIO(data)):
            return verify_ledger(Path("manual.tsv"), "source_path", expected,
                                 ("current_rust_owner", "trace_path"))

    def test_complete_manual_disposition_with_explicit_non_counterpart(self):
        self.assertEqual(self.check_rows("one\t-\tdevelopment/source/README.md\thost adapter\n"), 1)

    def test_duplicate_rejected_even_when_membership_set_matches(self):
        row = "one\t-\tdevelopment/source/README.md\thost adapter\n"
        with self.assertRaisesRegex(ValueError, "Duplicate"):
            self.check_rows(row + row)

    def test_missing_and_extra_membership_rejected(self):
        with self.assertRaisesRegex(ValueError, "missing=.*one.*extra=.*other"):
            self.check_rows("other\t-\tdevelopment/source/README.md\thost adapter\n")

    def test_missing_owner_or_trace_rejected(self):
        for owner, trace in (("src/nonexistent-atlas-owner.rs", "development/source/README.md"),
                             ("-", "datomic_pro_docs/nonexistent-atlas-trace.md")):
            with self.subTest(owner=owner, trace=trace):
                with self.assertRaisesRegex(ValueError, "Missing .* target"):
                    self.check_rows(f"one\t{owner}\t{trace}\tnative adaptation\n")

    def test_empty_disposition_is_not_coverage(self):
        with self.assertRaisesRegex(ValueError, "Empty disposition"):
            self.check_rows("one\t-\tdevelopment/source/README.md\t\n")


if __name__ == "__main__":
    unittest.main()
