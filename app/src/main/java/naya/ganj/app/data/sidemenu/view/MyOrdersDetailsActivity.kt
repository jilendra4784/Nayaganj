package naya.ganj.app.data.sidemenu.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import naya.ganj.app.R
import naya.ganj.app.data.sidemenu.adapter.ProductDetailListAdapter
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.OrderDetailViewModel
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.databinding.ActivityMyOrdersDetailsBinding
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.utility.Utility

class MyOrdersDetailsActivity : AppCompatActivity() {
    lateinit var viewModel: OrderDetailViewModel
    lateinit var binding: ActivityMyOrdersDetailsBinding
    lateinit var app:Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app=applicationContext as Nayaganj

        binding.includeLayout.ivBackArrow.setOnClickListener { finish() }
        if(app.user.getAppLanguage()==1){
            binding.includeLayout.toolbarTitle.text=resources.getString(R.string.order_details_h)
            binding.tvAddress.text=resources.getString(R.string.address_details_h)
            binding.btnReorder.text=resources.getString(R.string.re_order_h)
        }else{
            binding.includeLayout.toolbarTitle.text = "Order Details"
        }

        viewModel = ViewModelProvider(
            this,
            SideMenuViewModelFactory(SideMenuDataRepositry(RetrofitClient.instance))
        )[OrderDetailViewModel::class.java]


        intent.extras.let {
            val orderId = intent.getStringExtra(Constant.orderId)
            val orderStatus = intent.getStringExtra(Constant.orderStatus)
            val totalItems = intent.getStringExtra(Constant.totalItems)

            if(Utility.isAppOnLine(this@MyOrdersDetailsActivity,object : OnInternetCheckListener{
                    override fun onInternetAvailable() {
                        getMyOrderDetailRequest(orderId, totalItems)
                    }
                }))
            getMyOrderDetailRequest(orderId, totalItems)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getMyOrderDetailRequest(orderId: String?, totalItems: String?) {

        if(app.user.getLoginSession()){
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.orderId, orderId)
            viewModel.getOrderDetailRequest(app.user.getUserDetails()!!.userId,jsonObject).observe(this) {
                if (it != null) {
                    binding.tvOrderId.text = it.orderDetails.id
                    binding.tvOrderOn.text = it.orderDetails.created
                    binding.tvPaymentStatus.text = it.orderDetails.paymentStatus
                    binding.tvPaymentMode.text = it.orderDetails.paymentMode
                    binding.tvTotalItems.text = totalItems

                    binding.tvAmount.text =
                        resources.getString(R.string.Rs) + it.orderDetails.itemTotal
                    if (it.orderDetails.deliverCharges > 0) {
                        binding.tvDeliveryCharges.text = "+" + " " +
                                resources.getString(R.string.Rs) + it.orderDetails.deliverCharges.toString()
                        binding.tvTotalAmount.text = resources.getString(R.string.Rs) +
                                (it.orderDetails.totalAmount + it.orderDetails.deliverCharges).toString()
                    } else {
                        binding.tvDeliveryCharges.text = "Free"
                        binding.tvTotalAmount.text =
                            resources.getString(R.string.Rs) + it.orderDetails.totalAmount.toString()
                    }

                    val address: OrderDetailModel.OrderDetails.Address = it.orderDetails.address
                    binding.tvAddressDetail.text =
                        address.houseNo + address.apartName + address.landmark + address.city + address.pincode

                    val orderStatus = it.orderDetails.orderStatus.split("|")
                    try {
                        if (orderStatus[0] == "Pending") {
                            binding.tvOrderStatus.text = orderStatus[0]
                            binding.tvOrderStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@MyOrdersDetailsActivity,
                                    R.color.white
                                )
                            )
                            binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))

                        } else if (orderStatus[0].equals("Failed") || orderStatus[0].equals("Cancelled")) {
                            binding.tvOrderStatus.text = orderStatus[0]
                            binding.tvOrderStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@MyOrdersDetailsActivity,
                                    R.color.white
                                )
                            )
                            binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))
                        } else if (orderStatus[0].equals("Delivered")) {
                            binding.tvOrderStatus.text = orderStatus[0]
                            binding.tvOrderStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@MyOrdersDetailsActivity,
                                    R.color.white
                                )
                            )
                            binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))

                        } else {
                            binding.tvOrderStatus.text = orderStatus[0]
                            binding.tvOrderStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@MyOrdersDetailsActivity,
                                    R.color.white
                                )
                            )
                            binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // Set List Data
                    binding.rvItemList.layoutManager = LinearLayoutManager(this@MyOrdersDetailsActivity)
                    binding.rvItemList.adapter = ProductDetailListAdapter(this@MyOrdersDetailsActivity
                            ,it.orderDetails.products,app)

                }
            }
        }



        binding.btnReorder.setOnClickListener {
            val intent = Intent(this@MyOrdersDetailsActivity,MyCartActivity::class.java)
            intent.putExtra(Constant.orderId, binding.tvOrderId.text.toString())
            startActivity(intent)
            finish()
        }
    }
}