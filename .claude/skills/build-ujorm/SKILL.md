---
name: build-ujorm
description: Release a new version of the Ujorm library (ProjectUjorm, github.com/pponec/ujorm) end to end - pull request with a description of all changes, README/changes.txt review, verification of the dependent projects against the SNAPSHOT, merge, release notes, the release/yyyy-MM-dd/X.Y.Z branch without SNAPSHOT, the Maven Central upload hand-off, the next SNAPSHOT bump and switching the dependent projects (orm-benchmarks, ujorm-petstore, jWorkSheet, PPSee) to the released version. Use whenever the user wants to release, publish, "připravit novou verzi", "vydat Ujorm", "release větev", bump the Ujorm version or upgrade the dependent projects to a new Ujorm, even if they list only some of these steps.
---

# Ujorm release

The whole release of the Ujorm library, generalized from a real run. The user drives it in Czech;
answer in Czech, keep commit messages, PR texts and changes.txt in English (the repository language).

The procedure has **two gates where the user acts** - the merge of the pull request (unless they
tell you to merge it yourself) and the upload to Maven Central (GPG signing needs their pinentry).
Stop at each gate, say what is ready, and continue only after their answer. Everything else you
do yourself, step by step, reporting short progress lines.

## Projects

| Project | Path | Remote | Notes |
|---|---|---|---|
| Ujorm (the library) | `/home/pavel/Development/ProjectUjorm` | GitHub `pponec/ujorm` (SSH) | main branch = `origin/HEAD` (currently `ujorm3`) |
| orm-benchmarks | `/home/pavel/Development/orm-benchmarks` | GitHub (SSH) | property `ujorm.version` in root `pom.xml` |
| ujorm-petstore | `/home/pavel/Development/ujorm-petstore` | GitHub (SSH) | `ujorm.version` in `pom.xml`, the version also in `README.md` ("ORM and Web: Ujorm X") |
| jWorkSheet | `/home/pavel/Development/ProjectJWorkSheet` | GitHub (SSH) | `ujorm.version` in `pom.xml` |
| PPSee | `/home/pavel/Development/PPSee` | Bitbucket (SSH) | `ujorm.version` in `pom.xml` **and** `pom-native.xml` |

The dependent projects stay on whatever branch they are checked out on - commit there, don't switch
branches. Read the current branch with `git branch --show-current` and mention it in the report.

If the user says "we are in project X", make sure the session's directory is the right one before
touching anything (`change_directory`). The release steps below always run in ProjectUjorm, even
when the session started elsewhere.

## Step 0 - Preflight (ProjectUjorm)

```bash
cd /home/pavel/Development/ProjectUjorm
git fetch -q origin
git status -sb                                  # current branch, ahead/behind, dirty files
git remote show origin | grep 'HEAD branch'     # the main branch
git log --oneline origin/<main>..HEAD           # commits of the release
grep -m1 '<version>' pom.xml                    # X.Y.Z-SNAPSHOT
head -20 project-m2/ujo-core/src/main/java/org/ujorm/core/doc-files/changes.txt
```

Derive: `SNAPSHOT = X.Y.Z-SNAPSHOT`, `RELEASE = X.Y.Z`, `NEXT = X.Y.(Z+1)-SNAPSHOT`, `TODAY = yyyy-MM-dd`.
If the working tree has unrelated uncommitted changes, ask before going on.

## Step 1 - Pull request

1. Push the feature branch (`git push origin <branch>`) - it is often one commit ahead.
2. Write the PR body from the `release X.Y.Z-SNAPSHOT (?)` section of `changes.txt` plus the commit list,
   grouped by module (META, CORE, ORM, API (breaking), Documentation), with a short summary on top.
   End it with the attribution line from the system reminder.
3. `gh pr create --base <main> --head <branch> --title "... (X.Y.Z)" --body-file <file in scratchpad>`
4. Send the user the PR link.

## Step 2 - Documentation review (before the merge)

Compare `git diff origin/<main>...HEAD` against the docs and fix the gaps on the feature branch
(commit + push, so they land in the PR). Check at least:

- **README.md** - the Maven Dependencies section shows the coming `RELEASE` for `ujo-core`, `ujo-orm`
  and `ujorm-meta-processor` (it tends to lag several versions behind); every new feature has a section
  and a TOC entry; a breaking change has a migration note; wording matches the code (e.g. a generated
  type that changed from class to interface); alternative annotations are mentioned next to the
  original ones in FAQ and side remarks.
- **changes.txt** - one `+ MODULE: ...` line per user-visible change, including documentation changes.
  The header stays `release X.Y.Z-SNAPSHOT (?)` until the merge.
- **CLAUDE.md / llms.txt** - only when the change touches what they describe.

Report what was missing and what you changed.

## Step 3 - Verify the dependent projects against the SNAPSHOT

Install the library locally without signing (GPG fails without pinentry), then build each project
with a property override - **don't edit their poms in this step**:

```bash
cd /home/pavel/Development/ProjectUjorm && ./mvnw -B install -DskipTests -Dgpg.skip
cd <project> && ./mvnw -B -U clean test -Dujorm.version=<SNAPSHOT>
```

Send build logs to the scratchpad and grep for `BUILD`, `ERROR]`, `Tests run: N, Failures` and
`pre-compiled N domain handler(s)`. Beyond the tests, run the apps when it is cheap - the tests of
some projects barely touch Ujorm:

