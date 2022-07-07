package naya.ganj.app.data.mycart.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import naya.ganj.app.data.mycart.adapter.AddressListAdapter
import naya.ganj.app.data.mycart.model.AddressListModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.AddressListViewModel
import naya.ganj.app.databinding.ActivityAddressListBinding
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import com.google.gson.JsonObject

class AddressListActivity : AppCompatActivity(), OnitemClickListener {
    lateinit var binding: ActivityAddressListBinding
    lateinit var viewModel: AddressListViewModel
    lateinit var addresListAdapter: AddressListAdapter
    lateinit var addressList: MutableList<AddressListModel.Address>
    var addressId = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAddressListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.include.ivBackArrow.setOnClickListener { finish() }
        binding.include.toolbarTitle.setText("Address List")

        if (intent.extras != null) {
            addressId = intent.getStringExtra("ADDRESS_ID").toString()
        }

        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        ).get(AddressListViewModel::class.java)
        binding.btnAddAddress.setOnClickListener {
            startActivity(Intent(this, AddAddressActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.getAddressList().observe(this) {
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
    }
}