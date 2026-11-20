# Baseline profile

Records startup and recipe-list scrolling so ART precompiles those paths on install (smoother first
scroll, faster cold start, especially on older devices). The profile is written to
`androidApp/src/release/generated/baselineProfiles/` and picked up by the normal release build.

## Regenerate

The generator doesn't contain credentials, so sign in once by hand first:

1. Connect one device (API 28+, a physical device gives the best profile).
2. `./gradlew :androidApp:installBenchmarkRelease` — installs `dev.juanrincon.simmerly.benchmark`
   next to the normal app, so it doesn't touch the debug build's data.
3. Open that app and log in to your server. Leave it on the recipe list.
4. `./gradlew :androidApp:generateBaselineProfile`
5. Commit the generated `baseline-prof.txt` / `startup-prof.txt`.

Regenerate after big UI or navigation changes. The generator scrolls and taps by screen position, so
adjust `BaselineProfileGenerator` if the recipe list's layout changes a lot.
