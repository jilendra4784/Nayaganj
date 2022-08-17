package naya.ganj.app.deliverymodule.view

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.ActivityOrderDetailBinding
import naya.ganj.app.deliverymodule.adapter.DeliveredOrderDetailAdapter
import naya.ganj.app.deliverymodule.model.DeliveryOrderDetailModel
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
                        setUIData(it)
                    }
                }
        }
    }

    private fun setUIData(it: DeliveryOrderDetailModel) {
        if (it.orderDetails.products.isNotEmpty()) {
            binding.tvName.text = it.orderDetails.address.firstName +" "+ it.orderDetails.address.lastName
            binding.tvAddressType.text=it.orderDetails.address.nickName
            binding.tvAddress.text=it.orderDetails.address.houseNo+" "+it.orderDetails.address.apartName+" "+it.orderDetails.address.landmark+" "+it.orderDetails.address.city+"-"+it.orderDetails.address.pincode

            binding.tvOrderId.text=it.orderDetails.id
            binding.tvOrderOn.text=it.orderDetails.created
            binding.tvPaymentStatus.text=it.orderDetails.paymentStatus
            binding.tvPaymentMode.text=if(it.orderDetails.paymentMode == "COD") "Cash On Delivery" else it.orderDetails.paymentMode
            binding.tvTotalItems.text=it.orderDetails.products.size.toString()
            binding.tvTotalItemAmount.text=resources.getString(R.string.Rs)+ it.orderDetails.itemTotal.toString()
            binding.tvDeliveryCharges.text=if(it.orderDetails.deliverCharges<=0) "Free" else resources.getString(R.string.Rs)+it.orderDetails.deliverCharges.toString()
            binding.tvTotalAmount.text=resources.getString(R.string.Rs)+it.orderDetails.totalAmount.toString()
            binding.tvPaymentReceived.text=resources.getString(R.string.payment_received)+" "+resources.getString(R.string.Rs)+it.orderDetails.paymentRecieveByDelBoy.toString()
            binding.tvRefundAmount.text=resources.getString(R.string.refunded_amount)+" "+resources.getString(R.string.Rs)+it.orderDetails.refundedAmount.toString()

            binding.tvOrderStatus.setTextColor(ContextCompat.getColor(this@OrderDetailActivity, R.color.white))
            binding.tvOrderStatus.setBackgroundColor(Color.parseColor("#20BA44"))
            binding.tvOrderStatus.text=it.orderDetails.orderStatus

            if(orderType == "deliveryOrder"){
                binding.tvRefundAmount.visibility= View.GONE
            }else{
                if( it.orderDetails.refundedAmount>0){
                    binding.tvRefundAmount.visibility= View.VISIBLE
                }else {
                    binding.tvRefundAmount.visibility = View.GONE
                }
            }

            binding.rvProductList.layoutManager=LinearLayoutManager(this@OrderDetailActivity)
            binding.rvProductList.adapter = DeliveredOrderDetailAdapter(it.orderDetails.products)
        }
    }
}