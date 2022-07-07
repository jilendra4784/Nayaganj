package naya.ganj.app.data.mycart.view

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import naya.ganj.app.R
import naya.ganj.app.databinding.ActivityOtpactivityBinding
import naya.ganj.app.utility.Constant.MobileNumber

class OTPActivity : AppCompatActivity() {

    lateinit var binding: ActivityOtpactivityBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        countDownTimer()

    }

    private fun initData() {
        val mobileNumber = intent.getStringExtra(MobileNumber)
        binding.otpMobileNumber.text = "Enter the OTP sent to +91-" + mobileNumber

        binding.editNumber.setOnClickListener {
            finish()
        }

        binding.verifyOtp.setOnClickListener {
            verifyOTPRequest()
        }

        binding.resendOtp.setOnClickListener {
            countDownTimer()
            reSendOTPRequest()
        }
    }

    private fun verifyOTPRequest() {

    }

    private fun reSendOTPRequest() {

    }

    private fun countDownTimer() {

        binding.resendOtp.isEnabled = false
        binding.resendOtp.setTextColor(ContextCompat.getColor(this@OTPActivity, R.color.gray))
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
                        this@OTPActivity,
                        R.color.red_color
                    )
                )

            }
        }.start()
    }
}