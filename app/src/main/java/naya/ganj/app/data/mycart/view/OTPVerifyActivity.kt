package naya.ganj.app.data.mycart.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.LoginResponseViewModel
import naya.ganj.app.databinding.ActivityOtpactivityBinding
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class OTPVerifyActivity : AppCompatActivity() {

    lateinit var binding: ActivityOtpactivityBinding
    private var countDownTimer: CountDownTimer? = null
    var deviceToken: String? = null
    var deviceId: String? = null
    lateinit var viewModel: LoginResponseViewModel
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        getToken()
        getDeviceId()
        app = applicationContext as Nayaganj

        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        ).get(LoginResponseViewModel::class.java)

        initData()
        countDownTimer()

    }

    private fun initData() {
        val mobileNumber = intent.getStringExtra(Constant.MobileNumber)
        binding.otpMobileNumber.text = "Enter the OTP sent to +91-" + mobileNumber

        binding.editNumber.setOnClickListener {
            finish()
        }

        binding.verifyOtp.setOnClickListener {
            verifyOTPRequest(mobileNumber)
        }

        binding.resendOtp.setOnClickListener {
            countDownTimer()
            reSendOTPRequest()
        }
    }

    private fun verifyOTPRequest(mobileNumber: String?) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.MobileNumber, mobileNumber)
        jsonObject.addProperty(Constant.deviceId, deviceId)
        jsonObject.addProperty(Constant.deviceToken, deviceToken)
        jsonObject.addProperty(Constant.OTP, binding.otpViewEdittext.otp.toString())

        viewModel.getLoginResponse("", jsonObject).observe(this) {
            if (it.status) {
                syncCartData()
                if (it.isNewUser) {
                    Toast.makeText(this@OTPVerifyActivity, it.msg, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@OTPVerifyActivity, SignUpActivity::class.java))
                    app.user.saveUserDetail(it.userDetails)
                    app.user.setLoginSession(true)
                } else {
                    if (it.userDetails.role.equals("deliveryBoy")) {
                        // Open Deliver Boy Module
                        //TODO need to Implement Delivery Boy Module
                        Toast.makeText(
                            this@OTPVerifyActivity,
                            it.msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        app.user.saveUserDetail(it.userDetails)
                        app.user.setLoginSession(true)
                        val intent = Intent(this@OTPVerifyActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun syncCartData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listOfProduct =
                AppDataBase.getInstance(this@OTPVerifyActivity).productDao().getProductList()
            if (listOfProduct.isNotEmpty()) {
                runOnUiThread {
                    val jsonArray = JSONArray()
                    for (item in listOfProduct) {
                        val itemsData = JSONObject()
                        itemsData.put(Constant.PRODUCT_ID, item.productId)
                        itemsData.put(Constant.VARIANT_ID, item.variantId)
                        itemsData.put(Constant.QUANTITY, item.itemQuantity)
                        jsonArray.put(itemsData)
                    }

                    val jsonObject = JSONObject()
                    jsonObject.put(Constant.PRODUCT, jsonArray)

                    val myreqbody = JSONObject(jsonObject.toString()).toString()
                        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                    viewModel.synchDataRequest(app.user.getUserDetails()!!.userId, myreqbody)
                        .observe(this@OTPVerifyActivity) {
                            if (it.status) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    AppDataBase.getInstance(this@OTPVerifyActivity).productDao()
                                        .deleteAllProduct()
                                }
                            }
                        }
                }
            } else {
                val intent = Intent(this@OTPVerifyActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun reSendOTPRequest() {

    }

    private fun countDownTimer() {
        binding.resendOtp.isEnabled = false
        binding.resendOtp.setTextColor(ContextCompat.getColor(this@OTPVerifyActivity, R.color.gray))
        binding.tvOtptimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                /*if (sharedPreference.getlanguage(ApiConstants.language) == 1) {

                   // binding.tvOtptimer.setText(getString(R.string.time_remaining_h)+" 0:" + millisUntilFinished / 1000 +"s")
                }
                else
                {
                    binding.tvOtptimer.setText("Time Remaining "+"0:" + millisUntilFinished / 1000 +"s")
                }*/
                binding.tvOtptimer.setText("Time Remaining " + "0:" + millisUntilFinished / 1000 + "s")
            }

            override fun onFinish() {

                binding.resendOtp.isEnabled = true
                binding.tvOtptimer.visibility = View.INVISIBLE
                binding.resendOtp.setTextColor(
                    ContextCompat.getColor(
                        this@OTPVerifyActivity,
                        R.color.red_color
                    )
                )

            }
        }.start()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            deviceToken = task.result
            Log.d("DeviceToken", "" + deviceToken)
        })
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId() {
        deviceId = if (Build.VERSION.SDK_INT >= 29) {
            val uniquePseudoID =
                "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10
            val serial = Build.getRadioVersion()
            UUID(uniquePseudoID.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } else {
            Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
}