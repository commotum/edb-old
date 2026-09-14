#!/usr/bin/env python3
"""Mechanical provenance atlas and comment-only guard; not semantic coverage.

Run --write to regenerate inventory.tsv, or --check to verify the inventory,
manual source/passage ledgers and preservation of every baseline body.
Manual dispositions are never regenerated. Uses Git and Python's stdlib.
"""

import argparse
import csv
import difflib
import hashlib
import io
from pathlib import Path
import re
import subprocess
import tarfile


ROOT = Path(__file__).resolve().parents[2]
BASELINE = "cd7192e63d883a4a34aa7de4d5bcd17e6edb692d"
CORPUS = "1.0.7705"
ROOTS = [f"{CORPUS}/{artifact}/{language}" for artifact in ("peer", "transactor")
         for language in ("src-clj", "src-java")]
INVENTORY = Path(__file__).with_name("inventory.tsv")


def baseline_sources():
    archive = subprocess.run(
        ["git", "archive", BASELINE, *ROOTS], cwd=ROOT, check=True,
        stdout=subprocess.PIPE,
    ).stdout
    with tarfile.open(fileobj=io.BytesIO(archive)) as entries:
        return {entry.name: entries.extractfile(entry).read()
                for entry in entries.getmembers()
                if entry.isfile() and entry.name.endswith((".clj", ".cljc", ".java"))}


def exact_sources():
    path = f"{CORPUS}/transactor/source-manifests/exact-clojure-sources.tsv"
    data = subprocess.run(["git", "show", f"{BASELINE}:{path}"], cwd=ROOT,
                          check=True, stdout=subprocess.PIPE).stdout.decode()
    return {entry["source_path"]: entry["owner_archive"]
            for entry in csv.DictReader(io.StringIO(data), delimiter="\t")}


def reading_group(relative):
    """Navigation hints from names, deliberately not claims of semantic ownership."""
    name = relative.rsplit("/", 1)[-1].rsplit(".", 1)[0]
    if not relative.startswith(("datomic/", "com/datomic/")):
        return "dependency-or-resource"
    if relative.startswith("datomic/functions/"):
        return "jvm-callable-boundary"
    if relative.startswith("datomic/core2/"):
        return "core2-abstractions-and-providers"
    if any(part in name for part in ("cassandra", "dynamo", "hotrod", "ddb", "s3", "h2", "aws", "ec2", "iam", "cloudwatch", "memcached")):
        return "non-target-provider-or-integration-evidence"
    if any(part in name for part in ("index", "btset", "tree", "log", "codec", "fressian", "datum", "datom")):
        return "shared-structures-and-encoding"
    if any(part in name for part in ("query", "datalog", "pull", "aggregate", "aggregation", "lucene", "fulltext", "Entity")):
        return "query-navigation-and-search"
    if any(part in name for part in ("cache", "memory", "Memory")):
        return "caching-and-residency"
    if any(part in name for part in ("update", "transact", "lifecycle", "coordination", "launcher", "indexer")):
        return "write-processing-and-coordination"
    if any(part in name for part in ("backup", "garbage", "excise", "cleanup", "monitor", "catalog", "integrity")):
        return "operations-and-lifecycle"
    if any(part in name for part in ("peer", "api", "client", "server", "Connection", "Peer", "artemis")):
        return "application-and-transport-boundaries"
    if any(part in name for part in ("cluster", "kv_", "sql", "val_", "storage", "io")):
        return "storage-abstractions-and-providers"
    return "shared-model-or-support-to-inspect"


def render(sources):
    exact = exact_sources()
    output = io.StringIO()
    writer = csv.writer(output, delimiter="\t", lineterminator="\n")
    writer.writerow(["path", "origin", "archive", "reading_group_hint",
                     "counterpart", "baseline_relation", "baseline_sha256"])
    for path, data in sorted(sources.items()):
        _, artifact, language, relative = path.split("/", 3)
        other = "transactor" if artifact == "peer" else "peer"
        counterpart = f"{CORPUS}/{other}/{language}/{relative}"
        relation = "only-in-this-artifact"
        if counterpart in sources:
            relation = "byte-identical" if sources[counterpart] == data else "same-path-not-byte-identical"
        else:
            counterpart = ""
        archive = exact.get(relative, "") if artifact == "transactor" else ""
        if language == "src-java":
            origin = "recovered-java-origin"
        elif archive:
            origin = "exact-datomic-dependency" if relative.startswith("datomic/") else "exact-bundled-dependency"
        elif relative == "data_readers.clj":
            origin = "exact-packaged-resource"
        else:
            origin = "recovered-datomic-initializer"
        writer.writerow([path, origin, archive, reading_group(relative), counterpart,
                         relation, hashlib.sha256(data).hexdigest()])
    return output.getvalue()


