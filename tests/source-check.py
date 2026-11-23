from pathlib import Path
root=Path(__file__).resolve().parents[1]
checks={
"Manifest TV launcher":"android.intent.category.LEANBACK_LAUNCHER" in (root/'app/src/main/AndroidManifest.xml').read_text(),
"Player URL config":"seniorPlayerUrl" in (root/'app/build.gradle.kts').read_text(),
"App screen lock":"FLAG_KEEP_SCREEN_ON" in (root/'app/src/main/java/org/seniortv/player/MainActivity.kt').read_text(),
"WebView auto play":"mediaPlaybackRequiresUserGesture = false" in (root/'app/src/main/java/org/seniortv/player/MainActivity.kt').read_text(),
"No plaintext release":"android:usesCleartextTraffic=\"false\"" in (root/'app/src/main/AndroidManifest.xml').read_text(),
"GitHub APK workflow":"assembleDebug" in (root/'.github/workflows/build-apk.yml').read_text(),
}
for name,okay in checks.items():print(("PASS" if okay else "FAIL"),name)
assert all(checks.values())
