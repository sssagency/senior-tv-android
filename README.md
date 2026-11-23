# 시니어교육TV Android TV 앱 (V2.7 웹 Player 연동)

## 무엇을 만들었나요?
기존 V2.7 **웹 Player를 TV에서 바로 실행**하는 Android TV / Google TV 전용 앱 프로젝트입니다.

- TV 홈 화면에서 시니어교육TV 아이콘으로 실행
- 시작하면 미리 지정해 둔 Player 웹사이트 열기 (현장에서 API 주소 입력 불필요)
- 전체화면 / 가로형 / 리모컨 방향키 및 확인 버튼(WebView 네이티브 전달)
- 리모컨 MENU / BACK을 Enter / Escape로 전달
- 기존 Player의 SSS 번호 입력 → 기관 정보 확인 → CMS 승인 → 교육 선택
- WebView 로컬 저장소에 TV 인증정보가 유지되므로 보통 재입력 불필요
- 부팅 후 시작 시도(장비 OS에 따라 제한될 수 있음)
- 네트워크 오류·SSL 오류 시 한국어 안내와 다시 연결하기 버튼
- TV 배너·아이콘에 시니어스포츠산업진흥원 로고 사용

**범위:** 설치·실험용 **WebView TV 앱**입니다. **Media3 네이티브 영상 저장/재생을 구현한 APK는 아닙니다.** 오프라인 재생은 기존 V2.7 웹 Player 캐시 정책 및 장비 WebView 동작에 의존하므로 실제 TV에서 확인해야 합니다.

## 아주 쉬운 시작

1. 먼저 GitHub + Cloudflare로 **V2.7 Player를 배포**합니다. Player 주소 예: `https://나의-player.pages.dev`.
2. 이 프로젝트를 GitHub의 새 저장소 `senior-tv-android`에 업로드합니다. `.github/workflows/build-apk.yml`도 함께 올립니다.
3. GitHub 저장소의 **Actions → Android TV APK 만들기 → Run workflow**를 누릅니다.
4. `player_url` 칸에 실제 배포한 Player의 **HTTPS 주소**를 입력하고 실행합니다.
5. 성공 후 해당 실행 화면 아래 **Artifacts → senior-education-tv-android-debug-apk**를 눌러 APK 압축을 다운로드합니다.
6. 다운로드한 압축파일을 풀면 `app-debug.apk`가 들어 있습니다. 해당 APK를 Android TV/Google TV에 설치합니다.
7. TV 앱을 실행 → CMS에 있는 `SSS01` 입력 → 기관명 확인 → CMS에서 승인하면 연결됩니다.

> 위 GitHub Actions는 **APK를 클라우드에서 빌드하도록 준비한 파일**입니다. 이 대화 실행 환경에는 Android SDK/Gradle이 없어 실제 APK를 컴파일하지 못했으며, GitHub Actions에서 빌드 성공 여부도 아직 검증되지 않았습니다. 제작한 소스와 설정 파일 검사는 수행했습니다.

## Android Studio로 직접 APK 만들기

1. [Android Studio](https://developer.android.com/studio) 설치.
2. `Open`에서 **이 폴더**를 선택.
3. `gradle.properties`의 `seniorPlayerUrl`을 실제 Player 주소로 변경.
4. Gradle Sync 완료(처음에는 Android SDK/Gradle 다운로드 필요).
5. `Build → Build App Bundle(s) / APK(s) → Build APK(s)` 선택.
6. `app/build/outputs/apk/debug/app-debug.apk` 파일 확인.
7. 테스트 장비에서 설치할 수 있는 디버그 APK입니다. **정식 배포 전에는 전용 서명키로 Release APK를 만들어야 합니다.**

## 필수 확인

- APK는 **Android TV / Google TV / Android 셋톱박스**용입니다. LG webOS, 삼성 Tizen TV에는 설치할 수 없습니다.
- 현장 기기에는 앱 안에 **Player 주소를 미리 넣어** 빌드합니다. Worker API 주소는 Player `config.js`에 미리 설정합니다.
- 생산(Release)에는 `https://`만 허용하고 디버그용 빌드에서만 `http://` 테스트가 가능합니다.
- TV에 APK를 복사해 설치하려면 USB/파일 관리자 또는 ADB가 필요하며, 기기 설정에서 해당 설치 도구의 '알 수 없는 앱 설치'를 허용해야 할 수 있습니다.
- Android는 백그라운드에서 화면을 자동으로 시작하는 것을 제한할 수 있으므로 **부팅 후 자동 실행은 보장하지 않습니다.** 운영 기기에서 실제 확인해야 합니다.
- 앱 업데이트 시 `applicationId`와 서명키를 일관되게 유지하세요. 디버그 APK와 운영 Release APK는 보통 동일한 서명키가 아닙니다.
- 작업용 GitHub 저장소는 Private으로 생성하는 것을 권장합니다. 비밀번호·R2 비밀키는 코드에 넣지 마세요.

상세한 사용법은 통합 패키지의 `01_처음부터_차근차근_설치가이드.md`에 있습니다.


## V2.7 재생 로그
V2.7 Player를 앱 WebView에서 열면 Player의 재생 이벤트가 IndexedDB에 쌓이고 Workers로 전송됩니다. 별도 Android 브리지 없이 웹 Player가 로그를 수집합니다. 다만 WebView 저장소의 보존은 기기 OS 정책에 따르며, 실제 Android TV에서 장시간 오프라인 및 재접속 실기기 검증은 필요합니다. 네이티브 Media3 앱은 별도로 개발해야 합니다.
