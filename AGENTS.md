# AGENTS.md

## Project

Slay the Spire Java 8 mod built with Maven. It replaces vanilla cards/relics in place, keeping vanilla IDs
and image paths, so the game's pools, saves, and compendium keep working. Package/mod ID: `anotherspirerework`.
Depends on BaseMod only. Never enable the old `AnotherSpire` mod alongside this one.

Authoritative context:
- `README.md` (Chinese): complete list of card/relic/mechanic changes only; its tables reflect the implemented values.
- `CHANGELOG.md` (Chinese): history of fixes plus an end section「实现与维护参考」with architecture, build/verify,
  hard rules, and known tradeoffs. Read that section before editing.
- `../requirements.txt` (outside the repo, UTF-8): card-value spec. Some values intentionally diverge; trust the code/README tables.

## Commands

Build and tests **must run on JDK 8**. The shell default here is JDK 25, which makes
`PatchContractTest.postfixBindingRegression()` throw an NPE in javassist.

```powershell
$env:JAVA_HOME='C:/Program Files/Zulu/zulu-8'; mvn -B clean package
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify.ps1 -CheckJar
```

- The two test classes are plain `main()` programs executed by `maven-antrun-plugin` in the `test` phase,
  not JUnit. Surefire reports `Tests run: 0`; `-Dtest=...` / `-DskipTests` will not select or skip them.
  Run one directly after `mvn test-compile`:

```powershell
& 'C:/Program Files/Zulu/zulu-8/bin/java.exe' -cp "target/classes;target/test-classes;C:/Program Files (x86)/steam/steamapps/common/SlayTheSpire/desktop-1.0.jar;C:/Program Files (x86)/steam/steamapps/workshop/content/646570/1605060445/ModTheSpire.jar;C:/Program Files (x86)/steam/steamapps/workshop/content/646570/1605833019/BaseMod.jar" anotherspirerework.PatchContractTest
```

- Deployment is manual. `mod.deploy.skip` defaults to `true`, and passing `-Dmod.deploy.skip=false`
  still skips because Ant's `unless` only checks whether the property is set. Copy
  `target/anotherspirerework.jar` into the game's `mods/` folder and launch ModTheSpire with Java 8.
- Build output is `target/anotherspirerework.jar`. `scripts/verify.ps1 -CheckJar` requires that jar;
  without `-CheckJar` it checks sources/resources only.
- Other machine paths: use `mvn -Dlocal.dependencies="<dir>" ...` (profile `local-dependencies`).

## Hard constraints enforced by tests/verify.ps1

- `CardSwapPatch.replacements()` must contain exactly **128 unique** card classes. If you change it,
  update the assertion in `scripts/verify.ps1` and the counts in `README.md`.
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
- `ReworkedRelic` replaces the vanilla instance, so it must forward every vanilla hook whose
  behavior should survive (e.g. `VelvetChoker.onVictory`).
- New content uses namespaced IDs (`anotherspirerework:...`); replacement cards intentionally keep vanilla IDs.

## Gotchas

- `target/` is tracked in git and there is no `.gitignore`. Builds rewrite tracked files, so `git status`
  is noisy; do not delete/untrack `target` or assume build artifacts are disposable unless asked.
- There is no CI, linter, or formatter. Verification layers are: `SaturatingMathTest` (pure math),
  `PatchContractTest` (patches against real dependency JARs), `verify.ps1` (JSON/keys/art/JAR), then
  manual in-game checks. The first three cannot replace in-game validation of timing/UI/saves.
- Main entrypoint: `src/main/java/anotherspirerework/AnotherSpireRework.java` (`@SpireInitializer`).
  Code is split into `cards/<color>/`, `patches/`, `powers/`, `actions/`, `relics/`, `effects/`, `util/`.
- Delayed work (waiting for animations/screens) goes through `receivePostDungeonUpdate`;
  battle start/end cleanup goes through `receiveStartGame` / `receivePostBattle`.
- When changing relic behavior, check whether the relic is wrapped (`ReworkedRelic.supports`) or
  patched directly; both paths must stay consistent.
