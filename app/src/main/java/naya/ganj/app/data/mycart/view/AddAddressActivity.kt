package naya.ganj.app.data.mycart.view

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.AddAddressViewModel
import naya.ganj.app.databinding.ActivityAddAddressBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import naya.ganj.app.utility.Utility
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import javax.xml.transform.ErrorListener
import javax.xml.transform.TransformerException
import kotlin.collections.ArrayList

class AddAddressActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddAddressBinding
    lateinit var viewModel: AddAddressViewModel
    private var addressType = ""
    private var addressId = ""
    var isUpdateAddress = false
    lateinit var app: Nayaganj
    private var SELECT_CITY: CharSequence = ""
    private var SELECT_ADDRESS = ""
    var latitude:Double=0.0
    var longitude=0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        if (app.user.getAppLanguage() == 1) {
            binding.tvPersonelDetail.text = resources.getString(R.string.personal_details_h)
            binding.textView9.text = resources.getString(R.string.address_details_h)
            binding.tvFirstName.hint = resources.getString(R.string.enter_first_name_h)
            binding.tvLastName.hint = resources.getString(R.string.enter_last_name_h)
            binding.tvMobile.hint = resources.getString(R.string.mobile_number_h)
            binding.tvHouse.hint = resources.getString(R.string.house_no_h)
            binding.tvApart.hint = resources.getString(R.string.apart_name_h)
            binding.tvStreet.hint = resources.getString(R.string.street_details_h)
            binding.tvAddress.hint = resources.getString(R.string.area_details_h)

            binding.tvPin.hint = resources.getString(R.string.pincode_h)
        }

        if (intent.extras != null) {
            isUpdateAddress = true
            if(app.user.getAppLanguage()==1){
                binding.btnAddAddress.text = resources.getString(R.string.update_address_h)
            }else{
                binding.btnAddAddress.text = "Update Address"
            }

            binding.include2.toolbarTitle.text = "Edit Address"

            binding.tvFirstName.editText?.setText(intent.getStringExtra("firstName"))
            binding.tvLastName.editText?.setText(intent.getStringExtra("lastName"))
            binding.tvMobile.editText?.setText(intent.getStringExtra("contactNumber"))
            binding.tvHouse.editText?.setText(intent.getStringExtra("houseNo"))
            binding.tvApart.editText?.setText(intent.getStringExtra("ApartName"))
            binding.tvStreet.editText?.setText(intent.getStringExtra("street"))
            binding.tvAddress.editText?.setText(intent.getStringExtra("landmark"))
            binding.tvPin.editText?.setText(intent.getStringExtra("pincode"))
            addressId = intent.getStringExtra("addressId").toString()
            addressType = intent.getStringExtra("nickName").toString()

        } else {

            if(app.user.getAppLanguage()==1){
                binding.btnAddAddress.text =resources.getString(R.string.add_address_h)
                binding.include2.toolbarTitle.text = resources.getString(R.string.add_new_address_h)
            }else{
                binding.btnAddAddress.text = "Add Address"
                binding.include2.toolbarTitle.text = "Add New Address"
            }
        }

        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        ).get(AddAddressViewModel::class.java)

        binding.include2.ivBackArrow.setOnClickListener {
            finish()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        //  City List
        val cityList = ArrayList<String>()
        cityList.add("Select City")
        for (item in app.user.getUserDetails()?.configObj?.cities!!) {
            cityList.add(item)
        }
        val adapter = ArrayAdapter(this@AddAddressActivity, R.layout.auto_list_item, cityList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.tvCity.adapter = adapter

        binding.tvCity.onItemSelectedListener = object : AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                SELECT_CITY = if (p2 == 0) {
                    ""
                } else {
                    cityList[p2]
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            }
        }

        //  City List
        val mAddressList = ArrayList<String>()
        mAddressList.add("Select Address")
        for (item in app.user.getUserDetails()?.configObj?.addressType!!) {
            mAddressList.add(item)
        }
        val addressAdapter =
            ArrayAdapter(this@AddAddressActivity, R.layout.auto_list_item, mAddressList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.tvSelectAddress.adapter = addressAdapter

        binding.tvSelectAddress.onItemSelectedListener = object : AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                SELECT_ADDRESS = if (p2 == 0) {
                    ""
                } else {
                    mAddressList[p2]
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            }
        }


        binding.btnAddAddress.setOnClickListener {
            binding.btnAddAddress.isEnabled = true
            if (binding.tvFirstName.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please Enter First Name")
            } else if (binding.tvLastName.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please Enter Last Name")
            } else if (binding.tvMobile.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please Enter Mobile Number")
            } else if (binding.tvMobile.editText?.text.toString().startsWith("0")) {
                Utility().showToast(this@AddAddressActivity, "Please enter a valid mobile number")
            } else if (binding.tvMobile.editText?.text.toString().length < 10) {
                Utility().showToast(this@AddAddressActivity, "Please enter a valid mobile number")
            } else if (binding.tvHouse.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter house number")
            } else if (binding.tvApart.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter apartment name")
            } else if (binding.tvStreet.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter street name")
            } else if (binding.tvAddress.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter area details")
            } else if (SELECT_CITY == "") {
                Utility().showToast(this@AddAddressActivity, "Please select city")
            } else if (binding.tvPin.editText?.text.toString().equals("")) {
                Utility().showToast(this@AddAddressActivity, "Please enter pincode")
            } else if (binding.tvPin.editText?.text?.startsWith("0") == true) {
                Utility().showToast(this@AddAddressActivity, "Invalid pincode")
            } else if (binding.tvPin.editText?.text.toString().length < 6) {
                Utility().showToast(this@AddAddressActivity, "Invalid pincode")
            } else if (SELECT_ADDRESS == "") {
                Utility().showToast(this@AddAddressActivity, "Please select address type")
            } else {
                var addressString=binding.tvHouse.editText?.text.toString()+" "+
                        binding.tvApart.editText?.text.toString()+" "+
                        binding.tvStreet.editText?.text.toString()+" "+
                        SELECT_CITY+","+binding.tvPin.editText?.text.toString()

                lifecycleScope.launch(Dispatchers.IO){
                    convertAddress(addressString,this@AddAddressActivity)
                    withContext(Dispatchers.Main){
                        checkPinCodeExist(binding.tvPin.editText?.text.toString())
                    }
                }
            }
        }
    }

    private fun checkPinCodeExist(pinCode: String) {

        val url = "http://www.postalpincode.in/api/pincode/$pinCode"

        val queue: RequestQueue = Volley.newRequestQueue(this@AddAddressActivity)
        val objectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {

                    if (response?.getString("Status").toString()
                            .equals("Error", ignoreCase = true)
                    ) {
                        binding.btnAddAddress.isEnabled = true
                        notValidPincode()

                    } else {
                        val postOfficeArray: JSONArray = response!!.getJSONArray("PostOffice")
                        val obj: JSONObject = postOfficeArray.getJSONObject(0)
                        val district = obj.getString("District").toString()
                        Log.e("TAG", "district: " + district)
                        binding.btnAddAddress.isEnabled = true

                        if (district.contains(SELECT_CITY)) {
                            if (Utility.isAppOnLine(this@AddAddressActivity,
                                    object : OnInternetCheckListener {
                                        override fun onInternetAvailable() {
                                            addAddressRequest()
                                        }
                                    }))
                                addAddressRequest()
                        } else {
                            notValidPincode()
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            object : ErrorListener, Response.ErrorListener {

                override fun warning(exception: TransformerException?) {}


                override fun error(exception: TransformerException?) {
                }

                override fun fatalError(exception: TransformerException?) {
                }

                override fun onErrorResponse(error: VolleyError?) {

                }
            })

        queue.add(objectRequest)
    }

    private fun addAddressRequest() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.firstName, binding.tvFirstName.editText?.text.toString())
        jsonObject.addProperty(Constant.LastName, binding.tvLastName.editText?.text.toString())
        jsonObject.addProperty(Constant.contactNumber, binding.tvMobile.editText?.text.toString())
        jsonObject.addProperty(Constant.houseNo, binding.tvHouse.editText?.text.toString())
        jsonObject.addProperty(Constant.ApartName, binding.tvApart.editText?.text.toString())
        jsonObject.addProperty(Constant.street, binding.tvStreet.editText?.text.toString())
        jsonObject.addProperty(Constant.landmark, binding.tvAddress.editText?.text.toString())
        jsonObject.addProperty(Constant.city, SELECT_CITY.toString())
        jsonObject.addProperty(Constant.pincode, binding.tvPin.editText?.text.toString())
        jsonObject.addProperty(Constant.nickName, SELECT_ADDRESS)
        jsonObject.addProperty(Constant.lat, latitude)
        jsonObject.addProperty(Constant.long, longitude)

        if (isUpdateAddress) {
            jsonObject.addProperty(Constant.addressId, addressId)

            viewModel.updateAddressRequest(app.user.getUserDetails()?.userId,jsonObject).observe(this) {
                binding.btnAddAddress.isEnabled = true
                it.let {
                    if (it.status) {
                        Utility.showToast(
                            this@AddAddressActivity,
                            "Address update successfully..."
                        )
                        finish()
                    } else {
                        Utility().showToast(this@AddAddressActivity, "Invalid Address !")
                    }
                }
            }
        } else {
            viewModel.addAddressRequest(app.user.getUserDetails()?.userId,jsonObject).observe(this) {
                binding.btnAddAddress.isEnabled = true
                it.let {
                    if (it.status) {
                        Utility().showToast(
                            this@AddAddressActivity,
                            "Address Added successfully..."
                        )
                        finish()
                    } else {
                        Utility().showToast(this@AddAddressActivity, "Invalid Address !")
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        );
    }

    fun convertAddress(address: String,context:Context) {
        Handler(Looper.getMainLooper()).post {
            val coder = Geocoder(context, Locale.getDefault())
            if (address.isNotEmpty()) {
                try {
                    val addressList: List<Address> = coder.getFromLocationName(address, 1)
                    if (addressList.isNotEmpty()) {
                        latitude = addressList[0].latitude
                        longitude = addressList[0].longitude
                        Log.e("TAG", "convertAddress: Lat:" + latitude + " Long:" + longitude)

                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun notValidPincode() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Naya Ganj")
        builder.setMessage("This pincode does not exist in this City.Please correct the pincode.")
        builder.setPositiveButton("Ok")
        { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(resources.getColor(R.color.red_color))
    }

}