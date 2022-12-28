package naya.ganj.app.deliverymodule.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.ActivityOrderDetailBinding
import naya.ganj.app.deliverymodule.adapter.DeliveredOrderDetailAdapter
import naya.ganj.app.deliverymodule.adapter.ReturnProductModifyAdapter
import naya.ganj.app.deliverymodule.model.DeliveryOrderDetailModel
import naya.ganj.app.deliverymodule.model.Product
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleFactory
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.ProductSelectListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ReturnProduct
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class OrderDetailActivity : AppCompatActivity(), ProductSelectListener {

    lateinit var binding: ActivityOrderDetailBinding
    lateinit var viewModel: DeliveryModuleViewModel
    lateinit var app: Nayaganj
    private var orderType = ""
    private var FragmentType = ""
    private var changeOrderStatus = ""
    lateinit var refundJsonArray: JSONArray
    private var orderId = ""
    private var lat = ""
    private var long = ""
    private var paymentMode = ""
    private var orderStatus = ""
    var totalProductQty = 0
    private var productList: List<Product>? = ArrayList()
    var totalReturnItems: TextView? = null


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
            intent.putExtra("address", binding.tvAddress.text.toString())
            intent.putExtra("mobileNumber", binding.tvContactNumber.text.toString())
            intent.putExtra(Constant.orderId, orderId)
            intent.putExtra(Constant.lat, lat)
            intent.putExtra(Constant.long, long)
            intent.putExtra(Constant.orderStatus, orderStatus)
            intent.putExtra("paymentMode", paymentMode)
            startActivity(intent)

        }

        binding.reschedule.setOnClickListener {
            sendReschduleApi(orderId, orderType)
        }

        if (Utility.isAppOnLine(this@OrderDetailActivity, object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getOrderDetail(orderId, orderType)
                }
            }))
            getOrderDetail(orderId, orderType)

    }

    private fun sendReschduleApi(orderId: String, orderType: String) {

        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty(Constant.orderId, orderId)
            jsonObject.addProperty(Constant.type, Constant.Return)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModel.sendReschduleRequest(app.user.getUserDetails()?.userId,jsonObject).observe(this){
            if(it!=null &&it.status){
                Toast.makeText(this@OrderDetailActivity, resources?.getString(R.string.reschedule), Toast.LENGTH_SHORT).show()
                getOrderDetail(orderId,orderType)
            }else{
                Toast.makeText(this@OrderDetailActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun getOrderDetail(orderId: String?, type: String?) {

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


    private fun setUIData(it: DeliveryOrderDetailModel) {
        if (it.orderDetails.products.isNotEmpty()) {
            productList = it.orderDetails.products
            binding.tvName.text =
                it.orderDetails.address.firstName + " " + it.orderDetails.address.lastName
            binding.tvAddressType.text = it.orderDetails.address.nickName
            binding.tvAddress.text =
                it.orderDetails.address.houseNo + " " + it.orderDetails.address.apartName + " " + it.orderDetails.address.landmark + " " + it.orderDetails.address.city + "-" + it.orderDetails.address.pincode

            binding.tvOrderId.text = it.orderDetails.id
            binding.tvOrderOn.text = it.orderDetails.created
            binding.tvPaymentStatus.text = it.orderDetails.paymentStatus
            binding.tvPaymentMode.text =
                if (it.orderDetails.paymentMode == "COD") "Cash On Delivery" else it.orderDetails.paymentMode
            paymentMode = it.orderDetails.paymentMode
            binding.tvTotalItems.text = it.orderDetails.products.size.toString()
            binding.tvTotalItemAmount.text =
                resources.getString(R.string.Rs) + it.orderDetails.itemTotal.toString()
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
                        binding.statusCardview.visibility = View.VISIBLE
                        binding.statusButton.text = orderStatusValueAfterSplit[1]
                        changeOrderStatus = orderStatusValueAfterSplit[0]
                        binding.statusButton.setBackgroundColor(Color.parseColor(orderStatusValueAfterSplit[2]))
                    } else {
                        binding.statusCardview.visibility = View.GONE
                    }

                    // For ReSchedule Order
                    val rescheduleStatus = it.orderDetails.buttonIndex.split('|')[0]
                    val rescheduleStatusAfterSplit = rescheduleStatus.split(',')

                    if (rescheduleStatusAfterSplit[3].toInt() == 1) {
                        binding.rescheduleCardview.visibility = View.VISIBLE
                        binding.reschedule.setBackgroundColor(Color.parseColor(rescheduleStatusAfterSplit[2]))
                        binding.reschedule.setText(rescheduleStatusAfterSplit[1])
                    } else {

                        binding.rescheduleCardview.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Refund Status
                try {

                    val refundValue = it.orderDetails.buttonIndex.split('|')[2]
                    val refundAfterSplit = refundValue.split(',')

                    if (refundAfterSplit[3].toInt() == 1) {
                        binding.statusCardview.visibility = View.VISIBLE
                        binding.statusButton.text = refundAfterSplit[1]
                        binding.statusButton.setBackgroundColor(Color.parseColor(refundAfterSplit[2]))
                    } else {
                        binding.statusButton.visibility = View.GONE
                        binding.statusCardview.visibility = View.GONE
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
            binding.rvProductList.adapter = DeliveredOrderDetailAdapter(app, productList!!)


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
                        if(Utility.isAppOnLine(this@OrderDetailActivity,object : OnInternetCheckListener {
                                override fun onInternetAvailable() {
                                    returnproductApi(
                                        app.user.getUserDetails()?.userId,
                                        changeOrderStatus
                                    )
                                }
                            }))

                            returnproductApi(app.user.getUserDetails()?.userId, changeOrderStatus)
                    }

                    builder.setNeutralButton("No") { dialogInterface, which -> }

                    if (totalProductQty > 1) {
                        builder.setNegativeButton("Modify") { dialogInterface, which -> showProductBottomSheetDialog() }
                    }

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
                    if (Utility.isAppOnLine(this@OrderDetailActivity,object : OnInternetCheckListener {
                            override fun onInternetAvailable() {
                                refundApi(app.user.getUserDetails()?.userId)
                            }
                        })) {
                        refundApi(app.user.getUserDetails()?.userId)
                    }
                }
            }

            for (item in it.orderDetails.products) {
                totalProductQty += item.quantity
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
        }else{

        }

    }

    private fun returnproductApi(userId: String?, changeOrderStatus: String) {

            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.orderId, orderId)
            jsonObject.addProperty(Constant.orderStatus, changeOrderStatus)

            viewModel.returnProducApiRequest(userId, jsonObject).observe(this) {
                if (it.status) {
                    if (changeOrderStatus.equals(Constant.RETURNCOLLECTED, ignoreCase = true) || changeOrderStatus.equals(Constant.RETURNCOLLECTEDORRETURNPARTIALCOLLECTED, ignoreCase = true)
                    ) {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            "You have collected product from the customer successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        /*val intent = Intent(
                            this@OrderDetailActivity,
                            DeliveryBoyDashboardActivity::class.java
                        )
                        startActivity(intent)
                        finish()*/
                        startActivity(intent);
                        finish();
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
                        /*val intent = Intent(
                            this@OrderDetailActivity,
                            DeliveryBoyDashboardActivity::class.java
                        )
                        startActivity(intent)
                        finish()*/
                        startActivity(getIntent());
                        finish();
                    }
                } else {
                    Toast.makeText(this@OrderDetailActivity, it.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }


    private fun refundApi(userId: String?) {

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

    private fun showProductBottomSheetDialog() {

        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.return_product_deliveryboy_bottom_sheet_dialog)
        val dismiss_bottom =
            bottomSheetDialog.findViewById<ImageView>(R.id.dismiss_bottom) as ImageView
        totalReturnItems =
            bottomSheetDialog.findViewById<TextView>(R.id.total_return_item) as TextView
        val submit = bottomSheetDialog.findViewById<TextView>(R.id.place_order_button) as TextView

        val address_recycleview =
            bottomSheetDialog.findViewById<RecyclerView>(R.id.address_recycleview)!!
        address_recycleview.layoutManager = LinearLayoutManager(this@OrderDetailActivity)

        val adapter = ReturnProductModifyAdapter(this@OrderDetailActivity, app, this, productList)
        address_recycleview.adapter = adapter

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dismiss_bottom.setOnClickListener {
            bottomSheetDialog.dismiss()
            lifecycleScope.launch(Dispatchers.IO) {
                AppDataBase.getInstance(this@OrderDetailActivity).productDao().deleteAllReturnItem()
            }
        }
        bottomSheetDialog.show()

        submit.setOnClickListener {
            if (refundJsonArray.length() > 0) {
                modifyReturnProductApi()
            } else {
                Toast.makeText(this, "Please modify the product quantity", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onSelectProduct(productId: String, variantId: String, itemQuantity: Int) {
        var itemCount=0
        lifecycleScope.launch(Dispatchers.IO) {
            val isProductExist = AppDataBase.getInstance(this@OrderDetailActivity).productDao()
                .isReturnExist(productId, variantId)
            if (!isProductExist) {
                val returnProduct = ReturnProduct(productId, variantId, itemQuantity)
                AppDataBase.getInstance(this@OrderDetailActivity).productDao()
                    .insertReturnProduct(returnProduct)
            } else {
                AppDataBase.getInstance(this@OrderDetailActivity).productDao()
                    .updateReturnProduct(productId, variantId, itemQuantity)
            }


        }

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            lifecycleScope.launch(Dispatchers.IO) {
                val list: List<ReturnProduct> =
                    AppDataBase.getInstance(this@OrderDetailActivity).productDao()
                        .getReturnProductList()
                Log.e("TAG", "onSelectProduct: List Size: "+list.size )
                withContext(Dispatchers.Main) {
                    if (list.isNotEmpty()) {
                        for(item in list){
                            itemCount+=item.itemQuantity
                        }
                        totalReturnItems?.text=itemCount.toString()

                        if (itemCount==1) {
                            totalReturnItems?.text = "$itemCount Item"
                        } else {
                            totalReturnItems?.text = "$itemCount Items"
                        }
                        totalReturnItems?.visibility = View.VISIBLE
                    } else {
                        totalReturnItems?.visibility = View.GONE
                    }
                }
            }
        },500)

    }

    private fun modifyReturnProductApi() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<ReturnProduct> =
                AppDataBase.getInstance(this@OrderDetailActivity).productDao()
                    .getReturnProductList()

            val jsonArray = JSONArray()
            for (item in list) {
                val itemsData = JSONObject()
                itemsData.put(Constant.PRODUCT_ID, item.productId)
                itemsData.put(Constant.VARIANT_ID, item.variantId)
                itemsData.put(Constant.QUANTITY, item.itemQuantity)
                jsonArray.put(itemsData)
            }

            val obj = JSONObject()
            obj.put(Constant.productsArr, jsonArray)
            obj.put(Constant.orderId, orderId)

            val myreqbody = JSONObject(obj.toString()).toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            withContext(Dispatchers.Main) {
                viewModel.modifyReturnProductApi(app.user.getUserDetails()?.userId, myreqbody)
                    .observe(this@OrderDetailActivity) {
                        if (it != null) {
                            if (it.status) {
                                Toast.makeText(
                                    this@OrderDetailActivity,
                                    "Order Modify Successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                remoAllModifiedProduct()
                                finish()

                            } else {
                                Toast.makeText(this@OrderDetailActivity, it.msg, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        remoAllModifiedProduct()
    }

    fun remoAllModifiedProduct(){
        lifecycleScope.launch(Dispatchers.IO) {
            AppDataBase.getInstance(this@OrderDetailActivity).productDao().deleteAllReturnItem()
        }
    }

}