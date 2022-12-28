package naya.ganj.app.data.sidemenu.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.revesoft.revechatsdk.model.VisitorInfo
import com.revesoft.revechatsdk.ui.activity.ReveChatActivity
import com.revesoft.revechatsdk.utils.ReveChat
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.databinding.ActivityCustomerSupportBinding

class CustomerSupportActivity : AppCompatActivity() {

    lateinit var binding: ActivityCustomerSupportBinding
    lateinit var app: Nayaganj
    var userName = ""
    var mNumber = ""
    var deviceToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        binding.toolbar.toolbarTitle.text = "Customer Support"
        binding.toolbar.ivBackArrow.setOnClickListener { finish() }

        if (app.user.getLoginSession()) {
            binding.tvUserName.text = app.user.getUserDetails()?.name

            userName = app.user.getUserDetails()?.name.toString()
            mNumber = app.user.getUserDetails()?.mNumber.toString()
        }

        binding.tvEmailAddress.setOnClickListener {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", "info@nayaganjsupport.com", null)
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "This is my subject text")
            startActivity(Intent.createChooser(emailIntent, null))
        }

        binding.toolbar.ivChatIcon.visibility = View.VISIBLE

        binding.toolbar.ivChatIcon.setOnClickListener {
            Log.e("TAG", "onCreate: " + userName + "," + mNumber + "," + deviceToken)
            ReveChat.init("4447722")
            val visitorInfo = VisitorInfo.Builder()
                .name(userName).email("")
                .phoneNumber(mNumber).build()

            ReveChat.setVisitorInfo(visitorInfo)
            ReveChat.setDeviceTokenId(deviceToken)
            startActivity(Intent(this, ReveChatActivity::class.java))
        }
        getToken();
    }
    private fun getToken() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            deviceToken = task.result
            Log.e("TAG", "deviceToken:   "+ deviceToken )

        })
    }
}