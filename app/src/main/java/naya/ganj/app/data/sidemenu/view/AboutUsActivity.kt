package naya.ganj.app.data.sidemenu.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.databinding.ActivityAboutUsBinding

class AboutUsActivity : AppCompatActivity() {
    lateinit var binding: ActivityAboutUsBinding
    lateinit var app: Nayaganj
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        binding.include9.ivBackArrow.setOnClickListener { finish() }
        binding.include9.toolbarTitle.text = "About Us"

        setData()
    }

    private fun setData() {
        if (app.user.getAppLanguage() == 1) {
            // Set for Hindi Language
        }
    }
}