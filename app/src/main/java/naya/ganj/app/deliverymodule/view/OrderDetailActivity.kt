package naya.ganj.app.deliverymodule.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class OrderDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityOrderDetailBinding
    lateinit var viewModel: DeliveryModuleViewModel
    lateinit var app: Nayaganj
    private var orderType = ""
    private var FragmentType = ""
    private var changeOrderStatus = ""
    lateinit var refundJsonArray: JSONArray
    private var orderId = ""
    private var lat=""
    private var long=""
    private var paymentMode=""
    private var orderStatus=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        binding.include16.ivBackArrow.setOnClickListener { finish() }
        binding.include16.tvToolbarTitle.text = "Orders Detail"
        refundJsonArray = JSONArray()

        viewModel = ViewModelProvider(
            this@OrderDetailActivity,
            DeliveryModuleFactory(DeliveryModuleRepositry(RetrofitClient.instance))
        )[DeliveryModuleViewModel::class.java]
        app = applicationContext as Nayaganj

        setContentView(binding.root)

        orderId = intent.getStringExtra(Constant.ORDER_ID).toString()
        val type = intent.getStringExtra(Constant.Type)
        FragmentType = intent.getStringExtra(Constant.FragmetType).toString()

        orderType = if (type.equals("delivery")) {
            "deliveryOrder"
        } else {
            "returnOrder"
        }

        binding.ivLocation.setOnClickListener{

            val intent=Intent(this,TrackLocationByDeliveryBoy::class.java)
            intent.putExtra("name",binding.tvName.text.toString())
            intent.putExtra("address",binding.tvAddress.text.toString())
            intent.putExtra("mobileNumber",binding.tvContactNumber.text.toString())
            intent.putExtra(Constant.orderId,orderId)
            intent.putExtra(Constant.lat,lat)
            intent.putExtra(Constant.long,long)
            intent.putExtra(Constant.orderStatus,orderStatus)
            intent.putExtra("paymentMode",paymentMode)
            startActivity(intent)

        }

        getOrderDetail(orderId, orderType)

    }

    private fun getOrderDetail(orderId: String?, type: String?) {
        if (Utility.isAppOnLine(this@OrderDetailActivity)) {
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

            if (orderType == "deliveryOrder") {
                binding.tvRefundAmount.visibility = View.GONE
            } else {
                if (it.orderDetails.refundedAmount > 0) {
                    binding.tvRefundAmount.visibility = View.VISIBLE
                } else {
                    binding.tvRefundAmount.visibility = View.GONE
                }
            }
            if (FragmentType == "ORDERS") {
                binding.tvPaymentReceived.visibility = View.GONE
                binding.tvRefundAmount.visibility = View.GONE
                binding.cvCardview.visibility = View.GONE
                binding.ivPhone.visibility = View.VISIBLE
                binding.tvContactNumber.visibility = View.VISIBLE
                binding.tvContactNumber.text = it.orderDetails.address.contactNumber
                binding.ivLocation.visibility = View.VISIBLE
                if(orderType == "returnOrder"){
                    binding.tvRefundAmount.visibility=View.VISIBLE
                }


                changeOrderStatus = ""

                // OrderStatus button status
                try {
                    val orderStatusValue = it.orderDetails.buttonIndex.split('|')[1]
                    val orderStatusValueAfterSplit = orderStatusValue.split(',')

                    if (orderStatusValueAfterSplit[3].toInt() == 1) {
                        binding.statusButton.visibility = View.VISIBLE
                        binding.statusButton.text = orderStatusValueAfterSplit[1]
                        changeOrderStatus = orderStatusValueAfterSplit[0]
                        binding.statusButton.setBackgroundColor(
                            Color.parseColor(
                                orderStatusValueAfterSplit[2]
                            )
                        )
                    } else {
                        binding.statusButton.visibility = View.GONE
                    }

                    // For ReSchedule Order
                    val rescheduleStatus = it.orderDetails.buttonIndex.split('|')[0]
                    val rescheduleStatusAfterSplit = rescheduleStatus.split(',')

                    if (rescheduleStatusAfterSplit[3].toInt() == 1) {
                        binding.reschedule.visibility = View.VISIBLE
                        binding.reschedule.setBackgroundColor(
                            Color.parseColor(
                                rescheduleStatusAfterSplit[2]
                            )
                        )
                        binding.reschedule.setText(rescheduleStatusAfterSplit[1])
                    } else {
                        binding.reschedule.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Refund Status
                try {
                    val refundValue = it.orderDetails.buttonIndex.split('|')[2]
                    val refundAfterSplit = refundValue.split(',')

                    if (refundAfterSplit[3].toInt() == 1) {
                        binding.statusButton.visibility = View.VISIBLE
                        binding.statusButton.text = refundAfterSplit[1]
                        binding.statusButton.setBackgroundColor(Color.parseColor(refundAfterSplit[2]))
                    } else {
                        binding.statusButton.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else if (FragmentType == "HISTORY") {
                binding.tvPaymentReceived.visibility = View.VISIBLE
                binding.tvRefundAmount.visibility = View.VISIBLE
                binding.cvCardview.visibility = View.VISIBLE
            }
            binding.rvProductList.layoutManager = LinearLayoutManager(this@OrderDetailActivity)
            binding.rvProductList.adapter = DeliveredOrderDetailAdapter(it.orderDetails.products)
            binding.statusButton.setOnClickListener {

                val builder = AlertDialog.Builder(this@OrderDetailActivity)
                builder.setIcon(ContextCompat.getDrawable(this, R.drawable.my_order))

                if (changeOrderStatus.equals(
                        Constant.RETURNCOLLECTED,
                        ignoreCase = true
                    ) || changeOrderStatus.equals(
                        Constant.RETURNCOLLECTEDORRETURNPARTIALCOLLECTED,
                        true
                    )
                ) {
                    builder.setTitle("Collect Product")
                    builder.setMessage("Have you reached customer location & ready to collect product.")
                    builder.setPositiveButton("Collect")
                    { dialogInterface, which ->
                        returnproductApi(app.user.getUserDetails()?.userId, changeOrderStatus)
                    }

                    // builder.setNegativeButton("Modify") { dialogInterface, which ->showProductBottomSheetDialog() }
                    builder.setNeutralButton("No") { dialogInterface, which -> }

                    /*  if (totalProductQty > 1) {
                          builder.setNegativeButton("Modify") { dialogInterface, which -> showProductBottomSheetDialog() }
                      }*/

                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(true)
                    alertDialog.show()
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.red_color))
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.dark_gray))
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                        .setTextColor(resources.getColor(R.color.gray))


                } else if (changeOrderStatus.equals(
                        Constant.RETURNSUCCESS,
                        ignoreCase = true
                    ) || changeOrderStatus.equals(Constant.RETURNPARTIALSUCCESS, true)
                ) {
                    builder.setTitle("Return Product")
                    builder.setMessage("Have you collected product from the customer & ready to return product.")
                    builder.setPositiveButton("Yes")
                    { dialogInterface, which ->

                        returnproductApi(app.user.getUserDetails()?.userId, changeOrderStatus)
                        // showProductBottomSheetDialog()
                    }

                    builder.setNegativeButton("No") { dialogInterface, which -> }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(true)
                    alertDialog.show()
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.gray))
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.dark_gray))
                } else {
                    if (Utility.isAppOnLine(this@OrderDetailActivity)) {
                        refundApi(app.user.getUserDetails()?.userId)
                    }
                }
            }

            for (item in it.orderDetails.products) {
                val postedJSON = JSONObject()
                postedJSON.put(Constant.productId, item.productId)
                postedJSON.put(Constant.variantId, item.variantId)
                postedJSON.put(Constant.quantity, item.quantity)
                refundJsonArray.put(postedJSON)
            }

            try {
                if(it.orderDetails.loc.coordinates.isNotEmpty())
                {
                    lat= it.orderDetails.loc.coordinates[1].toString()
                    long= it.orderDetails.loc.coordinates[0].toString()
                }
            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }

            orderStatus=it.orderDetails.orderStatus
        }
    }

    private fun returnproductApi(userId: String?, changeOrderStatus: String) {
        if (Utility.isAppOnLine(this@OrderDetailActivity)) {
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.orderId, orderId)
            jsonObject.addProperty(Constant.orderStatus, changeOrderStatus)

            viewModel.returnProducApiRequest(userId, jsonObject).observe(this) {
                if (it.status) {
                    if (changeOrderStatus.equals(
                            Constant.RETURNCOLLECTED,
                            ignoreCase = true
                        ) || changeOrderStatus.equals(
                            Constant.RETURNCOLLECTEDORRETURNPARTIALCOLLECTED,
                            ignoreCase = true
                        )
                    ) {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            "You have collected product from the customer successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(
                            this@OrderDetailActivity,
                            DeliveryBoyDashboardActivity::class.java
                        )
                        startActivity(intent)
                        finish()
                    } else if (changeOrderStatus.equals(
                            Constant.RETURNSUCCESS,
                            ignoreCase = true
                        ) || changeOrderStatus.equals(
                            Constant.RETURNPARTIALSUCCESS,
                            ignoreCase = true
                        )
                    ) {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            "You have returned product successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(
                            this@OrderDetailActivity,
                            DeliveryBoyDashboardActivity::class.java
                        )
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@OrderDetailActivity, it.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refundApi(userId: String?) {

        if (Utility.isAppOnLine(this@OrderDetailActivity)) {
            val jsonObject = JSONObject()
            jsonObject.put(Constant.orderId, orderId)
            jsonObject.put(Constant.productsArr, refundJsonArray)

            val myreqbody = JSONObject(jsonObject.toString()).toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            viewModel.refundRequest(userId, myreqbody).observe(this@OrderDetailActivity) {
                if (it.status) {
                    Toast.makeText(
                        this@OrderDetailActivity,
                        "You have refunded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent =
                        Intent(this@OrderDetailActivity, DeliveryBoyDashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

}