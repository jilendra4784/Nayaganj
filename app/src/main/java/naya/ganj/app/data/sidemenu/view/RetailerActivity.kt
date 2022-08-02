package naya.ganj.app.data.sidemenu.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.RetailerViewModel
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.databinding.ActivityRetailerBinding
import naya.ganj.app.retrofit.RetrofitClient

class RetailerActivity : AppCompatActivity() {
    lateinit var binding: ActivityRetailerBinding
    lateinit var viewModel: RetailerViewModel
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRetailerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = applicationContext as Nayaganj
        viewModel = ViewModelProvider(
            this,
            SideMenuViewModelFactory(SideMenuDataRepositry(RetrofitClient.instance))
        )[RetailerViewModel::class.java]


        binding.include13.ivBackArrow.setOnClickListener { finish() }
        binding.include13.toolbarTitle.text = "Retailer Registration Form"

        binding.btnSubmit.setOnClickListener {
            sendRetailerRequest()
        }
    }

    private fun sendRetailerRequest() {
        val companyName = binding.edtCompanyName.editText?.text.toString()
        val directorNumber = binding.edtDirectorNumber.editText?.text.toString()
        val gstNumber = binding.edtGstNumber.editText?.text.toString()
        val panCardNumber = binding.edtPancard.editText?.text.toString()
        val tinNumber = binding.edtTinNumber.editText?.text.toString()

        if (companyName == "") {
            showToast("Please Enter Company Name")
        } else if (directorNumber == "") {
            showToast("Please Enter Director Number")
        } else if (gstNumber == "") {
            showToast("Please Enter GST Number")
        } else if (panCardNumber == "") {
            showToast("Please Enter PAN Card Number")
        } else if (tinNumber == "") {
            showToast("Please Enter TIN Number")
        } else {

            val jsonObject = JsonObject()
            try {
                jsonObject.addProperty("companyName", companyName)
                jsonObject.addProperty("directorNo", directorNumber)
                jsonObject.addProperty("gstNo", gstNumber)
                jsonObject.addProperty("panCardNo", panCardNumber)
                jsonObject.addProperty("tinNo", tinNumber)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d("registrationFormJson", "" + jsonObject)
            viewModel.sendRetailerRequest(app.user.getUserDetails()?.userId, jsonObject).observe(this) {

                Log.d("registrationFormJson", "" + it)
                    if (it != null && it.status) {
                        Toast.makeText(this@RetailerActivity, it.msg, Toast.LENGTH_SHORT).show()
                    }else{
                        //Toast.makeText(this@RetailerActivity, it.msg, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this@RetailerActivity, message, Toast.LENGTH_SHORT).show()
    }


}