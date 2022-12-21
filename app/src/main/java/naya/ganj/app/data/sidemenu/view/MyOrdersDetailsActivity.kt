package naya.ganj.app.data.sidemenu.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.data.sidemenu.adapter.ProductDetailListAdapter
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.OrderDetailViewModel
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.databinding.ActivityMyOrdersDetailsBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility


class MyOrdersDetailsActivity : AppCompatActivity() {
    lateinit var viewModel: OrderDetailViewModel
    lateinit var binding: ActivityMyOrdersDetailsBinding
    lateinit var app: Nayaganj
    lateinit var popupMenu: PopupMenu
    var orderId = ""
    var orderDetailModel: OrderDetailModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        binding.includeLayout.ivBackArrow.setOnClickListener { finish() }
        if (app.user.getAppLanguage() == 1) {
            binding.includeLayout.toolbarTitle.text = resources.getString(R.string.order_details_h)
            binding.tvAddress.text = resources.getString(R.string.address_details_h)
            binding.btnReorder.text = resources.getString(R.string.re_order_h)
        } else {
            binding.includeLayout.toolbarTitle.text = "Order Details"
        }

        viewModel = ViewModelProvider(
            this,
            SideMenuViewModelFactory(SideMenuDataRepositry(RetrofitClient.instance))
        )[OrderDetailViewModel::class.java]


        intent.extras.let {
            orderId = intent.getStringExtra(Constant.orderId).toString()
            val orderStatus = intent.getStringExtra(Constant.orderStatus)
            val totalItems = intent.getStringExtra(Constant.totalItems)

            if (Utility.isAppOnLine(this@MyOrdersDetailsActivity, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        getMyOrderDetailRequest(orderId, totalItems)
                    }
                }))
                getMyOrderDetailRequest(orderId, totalItems)
        }

        popupMenu = PopupMenu(this, binding.includeLayout.cancelOrder, R.style.MyPopupMenu)
        popupMenu.menuInflater.inflate(R.menu.cancel_order_menu, popupMenu.menu)
        //popupMenu.gravity = Gravity.END


        binding.includeLayout.cancelOrder.setOnClickListener {
            Log.e("TAG", "onCreate: Showing pop up menu")
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.cancel_order -> {
                        changeOrderStatusApi(app.user.getUserDetails()?.userId, orderId)
                    }
                    R.id.return_product -> {

                        val gson = Gson() // Or use new GsonBuilder().create();
                        val orderModel = gson.toJson(orderDetailModel)

                        val intent = Intent(this, ReturnProductActivity::class.java)
                        intent.putExtra("productlist",orderModel )
                        intent.putExtra(Constant.orderId, orderId)
                        startActivity(intent)
                    }
                }
                true
            }
            popupMenu.show()

        }

    }

    private fun changeOrderStatusApi(userId: String?, orderId: String) {
        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty(Constant.orderId, orderId)
            jsonObject.addProperty(Constant.orderStatus, "CANCELLED")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewModel.changeOrderStatusRequest(userId!!, jsonObject).observe(this) {
            if (it != null) {
                Toast.makeText(
                    this@MyOrdersDetailsActivity,
                    "Order cancelled Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getMyOrderDetailRequest(orderId: String?, totalItems: String?) {

        if (app.user.getLoginSession()) {
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.orderId, orderId)
            viewModel.getOrderDetailRequest(app.user.getUserDetails()!!.userId, jsonObject)
                .observe(this) {
                    if (it != null) {
                        orderDetailModel=it
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

                    // Set Menu Option
                        try
                        {
                            //Reorder -- Cancel Order  ---Return Product
                            val reOrder = it.orderDetails.buttonIndex.split('|')[0]
                            val cancelOrder = it.orderDetails.buttonIndex.split('|')[1]
                            val returnOrder = it.orderDetails.buttonIndex.split('|')[2]

                            if (reOrder.toInt() == 1) {
                                binding.btnReorder.visibility = View.VISIBLE
                            }
                            if (cancelOrder.toInt() == 1) {
                                binding.includeLayout.cancelOrder.visibility = View.VISIBLE
                                popupMenu.menu.findItem(R.id.cancel_order).isVisible = true
                            }
                            if (returnOrder.toInt() == 1) {
                                binding.includeLayout.cancelOrder.visibility = View.VISIBLE
                                popupMenu.menu.findItem(R.id.return_product).isVisible = true
                            }

                        } catch (e: Exception)
                        {
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