#!/usr/bin/env python3
"""Mechanical provenance atlas and comment-only guard; not semantic coverage.

Run --write to regenerate inventory.tsv, or --check to verify the inventory and
that source study has preserved every baseline body. Uses Git and Python's stdlib.
"""

import argparse
import csv
import difflib
import hashlib
import io
from pathlib import Path
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
    print(f"{len(sources)} source files accounted for; {changed} contain comment-only study changes; baseline {BASELINE}")


if __name__ == "__main__":
    main()
