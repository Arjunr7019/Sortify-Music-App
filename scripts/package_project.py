"""Package the native project, reference source, and verified debug APK."""
import hashlib
import json
import pathlib
import shutil
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
DIST = ROOT / "dist"
DIST.mkdir(exist_ok=True)
APK = ROOT / "app/build/outputs/apk/debug/app-debug.apk"
if not APK.is_file():
    raise SystemExit("Build :app:assembleDebug first")
shutil.copy2(APK, DIST / "Sortify-debug.apk")
roots = ["app", "gradle", "scripts", "docs", "Sortify-Music-App-main"]
files = ["README.md", "PRODUCT.md", "DESIGN.md", "settings.gradle.kts", "build.gradle.kts", "gradle.properties", "gradlew", "gradlew.bat", ".gitignore", ".impeccable/design.json", ".impeccable/surfaces/android-app.md"]
paths = [ROOT / f for f in files]
for folder in roots:
    paths += [p for p in (ROOT / folder).rglob("*") if p.is_file() and not any(part in ("build", "__pycache__", "node_modules", ".gradle") for part in p.relative_to(ROOT).parts)]
with zipfile.ZipFile(DIST / "Sortify-Kotlin-source.zip", "w", zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(set(paths)):
        if not path.exists():
            raise SystemExit("Missing project artifact: " + str(path))
        archive.write(path, "Sortify-Kotlin/" + path.relative_to(ROOT).as_posix())
checksums = {name: hashlib.sha256((DIST / name).read_bytes()).hexdigest() for name in ("Sortify-debug.apk", "Sortify-Kotlin-source.zip")}
(DIST / "SHA256SUMS.json").write_text(json.dumps(checksums, indent=2) + "\n", encoding="utf-8")
for name, checksum in checksums.items():
    print(name, (DIST / name).stat().st_size, checksum)
