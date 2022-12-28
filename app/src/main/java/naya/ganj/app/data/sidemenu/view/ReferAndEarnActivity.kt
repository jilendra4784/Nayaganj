package naya.ganj.app.data.sidemenu.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.ActivityReferAndEarnBinding

class ReferAndEarnActivity : AppCompatActivity() {

    lateinit var binding:ActivityReferAndEarnBinding
    lateinit var app:Nayaganj
    private lateinit var shortLink:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityReferAndEarnBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app=applicationContext as Nayaganj

        init()
    }
    private fun init(){
        binding.cstToolbar.ivBackArrow.setOnClickListener{finish()}
        binding.cstToolbar.toolbarTitle.text=getString(R.string.refer_and_earn)
        if (app.user.getAppLanguage() == 1) {
            binding.cstToolbar.toolbarTitle.text=getString(R.string.refer_and_earn_h)
            binding.aText.setText(getString(R.string.your_referral_money))
            binding.shareLink.setText(getString(R.string.share_link_h))
            binding.referralTitle.setText(getString(R.string.refer_and_earnupto_h))
            binding.referralDescription.setText(getString(R.string.share_referral_link_h))
        }

        binding.shareLink.setOnClickListener {
            shortLink()
        }
    }
    private fun shortLink()
    {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://www.example.com/?userId="+app.user.getUserDetails()?.userId))
            .setDomainUriPrefix("https://nayaganj.page.link")
            //Setting parameters
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .buildShortDynamicLink()

            .addOnSuccessListener{ result->
                //Short link created
                shortLink = result.shortLink!!
                val flowchartLink = result.previewLink
                Log.d("shortLink_1234",""+shortLink)

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "NayaGanj Referral Link")
                intent.putExtra(Intent.EXTRA_TEXT,shortLink.toString())
                startActivity(intent)

            }
            .addOnFailureListener{

                Log.d("shortLink_1234","shortLink")

            }
    }
}