- **orm-benchmarks**: the `ujorm3` module has no tests. `./mvnw -B install -DskipTests -Dujorm.version=<SNAPSHOT>`,
  then run the Ujorm benchmark on H2 with a small iteration count. The runner appends to `~/ujo-benchmark.csv`;
  keep the user's file clean by redirecting the home directory:
  `MAVEN_OPTS="-Xmx4G -Duser.home=<scratchpad> -Dmaven.repo.local=$HOME/.m2/repository" ./mvnw -B exec:java -pl benchmark -Dujorm.version=<SNAPSHOT> -Dexec.classpathScope=test -Dexec.mainClass=org.benchmark.runner.BenchmarkRunner -Dexec.args="UJORM 2000" -Dbenchmark.db.profile=h2 -s $HOME/.m2/settings.xml`
- **ujorm-petstore**: start `./mvnw -B jetty:run -Dujorm.version=<SNAPSHOT>` in the background, wait for
  HTTP 200 on `http://localhost:8080/` (check the port is free first), check that the pet table has rows
  and `/info` answers, then stop Jetty and confirm the port is free again. Stop it by its PID or a narrow
  pattern - a broad `pkill -f` can kill your own shell.
- **jWorkSheet**, **PPSee**: `clean test` (PPSee: also `-f pom-native.xml validate`).

Note any untracked or modified files you find there, and leave them alone.

## Step 4 - Merge (gate)

Wait until the user merges the PR or tells you to merge it. Merge the way the recent PRs were merged
(check `gh pr list --state merged --limit 5`; lately it is **squash**):

```bash
gh pr merge <N> --squash --subject "<PR title> (#<N>)"
git checkout <main> && git pull --ff-only
```

## Step 5 - Release notes on the main branch

In `changes.txt` change `release X.Y.Z-SNAPSHOT (?)` to `release X.Y.Z (TODAY)`.
Commit `Add release X.Y.Z notes to changes.txt`, push to the main branch.

## Step 6 - Release branch without SNAPSHOT

Branch name: **`release/TODAY/X.Y.Z`** (e.g. `release/2026-11-22/3.0.7`). Never create a bare
`release/TODAY` - git can't hold both `release/D` and `release/D/V` refs, and the user wants the version
in the name.

```bash
git checkout -b release/<TODAY>/<RELEASE>
files=$(git grep -l '<SNAPSHOT escaped>' -- '*pom.xml')
sed -i 's/<SNAPSHOT escaped>/<RELEASE>/g' $files
```

Also set the line `ProjectUjorm (root pom.xml, version ...)` in `CLAUDE.md` to `RELEASE`. The previous
release touched 12 files (11 poms + CLAUDE.md) - compare with `git show --stat` of the last
`Release ...` commit; `tools/MetaGen/pom.xml` has its own `1.0-SNAPSHOT` and stays.

Run the full build with tests: `./mvnw -B clean install -Dgpg.skip` (all modules must pass), then
commit `Release X.Y.Z` and `git push -u origin release/<TODAY>/<RELEASE>`.

If a branch with a wrong name was already pushed: rename locally via a temporary name, delete the
remote one (`git push origin --delete <old>`), `git fetch --prune`, push the new one with `-u`.

## Step 7 - Maven Central upload (gate)

Ask the user to upload `RELEASE` from the release branch to Maven Central (they sign it with GPG).
Don't wait idly for the answer if the next step doesn't depend on it - the SNAPSHOT bump can follow.

## Step 8 - Next SNAPSHOT on the main branch

```bash
git checkout <main>
sed -i 's/<SNAPSHOT escaped>/<NEXT>/g' $(git grep -l '<SNAPSHOT escaped>' -- '*pom.xml')
```

- `CLAUDE.md`: `ProjectUjorm (root pom.xml, version <NEXT>)`.
- `changes.txt`: add an empty section `release <NEXT> (?)` above `release X.Y.Z (TODAY)`.
- Verify with `./mvnw -B install -DskipTests -Dgpg.skip` (an offline `validate` fails on the
  not-yet-built SNAPSHOT artifacts, so don't use it as the check).
- Commit `<NEXT>` (e.g. `3.0.8-SNAPSHOT`), push.

## Step 9 - Switch the dependent projects to the release

Only when the user asks (usually right after the upload). For each project:

1. Set `ujorm.version` to `RELEASE` (PPSee: both poms); update a version mention in README when it
   describes the current dependency (petstore). Leave a version that labels measured results alone
   (the orm-benchmarks README) and say so.
2. `./mvnw -B clean test`; report tests and pre-compiled handlers per project in a table.
3. Check `https://repo1.maven.org/maven2/org/ujorm/ujo-core/<RELEASE>/ujo-core-<RELEASE>.pom` - Central
   sync lags; a 404 means the builds used the local `~/.m2` and CI elsewhere will fail until the sync.
4. Ask before committing. Then commit only the version files (`git add pom.xml ...`, never `-a` -
   the projects carry unrelated dirty files like `.idea/compiler.xml`), message `Ujorm X.Y.Z`, push.
   If a push fails on an HTTPS remote without credentials, offer to switch `origin` to the SSH URL.

## Commit conventions

- Messages in English, imperative-ish, no prefixes beyond the module style already used
  (`README: ...`, `changes.txt: ...`, `Release X.Y.Z`, `X.Y.Z-SNAPSHOT`, `Ujorm X.Y.Z`).
- End each commit message with the `Co-Authored-By` line from the system reminder.

## Final report

Close with a short summary: PR link, merged commit, release branch and its commit, next SNAPSHOT
commit, and per dependent project the branch, commit and push state. Name anything not verified
(e.g. native build not run, Central not synced yet).
