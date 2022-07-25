package naya.ganj.app.data.sidemenu.view

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import naya.ganj.app.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    lateinit var binding: ActivityPrivacyPolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.include8.ivBackArrow.setOnClickListener { finish() }
        binding.include8.toolbarTitle.text = "Privacy Policy"
        loadData()
    }

    private fun loadData() {
        val policyUrl =
            "https://www.freeprivacypolicy.com/live/d60e1482-23ee-45bc-9be1-59ffcb4a59ea"
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
        binding.webview.loadUrl(policyUrl)
    }
}