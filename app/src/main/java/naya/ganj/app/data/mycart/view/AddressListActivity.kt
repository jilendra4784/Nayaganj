package naya.ganj.app.data.mycart.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.adapter.AddressListAdapter
import naya.ganj.app.data.mycart.model.AddressListModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.AddressListViewModel
import naya.ganj.app.databinding.ActivityAddressListBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import naya.ganj.app.utility.Utility

class AddressListActivity : AppCompatActivity(), OnitemClickListener {
    lateinit var binding: ActivityAddressListBinding
    lateinit var viewModel: AddressListViewModel
    lateinit var addresListAdapter: AddressListAdapter
    lateinit var addressList: MutableList<AddressListModel.Address>
    var addressId = ""
    lateinit var app: Nayaganj


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAddressListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        binding.include.ivBackArrow.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);}

        if(app.user.getAppLanguage()==1){
            binding.include.toolbarTitle.text = resources.getString(R.string.address_list_h)
        }else{
            binding.include.toolbarTitle.text = "Address List"
        }


        if (intent.extras != null) {
            addressId = intent.getStringExtra("ADDRESS_ID").toString()
        }

        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        )[AddressListViewModel::class.java]

        if(app.user.getAppLanguage()==1){
            binding.btnAddAddress.text=resources.getString(R.string.add_new_address_h)
        }

        binding.btnAddAddress.setOnClickListener {
            startActivity(Intent(this, AddAddressActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }
    }

    override fun onResume() {
        super.onResume()
        if(Utility.isAppOnLine(this@AddressListActivity,object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getAddressListRequestData()
                }
            }))
        getAddressListRequestData()

    }

    override fun onclick(position: Int, data: String) {
        // Delete Address Request

        val result = data.split("@")
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.addressId, result[0])
        if (result[1] == "DELETE") {
            viewModel.deleteAddressRequest(jsonObject).observe(this) {}
        } else {
            viewModel.setAddress(jsonObject).observe(this) {
                finish()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(
            Runnable { addresListAdapter.notifyDataSetChanged() },
            200
        )
    }

    private fun getAddressListRequestData(){
        viewModel.getAddressList(app.user.getUserDetails()?.userId).observe(this) {
            it.let {
                addressList = it.addressList
                addresListAdapter = AddressListAdapter(
                    it.addressList,
                    this@AddressListActivity,
                    this@AddressListActivity,
                    addressId
                )
                binding.rvAddressList.layoutManager = LinearLayoutManager(this)
                binding.rvAddressList.adapter = addresListAdapter
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left,
            R.anim.slide_out_right);
    }
}