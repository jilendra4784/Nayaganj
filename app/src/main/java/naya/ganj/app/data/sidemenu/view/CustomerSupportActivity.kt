package naya.ganj.app.data.sidemenu.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import com.revesoft.revechatsdk.Utility.ReveChat
import com.revesoft.revechatsdk.ui.ReveChatActivity
import com.revesoft.revechatsdk.visitor.VisitorInfo
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.mycart.view.LoginActivity
import naya.ganj.app.databinding.ActivityCustomerSupportBinding
import naya.ganj.app.utility.Constant


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
           /* val emailIntent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", "nayaganj9@gmail.com", null)
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "This is my subject text")
            startActivity(Intent.createChooser(emailIntent, null))*/

            if(app.user.getLoginSession()){
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("nayaganj9@gmail.com"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "User Id:"+app.user.getUserDetails()?.userId)
                intent.type = "message/rfc822"
                startActivity(Intent.createChooser(intent, "Select email"))
            }else{
                MaterialAlertDialogBuilder(this@CustomerSupportActivity)
                    .setTitle("Login?")
                    .setMessage("Please login to connect support.")
                    .setPositiveButton(
                        "GOT IT"
                    ) { dialogInterface, i ->
                        run {
                            dialogInterface.dismiss()
                            Constant.IS_FROM_MYCART=true
                            startActivity(Intent(this@CustomerSupportActivity, LoginActivity::class.java))
                        }
                    }
                    .setNegativeButton(
                        "CANCEL"
                    ) { dialogInterface, i -> }
                    .show()
            }
        }

        //
        //binding.toolbar.ivChatIcon.visibility = View.VISIBLE

        binding.toolbar.ivChatIcon.setOnClickListener {
            Log.e("TAG", "onCreate: $userName,$mNumber,$deviceToken")
            ReveChat.init("7570386")
            val visitorInfo = VisitorInfo.Builder()
                .name(userName).email("")
                .phoneNumber(mNumber).build()

            ReveChat.setVisitorInfo(visitorInfo)
            ReveChat.setDeviceTokenId(deviceToken)
            startActivity(Intent(this, ReveChatActivity::class.java))
        }
        getToken()
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