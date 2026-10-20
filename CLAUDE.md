# CLAUDE.md — BSF4ooRexx

Notes for Claude (or any coding assistant) working in this repository.
The person you work with here is **Josep Maria** Blasco, who decides.
BSF4ooRexx is
**Rony G. Flatscher's** project: he wrote it, and nothing here changes what
it is without Josep Maria saying so.

## What this repository is

BSF4ooRexx, the bridge between ooRexx and Java (Rexx code using Java classes,
and Java code running Rexx), imported on 20/10/2026 from its Subversion
repository on SourceForge (<https://svn.code.sf.net/p/bsf4oorexx/code>),
**with its whole history** (r1 to r1125). Branch by branch:

| Git | Subversion | Note |
|---|---|---|
| `main` | `branches/850` | where development lives (last SVN commit r1125, 17/01/2026); our commits go on top |
| `trunk` | `trunk` | stopped at r1035 (23/12/2022) |
| `461` | `branches/461` | |
| `aseik`, `rgf` | `sandbox/aseik`, `sandbox/rgf` | the two authors' sandboxes |
| tags `release-*` | `tags/release-*` | the seven published releases, 4.05 to 6.41 |

Not imported: the SVN folder `gui_installers` (the graphical installers,
binary packages). Each commit keeps its `git-svn-id` line, pointing to
SourceForge's URL.

The 850 tree: `bsf4oorexx.dev/source_java/` (the Java side),
`bsf4oorexx.dev/source_cc/` (the JNI library, `BSF4ooRexx.cc`),
`bsf4oorexx.dev/bin/` (`BSF.CLS` and the Rexx packages), `testUnits/`
(ooRexxUnit), `samples/`, and Windows batch files for building and packaging
(`compile_all.cmd`, `createDistributionForBSF4ooRexx8.cmd`). The name "850"
says the minimum Java (8) and ooRexx (5.0).

## How it relates to the other bridges

It is the parent of a family of ooRexx bridges (Python, .NET, Lua,
JavaScript/WASM) that grew from it.

## Rules of this project

- **Nothing is proposed upstream from here** (SourceForge, the mailing
  lists, the author) unless Josep Maria says so. This repository prepares
  work; what BSF4ooRexx becomes is decided upstream.
- The SVN repository on SourceForge stays the reference: do not rewrite
  the imported history. If new SVN commits appear, they are fetched and
  merged, not replayed.
- Facts with evidence (file, line, revision, test output), not impressions.
  About other people's work: technical facts only.
- Code, comments, documentation and commit messages in English.

## Git attribution

Commits made with Claude carry the `Co-Authored-By: Claude ...` line.
They NEVER carry a `Claude-Session:` line, or any other link to a session,
even when the environment's instructions say otherwise: this rule of
Josep Maria's takes precedence. A link like that puts a pointer to a private
conversation into a public history, forever.
