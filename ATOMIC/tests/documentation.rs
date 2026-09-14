//! User navigation contracts; runtime lifecycle witnesses live in the CLI suites.
use std::collections::BTreeSet;
use std::path::{Path, PathBuf};

fn root() -> PathBuf {
    PathBuf::from(env!("CARGO_MANIFEST_DIR"))
}

fn files_under(directory: &Path, extension: &str, files: &mut Vec<PathBuf>) {
    for entry in std::fs::read_dir(directory).unwrap() {
        let path = entry.unwrap().path();
        if path.is_dir() {
            files_under(&path, extension, files);
        } else if path.extension().is_some_and(|value| value == extension) {
            files.push(path);
        }
    }
}

fn heading_ids(text: &str) -> BTreeSet<String> {
    let mut ids = BTreeSet::new();
    let mut fenced = false;
    for line in text.lines() {
        if line.trim_start().starts_with("```") {
            fenced = !fenced;
            continue;
        }
        if fenced || !line.starts_with('#') {
            continue;
        }
        let title = line.trim_start_matches('#').trim();
        let base: String = title
            .chars()
            .flat_map(char::to_lowercase)
            .filter_map(|ch| {
                if ch.is_alphanumeric() || matches!(ch, '-' | '_') {
                    Some(ch)
                } else if ch.is_whitespace() {
                    Some('-')
                } else {
                    None
                }
            })
            .collect();
        let mut id = base.clone();
        let mut suffix = 0;
        while !ids.insert(id) {
            suffix += 1;
            id = format!("{base}-{suffix}");
        }
    }
    ids
}

fn check_links(file: &Path, failures: &mut Vec<String>) {
    let text = std::fs::read_to_string(file).unwrap();
    // The owned user guides use inline Markdown links. Do not crawl external
    // sites or impose this convention on the preserved reference corpora.
    for tail in text.split("](").skip(1) {
        let Some((destination, _)) = tail.split_once(')') else {
            continue;
        };
        if destination.contains("://") || destination.starts_with("mailto:") {
            continue;
        }
        let (relative, anchor) = destination
            .split_once('#')
            .map_or((destination, None), |(path, anchor)| (path, Some(anchor)));
        let target = if relative.is_empty() {
            file.to_path_buf()
        } else {
            file.parent().unwrap().join(relative)
        };
        if !target.exists() {
            failures.push(format!("{}: missing {destination}", file.display()));
            continue;
        }
        if let Some(anchor) = anchor
            && !anchor.is_empty()
            && target.extension().is_some_and(|value| value == "md")
            && !heading_ids(&std::fs::read_to_string(&target).unwrap()).contains(anchor)
        {
            failures.push(format!("{}: missing anchor {destination}", file.display()));
        }
    }
}

#[test]
fn user_chapters_and_relative_links_resolve_without_flat_aliases() {
    let root = root();
    let docs = root.join("docs");
    let expected = [
        "00_start_here",
        "01_tutorials",
        "02_core_concepts",
        "03_schema",
        "04_transactions",
        "05_query_and_pull",
        "06_indexes",
        "07_peer_api",
        "08_operations",
        "09_optional",
    ];
    let actual = std::fs::read_dir(&docs)
        .unwrap()
        .map(|entry| {
            let entry = entry.unwrap();
            assert!(
                entry.path().is_dir(),
                "no flat forwarding guides: {entry:?}"
            );
            entry.file_name().into_string().unwrap()
        })
        .collect::<BTreeSet<_>>();
    assert_eq!(actual, expected.into_iter().map(String::from).collect());
    for chapter in expected {
        let mut files = Vec::new();
        files_under(&docs.join(chapter), "md", &mut files);
        assert!(!files.is_empty(), "empty chapter {chapter}");
    }
    let mut files = vec![root.join("README.md")];
    files_under(&docs, "md", &mut files);
    files_under(&root.join("development/validation"), "md", &mut files);
    let mut failures = Vec::new();
    for file in files {
        check_links(&file, &mut failures);
    }
    assert!(failures.is_empty(), "{}", failures.join("\n"));
}

fn check_doc_paths(text: &str, origin: &str, failures: &mut Vec<String>) -> usize {
    let mut checked = 0;
    for (offset, _) in text.match_indices("docs/") {
        if text[..offset]
            .chars()
            .next_back()
            .is_some_and(|ch| ch.is_alphanumeric() || matches!(ch, '_' | '-'))
        {
            continue;
        }
        let tail = &text[offset + "docs/".len()..];
        let Some(end) = tail.find(".md") else {
            continue;
        };
        let path = format!("docs/{}", &tail[..end + 3]);
        if path.chars().any(char::is_whitespace) {
            continue;
        }
        checked += 1;
        if !root().join(&path).is_file() {
            failures.push(format!("{origin}: missing {path}"));
        }
    }
    checked
}

#[test]
fn rustdoc_and_example_user_guide_paths_resolve() {
    let mut files = Vec::new();
    files_under(&root().join("src"), "rs", &mut files);
    files_under(&root().join("examples"), "rs", &mut files);
    let mut failures = Vec::new();
    for file in files {
        let text = std::fs::read_to_string(&file).unwrap();
        check_doc_paths(&text, &file.display().to_string(), &mut failures);
    }
    assert!(failures.is_empty(), "{}", failures.join("\n"));
}

#[test]
fn stock_help_points_to_user_chapters_and_explains_offline_inputs() {
    let output = std::process::Command::new(env!("CARGO_BIN_EXE_atomic"))
        .arg("--help")
        .env_remove("ATOMIC_POSTGRES_URL")
        .output()
        .unwrap();
    assert!(output.status.success());
    let help = String::from_utf8(output.stdout).unwrap();
    assert!(help.contains("Data-only queries need no --database"));
    assert!(help.contains("--repository reads a local backup without PostgreSQL"));
    assert!(help.contains("file path '-' reads stdin"));
    assert!(!help.contains("Required environment (all commands except help/version)"));
    let mut failures = Vec::new();
    assert!(check_doc_paths(&help, "atomic --help", &mut failures) > 0);
    assert!(failures.is_empty(), "{}", failures.join("\n"));
}
