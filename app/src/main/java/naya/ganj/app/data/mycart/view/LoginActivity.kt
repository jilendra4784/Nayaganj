package naya.ganj.app.data.mycart.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.OTPViewModel
import naya.ganj.app.databinding.ActivityLoginBinding
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant.MobileNumber
import naya.ganj.app.utility.MyViewModelFactory
import com.google.gson.JsonObject

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var viewModel: OTPViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.include6.ivBackArrow.setOnClickListener { finish() }
        binding.include6.toolbarTitle.text = "Login/SignUp"
        
        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        )[OTPViewModel::class.java]

        binding.btnNextButton.setOnClickListener {
            val mobileNumber: String = binding.textInputLayout.editText?.text.toString()
            if (mobileNumber.length < 10 || mobileNumber.startsWith("0")) {
                Toast.makeText(this, "Please Enter Valid Number", Toast.LENGTH_SHORT).show()
            } else {
                loginRequest(mobileNumber)
            }
        }
    }

    private fun loginRequest(mobileNumber: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(MobileNumber, mobileNumber)

        viewModel.getOTPRequest(jsonObject).observe(this) {
            if (it.status) {
                val intent = Intent(this@LoginActivity, OTPVerifyActivity::class.java)
                intent.putExtra(MobileNumber, mobileNumber)
                startActivity(intent)
            } else {
                Toast.makeText(this@LoginActivity, it.msg, Toast.LENGTH_LONG).show()
            }
        }
    }
}