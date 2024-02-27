import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

// สร้าง Interface เพื่อรับผลลัพธ์จากการอัปโหลดวิดีโอ
interface OnVideoUploadListener {
    fun onVideoUploadComplete(videoId: String?, videoUrl: String?)
}

// สร้าง JavaScript Interface
class JavaScriptInterface(private val listener: OnVideoUploadListener) {

    private var videoId: String? = null
    private var videoUrl: String? = null

    // เมธอดที่เรียกจาก JavaScript เมื่อได้รับ VideoId หรือ videoUrl
    @JavascriptInterface
    fun onVideoUploaded(id: String?, url: String?) {
        videoId = id
        videoUrl = url

        // เรียกใช้งาน callback ใน listener
        listener.onVideoUploadComplete(videoId, videoUrl)
    }
}

