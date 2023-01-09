package naya.ganj.app

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import naya.ganj.app.data.category.model.CategoryDataModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.view.OfferBottomSheetDetail.Companion.TAG
import naya.ganj.app.data.mycart.viewmodel.LoginResponseViewModel
import naya.ganj.app.deliverymodule.view.DeliveryBoyDashboardActivity
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.retrofit.URLConstant
import naya.ganj.app.utility.*
import retrofit2.Call
import retrofit2.Response
import java.util.*
import javax.security.auth.callback.Callback


class SplashActivity : AppCompatActivity() {

    private var deviceId = ""
    private var deviceToken = ""
    lateinit var app: Nayaganj

    lateinit var viewModel: LoginResponseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_splash)


        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        )[LoginResponseViewModel::class.java]
        app = applicationContext as Nayaganj

        val ima = findViewById<ImageView>(R.id.imageView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Thread {
                val decodedAnimation = ImageDecoder.decodeDrawable(ImageDecoder.createSource(resources, R.drawable.front_logo))
                ima.setImageDrawable(decodedAnimation)
                (decodedAnimation as? AnimatedImageDrawable)?.start()
            }.start()
        }

        if(Utility.isAppOnLine(this@SplashActivity,object : OnInternetCheckListener{
                override fun onInternetAvailable() {
                    getDeviceId()
                    getToken()
                }
            })){
            getDeviceId()
            getToken()
            getDynamicLink()
        }
    }

    private fun getDynamicLink() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Log.e(TAG, "getDynamicLink: "+deepLink )
                    val offerKey = deepLink!!.getQueryParameter("userId")
                    Log.e(TAG, "offerKey: "+offerKey )
                }

            }
            .addOnFailureListener(
                this
            ) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    private fun getToken() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            deviceToken = task.result
            Log.e("TAG", "deviceToken:   "+ deviceToken )
            if (app.user.getLoginSession()) {
                URLConstant.BaseImageUrl=""
                app.user.getUserDetails()?.userId?.let {
                        autoLoginAPi(it, deviceId, deviceToken)
                }

            } else {

                Utility.isAppOnLine(this@SplashActivity,object:OnInternetCheckListener{
                    override fun onInternetAvailable() {
                        sendConfigRequest()
                    }
                })
                sendConfigRequest()
            }
        })
    }

    private fun sendConfigRequest() {

        val jsonObject=JsonObject()
        jsonObject.addProperty("required",true)

        RetrofitClient.instance.sendConfigRequest(jsonObject).enqueue(object :
            retrofit2.Callback<ConfigModel> {
            override fun onResponse(
                call: Call<ConfigModel>,
                response: Response<ConfigModel>
            ) {
                if(response.isSuccessful){
                    if(response.body()!!.status){
                        URLConstant.BaseImageUrl=response.body()!!.configData.productImgUrl
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        }, 2000)
                    }
                }else{
                    Utility.showToast(this@SplashActivity,response.message())
                }

            }

            override fun onFailure(call: Call<ConfigModel>, t: Throwable) {
                Utility.serverNotResponding(this@SplashActivity,t.message.toString())
            }
        })

    }

    private fun getDeviceId() {

        if (Build.VERSION.SDK_INT >= 29) {

            val uniquePseudoID =
                "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10
            val serial = Build.getRadioVersion()
            deviceId =
                UUID(uniquePseudoID.hashCode().toLong(), serial.hashCode().toLong()).toString()

            Log.d("Uniqueid_device_id12345", "" + deviceId)

        } else {
            deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }

    private fun autoLoginAPi(userId: String, deviceId: String, DeviceToken: String) {
        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty(Constant.deviceId, deviceId)
            jsonObject.addProperty(Constant.deviceToken, DeviceToken)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        viewModel.getAutoLoginResponse(userId, jsonObject).observe(this) { response ->
            when(response){

                is NetworkResult.Success ->{
                    val it=response.data!!
                    if (it.status) {
                        if (it.userDetails.role == "deliveryBoy") {
                            Toast.makeText(
                                this@SplashActivity,
                                it.msg,
                                Toast.LENGTH_SHORT
                            ).show()
                            app.user.setLoginSession(true)
                            app.user.saveUserDetail(it.userDetails)
                            val intent = Intent(applicationContext, DeliveryBoyDashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            app.user.setLoginSession(true)
                            app.user.saveUserDetail(it.userDetails)
                            finish()
                        }
                    } else {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                        Toast.makeText(this@SplashActivity, it.msg, Toast.LENGTH_SHORT).show()
                    }
                }
                is NetworkResult.Error ->{
                    Utility.serverNotResponding(this@SplashActivity,response.message.toString())
                }
            }
        }
    }
}