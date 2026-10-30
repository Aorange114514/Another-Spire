# AGENTS.md

## Project

Slay the Spire Java 8 mod built with Maven. It replaces vanilla cards/relics in place, keeping vanilla IDs
and image paths, so the game's pools, saves, and compendium keep working. Package/mod ID: `anotherspirerework`.
Depends on BaseMod only. Never enable the old `AnotherSpire` mod alongside this one.

Authoritative context:
- `README.md` (Chinese) and `README.en.md` (English): complete list of card/relic/mechanic changes only; their
  tables reflect the implemented values. Keep the two language editions structurally identical (same rows,
  counts, vanilla IDs and links); each starts with a 中文/English switcher.
- `CHANGELOG.md` (Chinese): history of fixes plus an end section「实现与维护参考」with architecture, build/verify,
  hard rules, and known tradeoffs (anchor `#maintenance`). Read that section before editing.
- `requirements.txt` (UTF-8, repository root or its parent directory): card-value spec, not shipped content.
  Some values intentionally diverge; trust the code/README tables.

## Commands

Build and tests **must run on JDK 8**. A newer JDK makes
`PatchContractTest.postfixBindingRegression()` throw an NPE in javassist.

Set the following to the paths on your machine (never commit them):

```powershell
$env:JAVA_HOME   = '<JDK 8 install directory>'
$env:STS_JAR     = '<Steam>/steamapps/common/SlayTheSpire/desktop-1.0.jar'
$env:MTS_JAR     = '<Steam>/steamapps/workshop/content/646570/1605060445/ModTheSpire.jar'
$env:BASEMOD_JAR = '<Steam>/steamapps/workshop/content/646570/1605833019/BaseMod.jar'
```

```powershell
mvn -B clean package
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify.ps1 -CheckJar
```

- The two test classes are plain `main()` programs executed by `maven-antrun-plugin` in the `test` phase,
  not JUnit. Surefire reports `Tests run: 0`; `-Dtest=...` / `-DskipTests` will not select or skip them.
  Run one directly after `mvn test-compile`:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -cp "target/classes;target/test-classes;$env:STS_JAR;$env:MTS_JAR;$env:BASEMOD_JAR" anotherspirerework.PatchContractTest
```

- Deployment is manual. `mod.deploy.skip` defaults to `true`, and passing `-Dmod.deploy.skip=false`
  still skips because Ant's `unless` only checks whether the property is set. Copy
  `target/anotherspire.jar` into the game's `mods/` folder and launch ModTheSpire with Java 8.
- Build output is `target/anotherspire.jar` (the artifactId is `anotherspire`). `scripts/verify.ps1 -CheckJar`
  requires that jar; without `-CheckJar` it checks sources/resources only.
- Other machine paths: use `mvn -Dlocal.dependencies="<dir>" ...` (profile `local-dependencies`).

## Hard constraints enforced by tests/verify.ps1

- `CardSwapPatch.replacements()` must contain exactly **130 unique** card classes. If you change it,
  update the assertion in `scripts/verify.ps1` and the counts in `README.md` and `README.en.md`.
- `RelicSwapPatch.replacements()` must contain exactly **45 unique** reworked relic classes, one class per file
  under `src/main/java/anotherspirerework/relics/`; `verify.ps1` checks the count and that each class extends its
  vanilla relic.
- Localization keys are `"${modID}:<vanilla ID>"` under `src/main/resources/anotherspirerework/localization/{eng,zhs}/`.
  Eng and zhs must have identical key/field sets and identical dynamic-token sequences
  (`!D!`, `!M!`, `[E]`, `[R]`, etc.; case-sensitive).
- In a value-returning `@SpirePostfixPatch`, the first parameter must be `__result`. ModTheSpire binds hooks
  positionally (result first, then instance); instance hooks must also declare the target instance.
  Getting this wrong makes copies alias the source card.
- Every `AbstractPower` subclass must declare a static field named `powerStrings`.
- Card art must be exactly 250x190 or 500x380. Power icons go in `images/powers/` as
  `<power>_48.png` (48x48) or `<power>_128.png` (128x128), square.
- If a card uses `UPGRADE_DESCRIPTION`, `upgrade()` must explicitly assign it to `rawDescription`
  and call `initializeDescription()`.
- Prefer Prefix/Postfix/semantic locators; use `Replace`/`Insert` only when unavoidable.
- Relics follow the card pattern: one class per reworked relic in `relics/<Name>.java`, extending the vanilla
  relic class and swapped in by `RelicSwapPatch`; vanilla hooks that are not overridden are inherited, so do not
  write a wrapper class. A relic whose vanilla class overrides `updateDescription()` must override it too,
  otherwise the vanilla text replaces the mod text on load.
- New content uses namespaced IDs (`anotherspirerework:...`); replacement cards and relics intentionally keep
  vanilla IDs.

## Gotchas

- `target/` is tracked in git by project convention (the jar, classes and resources ship with the repo).
  Builds rewrite tracked files, so `git status` is noisy; do not delete/untrack the rest of `target` or assume
  build artifacts are disposable unless asked. The generated Maven metadata directories that embed absolute
  build-machine paths — `target/maven-status/`, `target/surefire-reports/`, `target/antrun/` — are listed in
  `.gitignore` and must not be committed.
- Never commit machine-specific information: absolute paths (`C:\Users\...`), usernames, home directories,
  JDK/Steam/Maven install paths, environment dumps or build logs. Keep commands in docs parameterized with
  placeholders such as `$env:JAVA_HOME`.
- `verify.ps1 -CheckJar` needs `target/anotherspire.jar`; the source-only run (no `-CheckJar`) is the
  docs/README gate and does not need the game or a built jar.
- There is no CI, linter, or formatter. Verification layers are: `SaturatingMathTest` (pure math),
  `PatchContractTest` (patches against real dependency JARs), `verify.ps1` (JSON/keys/art/JAR), then
  manual in-game checks. The first three cannot replace in-game validation of timing/UI/saves.
- Main entrypoint: `src/main/java/anotherspirerework/AnotherSpireRework.java` (`@SpireInitializer`).
  Code is split into `cards/<color>/`, `patches/`, `powers/`, `actions/`, `relics/`, `effects/`, `util/`.
- Delayed work (waiting for animations/screens) goes through `receivePostDungeonUpdate`;
  battle start/end cleanup goes through `receiveStartGame` / `receivePostBattle`.
- When changing relic behavior, edit that relic's own class in `relics/<Name>.java`. Cross-cutting rules stay
  in `patches/`: `RelicSwapPatch` replaces the prototypes, `ActFourRelicBlockPatch` hides 27 relics in act 4,
  `RelicTierPatch` repairs relics pools saved before a tier change, `ShopRelicPatch` owns pool draws.
