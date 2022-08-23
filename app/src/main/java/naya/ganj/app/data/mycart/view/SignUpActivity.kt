package naya.ganj.app.data.mycart.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.PersonalDetailViewModel
import naya.ganj.app.databinding.ActivitySignUpBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import naya.ganj.app.utility.Utility
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    lateinit var viewModel: PersonalDetailViewModel
    lateinit var app: Nayaganj
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = applicationContext as Nayaganj
        binding.include.ivBackArrow.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.include.toolbarTitle.text = "User Detail"

        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        ).get(PersonalDetailViewModel::class.java)
        binding.btnSubmit.setOnClickListener {

            if (binding.textNameinputLayout.editText?.text.toString().equals("")) {
                Toast.makeText(this, "Please Enter Name", Toast.LENGTH_LONG).show()
            } else if (binding.edtEmail.editText?.text.toString().equals("")) {
                Toast.makeText(this, "Please Enter Email Id", Toast.LENGTH_LONG).show()
            } else if (!isValidEmailId(binding.edtEmail.editText?.text.toString())) {
                Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            } else {

                if (Utility.isAppOnLine(this@SignUpActivity, object : OnInternetCheckListener {
                        override fun onInternetAvailable() {
                            updateProfileRequest(
                                binding.textNameinputLayout.editText?.text.toString(),
                                binding.edtEmail.editText?.text.toString()
                            )
                        }
                    }))

                    updateProfileRequest(
                        binding.textNameinputLayout.editText?.text.toString(),
                        binding.edtEmail.editText?.text.toString()
                    )
            }
        }
    }

    private fun isValidEmailId(emailId: String): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(emailId).matches()
    }

    private fun updateProfileRequest(name: String, emailId: String) {

        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.newname, name)
        jsonObject.addProperty(Constant.newemailid, emailId)
        jsonObject.addProperty(Constant.OTP, "")
        jsonObject.addProperty("newMobileNumber", "")

        viewModel.updateProfileRequet(app.user.getUserDetails()?.userId, jsonObject).observe(this) {
            if (it.status) {
                val i = Intent(this, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
            }
        }
    }
}