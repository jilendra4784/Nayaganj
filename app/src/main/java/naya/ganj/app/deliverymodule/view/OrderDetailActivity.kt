package naya.ganj.app.deliverymodule.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.databinding.ActivityOrderDetailBinding
import naya.ganj.app.deliverymodule.adapter.DeliveredOrderDetailAdapter
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleFactory
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility

class OrderDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityOrderDetailBinding
    lateinit var viewModel: DeliveryModuleViewModel
    lateinit var app: Nayaganj
    var orderType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(
            this@OrderDetailActivity,
            DeliveryModuleFactory(DeliveryModuleRepositry(RetrofitClient.instance))
        )[DeliveryModuleViewModel::class.java]
        app = applicationContext as Nayaganj

        setContentView(binding.root)

        val orderId = intent.getStringExtra(Constant.ORDER_ID)
        val type = intent.getStringExtra(Constant.Type)

        orderType = if (type.equals("delivery")) {
            "deliveryOrder"
        } else {
            "returnOrder"
        }

        binding.rvProductList.layoutManager = LinearLayoutManager(this@OrderDetailActivity)
        binding.rvProductList.isNestedScrollingEnabled = false
        getOrderDetail(orderId, orderType)
    }

    private fun getOrderDetail(orderId: String?, type: String?) {

        if (Utility().isAppOnLine(this@OrderDetailActivity)) {
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.orderId, orderId)
            jsonObject.addProperty(Constant.Type, orderType)

            viewModel.getOrderDetail(app.user.getUserDetails()?.userId, jsonObject)
                .observe(this@OrderDetailActivity) {

                    if (it != null) {
                        if (it.orderDetails.products.isNotEmpty()) {
                            binding.rvProductList.adapter =
                                DeliveredOrderDetailAdapter(it.orderDetails.products)
                        }
                    }
                }
        }

    }
}