def verify_comments(sources):
    found = {path.relative_to(ROOT).as_posix() for directory in ROOTS
             for path in (ROOT / directory).rglob("*")
             if path.is_file() and path.suffix in (".clj", ".cljc", ".java")}
    if found != set(sources):
        raise ValueError(f"Source inventory changed: {found ^ set(sources)}")
    changed = 0
    for path, before in sources.items():
        after = (ROOT / path).read_bytes()
        if after == before:
            continue
        changed += 1
        old_lines = before.decode().splitlines(keepends=True)
        new_lines = after.decode().splitlines(keepends=True)
        prefix = (";",) if path.endswith((".clj", ".cljc")) else ("//",)
        for tag, a, b, c, d in difflib.SequenceMatcher(None, old_lines, new_lines, autojunk=False).get_opcodes():
            if tag == "equal":
                continue
            if any(line.strip() for line in old_lines[a:b]):
                raise ValueError(f"Baseline text removed/replaced in {path}:{a + 1}")
            if any(line.strip() and not line.lstrip().startswith(prefix) for line in new_lines[c:d]):
                raise ValueError(f"Non-comment addition in {path}:{c + 1}")
    return changed


def verify_ledger(path, key, expected, local_fields):
    """Check complete manual accounting, not the truth of a disposition."""
    with path.open(newline="") as stream:
        reader = csv.DictReader(stream, delimiter="\t")
        required = {key, "disposition", *local_fields}
        if not required.issubset(reader.fieldnames or ()):
            raise ValueError(f"Missing ledger columns in {path}: {required}")
        entries = list(reader)
    seen = set()
    for row in entries:
        entry = row[key]
        if entry in seen:
            raise ValueError(f"Duplicate {key} in {path}: {entry}")
        seen.add(entry)
        if not row["disposition"].strip():
            raise ValueError(f"Empty disposition in {path}: {entry}")
        for field in local_fields:
            target = row[field]
            if not target:
                raise ValueError(f"Empty {field} in {path}: {entry}; use - explicitly")
            if target != "-" and not (ROOT / target).is_file():
                raise ValueError(f"Missing {field} target in {path}: {target}")
    if seen != set(expected):
        raise ValueError(f"Ledger membership differs in {path}: "
                         f"missing={sorted(set(expected) - seen)}, "
                         f"extra={sorted(seen - set(expected))}")
    return len(entries)


def verify_ledgers(sources):
    directory = INVENTORY.parent
    source_count = verify_ledger(
        directory / "coverage.tsv", "source_path", sources,
        ("counterpart_path", "current_rust_owner", "trace_path"),
    )
    chapters = {path.relative_to(ROOT).as_posix()
                for path in (ROOT / "datomic_pro_docs").rglob("*.md")
                if not path.name.endswith(".atomic.md")}
    chapter_count = verify_ledger(
        directory / "passages.tsv", "reference_path", chapters, ("trace_path",),
    )
    return source_count, chapter_count


def verify_trace_links():
    """Resolve local Markdown destinations in study companions and the atlas."""
    files = [INVENTORY.with_name("README.md"),
             *(ROOT / "datomic_pro_docs").rglob("*.atomic.md")]
    checked = 0
    for path in files:
        for match in re.finditer(r"\]\((<?[^\s)]+>?)(?:\s+\"[^\"]*\")?\)",
                                 path.read_text()):
            target = match.group(1).strip("<>")
            if target.startswith(("#", "http:", "https:", "mailto:")):
                continue
            target = re.sub(r":\d+$", "", target.split("#", 1)[0])
            resolved = ROOT / target.lstrip("/") if target.startswith("/") else path.parent / target
            if not resolved.exists():
                raise ValueError(f"Broken local trace link in {path.relative_to(ROOT)}: {target}")
            checked += 1
    return checked


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--write", action="store_true")
    mode.add_argument("--check", action="store_true")
    args = parser.parse_args()
    sources = baseline_sources()
    expected = render(sources)
    if args.write:
        INVENTORY.write_text(expected)
    elif INVENTORY.read_text() != expected:
        raise ValueError("inventory.tsv differs from the baseline; regenerate with --write")
    changed = verify_comments(sources)
    source_count, chapter_count = verify_ledgers(sources)
    links = verify_trace_links()
    print(f"{len(sources)} source files accounted for; {changed} contain comment-only study changes; baseline {BASELINE}")
    print(f"{source_count} source dispositions; {chapter_count} chapter dispositions; {links} local trace links resolve")


if __name__ == "__main__":
    main()
