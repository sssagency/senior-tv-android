package org.seniortv.player

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Android TV shell for the EXISTING V2.6 Player web app.
 * Player (Pages) handles SSS registration, playlists, R2 playback and web caching.
 * This is NOT a native Media3 download/playback engine.
 */
class MainActivity : Activity() {
    private var browser: WebView? = null
    private lateinit var root: FrameLayout
    private lateinit var errorPanel: LinearLayout
    private lateinit var errorText: TextView
    private var mainFrameFailed = false
    private var customView: View? = null
    private var customCallback: WebChromeClient.CustomViewCallback? = null
    private val playerUrl: String get() = BuildConfig.PLAYER_URL.trim()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )

        root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(11, 29, 56))
        setContentView(root)
        createErrorPanel()

        if (!validPlayerUrl()) {
            showError("Player 주소가 아직 설정되지 않았습니다.\n\n" +
                "설치 담당자가 Android Studio 프로젝트의 gradle.properties에 " +
                "seniorPlayerUrl=https://실제-Player주소 를 설정한 후 APK를 다시 제작해야 합니다.")
            return
        }
        val web = WebView(this)
        browser = web
        web.setBackgroundColor(Color.rgb(11, 29, 56))
        web.isFocusable = true
        web.isFocusableInTouchMode = true
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.settings.loadsImagesAutomatically = true
        web.settings.allowFileAccess = false
        web.settings.allowContentAccess = false
        web.settings.javaScriptCanOpenWindowsAutomatically = false
        web.settings.setSupportMultipleWindows(false)
        web.settings.cacheMode = WebSettings.LOAD_DEFAULT
        web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        web.settings.userAgentString = web.settings.userAgentString + " SeniorEducationTV/1.0"
        if (Build.VERSION.SDK_INT >= 26) web.settings.safeBrowsingEnabled = true
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false)

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view == null || customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customCallback = callback
                browser?.visibility = View.GONE
                root.addView(view, FrameLayout.LayoutParams(-1, -1))
            }
            override fun onHideCustomView() {
                customView?.let { root.removeView(it) }
                customView = null
                customCallback?.onCustomViewHidden()
                customCallback = null
                browser?.visibility = View.VISIBLE
            }
        }
        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                mainFrameFailed = false
            }
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return true
                return !isTrustedNavigation(uri)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                if (!mainFrameFailed && url != null && isTrustedNavigation(Uri.parse(url))) hideError()
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    mainFrameFailed = true
                    showError("교육 화면을 열 수 없습니다.\n인터넷 연결을 확인하고 다시 시도해주세요.")
                }
            }
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: android.webkit.WebResourceResponse?) {
                if (request?.isForMainFrame == true) {
                    mainFrameFailed = true
                    showError("교육 화면 응답 오류가 발생했습니다. 다시 시도해주세요.")
                }
            }
            @Suppress("DEPRECATION")
            override fun onReceivedSslError(view: WebView?, handler: android.webkit.SslErrorHandler?, error: android.net.http.SslError?) {
                handler?.cancel()
                mainFrameFailed = true
                showError("안전한 연결을 확인할 수 없습니다. 서버 주소와 인증서를 확인해주세요.")
            }
        }
        root.addView(web, 0, FrameLayout.LayoutParams(-1, -1))
        web.requestFocus()
        web.loadUrl(playerUrl)
    }

    private fun validPlayerUrl(): Boolean {
        return try {
            val u = Uri.parse(playerUrl)
            val schemeOk = u.scheme == "https" || (BuildConfig.DEBUG && u.scheme == "http")
            schemeOk && !u.host.isNullOrBlank() &&
                !playerUrl.contains("CHANGE-TO-YOUR", ignoreCase = true) &&
                !playerUrl.contains("YOUR-", ignoreCase = true) &&
                !playerUrl.contains("replace-me", ignoreCase = true)
        } catch (_: Exception) { false }
    }

    private fun isTrustedNavigation(other: Uri): Boolean {
        val allowed = Uri.parse(playerUrl)
        return other.scheme == allowed.scheme && other.host.equals(allowed.host, true) &&
            other.port == allowed.port
    }

    private fun createErrorPanel() {
        errorPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(11, 29, 56))
            gravity = android.view.Gravity.CENTER
            setPadding(48, 35, 48, 35)
            visibility = View.GONE
        }
        val title = TextView(this).apply {
            text = "시니어교육TV"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
        }
        errorText = TextView(this).apply {
            textSize = 19f
            setTextColor(Color.rgb(221, 231, 243))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 28, 0, 28)
        }
        val retry = Button(this).apply {
            text = "다시 연결하기"
            textSize = 19f
            isFocusable = true
            setOnClickListener {
                if (validPlayerUrl()) {
                    hideError()
                    browser?.loadUrl(playerUrl)
                }
            }
        }
        errorPanel.addView(title)
        errorPanel.addView(errorText)
        errorPanel.addView(retry, LinearLayout.LayoutParams(320, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(errorPanel, FrameLayout.LayoutParams(-1, -1))
    }

    private fun showError(message: String) {
        Log.w("SeniorEducationTV", message)
        errorText.text = message
        errorPanel.visibility = View.VISIBLE
        errorPanel.bringToFront()
        errorPanel.requestFocus()
    }
    private fun hideError() {
        errorPanel.visibility = View.GONE
        browser?.requestFocus()
    }
    private fun dispatchPlayerKey(key: String) {
        browser?.evaluateJavascript(
            "document.dispatchEvent(new KeyboardEvent('keydown',{key:'${key}',bubbles:true,cancelable:true}))",
            null
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> { dispatchPlayerKey("Enter"); true }
            KeyEvent.KEYCODE_BACK -> { dispatchPlayerKey("Escape"); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    @Deprecated("Compatibility with traditional TV remote Back key")
    override fun onBackPressed() { dispatchPlayerKey("Escape") }
    override fun onPause() { browser?.onPause(); super.onPause() }
    override fun onResume() { super.onResume(); browser?.onResume() }
    override fun onDestroy() {
        if (customView != null) { customView?.let { root.removeView(it) }; customView = null }
        browser?.let { root.removeView(it); it.destroy() }
        browser = null
        super.onDestroy()
    }
}
