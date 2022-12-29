package naya.ganj.app.data.mycart.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.mycart.adapter.MyCartAdapter
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.mycart.model.UpdatedCart
import naya.ganj.app.data.mycart.viewmodel.MyCartViewModel
import naya.ganj.app.databinding.ActivityMyCartBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.ADDRESS_RADIO_SELECTION
import naya.ganj.app.utility.Constant.CHANGE_ADDRESS_VALUE
import naya.ganj.app.utility.Constant.IS_FROM_MYCART
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyCartActivity : AppCompatActivity(), OnclickAddOremoveItemListener,
    MyCartAdapter.RemoveProduct {
    lateinit var app: Nayaganj
    lateinit var myCartViewModel: MyCartViewModel
    lateinit var binding: ActivityMyCartBinding
    lateinit var myCartModel: MyCartModel
    private var addressId: String? = null
    var orderId: String? = null
    var couponList: ArrayList<UpdatedCart> = ArrayList()
    var cartList: MutableList<MyCartModel.Cart>? = null
    lateinit var myCartAdapter: MyCartAdapter
    var promoId = ""
    var couponCode = ""
    var couponDes = ""
    var codeName = ""
    var couponPrice = 0.0
    var isCouponApplied = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ADDRESS_RADIO_SELECTION = 0

        myCartViewModel = ViewModelProvider(this)[MyCartViewModel::class.java]
        app = applicationContext as Nayaganj
        orderId = intent.getStringExtra(Constant.orderId)
        binding.includeToolbar.ivBackArrow.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        if (app.user.getAppLanguage() == 1) {
            binding.includeToolbar.toolbarTitle.text = resources.getString(R.string.my_cart_h)
            binding.btnChangeAddress.text = resources.getString(R.string.change_h)
            binding.tvAddress.text = resources.getString(R.string.delivery_address_h)
            binding.textView22.text = resources.getString(R.string.cart_amount_h)
            binding.textView24.text = resources.getString(R.string.delivery_charge_h)
            binding.textView29.text = resources.getString(R.string.total_amount_h)

        } else {
            binding.includeToolbar.toolbarTitle.text = "My Cart"
        }

        binding.btnChangeAddress.setOnClickListener {

            val intent = Intent(this@MyCartActivity, AddressListActivity::class.java)
            intent.putExtra("ADDRESS_ID", addressId)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.tvViewOffer.setOnClickListener{

            binding.tvViewOffer.isClickable=false
            val intent = Intent(this@MyCartActivity, CouponActivity::class.java)
            intent.putExtra(Constant.promoCodeId, promoId)
            intent.putExtra(Constant.cartAmount, binding.tvCartAmount.text.toString())
            resultLauncher.launch(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnLoginButton.setOnClickListener {
            if (binding.btnLoginButton.text.toString() == "Checkout" || binding.btnLoginButton.text.toString() == resources.getString(R.string.checkout_h)) {
                if (addressId == null) {
                    startActivity(Intent(this@MyCartActivity, AddressListActivity::class.java))
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val allProduct = AppDataBase.getInstance(this@MyCartActivity).productDao()
                            .getProductList()
                        if (allProduct.isNotEmpty()) {
                            runOnUiThread {
                                val intent =
                                    Intent(this@MyCartActivity, PaymentOptionActivity::class.java)
                                intent.putExtra(
                                    "TOTAL_AMOUNT",
                                    binding.tvFinalAmount.text.toString()
                                )
                                intent.putExtra("ADDRESS_ID", addressId)
                                intent.putExtra("PROMO_CODE", "")
                                intent.putExtra("WALLET_BALANCE", "0")
                                startActivity(intent)

                            }
                        }
                    }
                }

            } else {
                startActivity(Intent(this@MyCartActivity, LoginActivity::class.java))
                IS_FROM_MYCART = true
            }
        }

        binding.btnShopNow.setOnClickListener {
            finish()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            if (app.user.getLoginSession()) {
                AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllSavedAmount()
                AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllCartData()
            }
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Coupon Code
            val data: Intent? = result.data
            couponCode = data?.getStringExtra("couponCode").toString()
            couponDes = data?.getStringExtra("saveAmountText").toString()
            promoId = data?.getStringExtra("promoCodeId").toString()
            couponPrice = data?.getDoubleExtra("Couponprice", 0.0)!!
            codeName = data.getStringExtra("codeName").toString()
            couponList.clear()
            couponList = data.getParcelableArrayListExtra("productlist")!!

            myCartAdapter = MyCartAdapter(
                this@MyCartActivity,
                cartList!!,
                couponList,
                this@MyCartActivity,
                this@MyCartActivity,
                app, promoId, this
            )
            binding.rvMycartList.adapter = myCartAdapter
            setCouponData()

            isCouponApplied = true//// check if coupon applied
        }
        CHANGE_ADDRESS_VALUE = ""

        lifecycleScope.launch(Dispatchers.IO) {
            if (app.user.getLoginSession())
                AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllProduct()
            Log.e("TAG", "getLoginSession: " + app.user.getLoginSession())
            AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllSavedAmount()
            AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllCartData()
            withContext(Dispatchers.Main) {
                if (orderId == null) {
                    orderId = ""
                }
                if (Utility.isAppOnLine(this@MyCartActivity, object : OnInternetCheckListener {
                        override fun onInternetAvailable() {
                            getMyCartData(orderId!!)
                        }
                    }))
                    getMyCartData(orderId!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (app.user.getLoginSession()) {
            if (app.user.getAppLanguage() == 1) {
                binding.btnLoginButton.text = resources.getString(R.string.checkout_h)
            } else {
                binding.btnLoginButton.text = "Checkout"
            }
        } else {
            binding.btnLoginButton.text = "Login/SignUp"
        }

        binding.tvViewOffer.isClickable = true
        //binding.changeOffers.isClickable=true

        if (CHANGE_ADDRESS_VALUE != "") {
            binding.tvAddressDetail.text = CHANGE_ADDRESS_VALUE
        }
        if (app.user.getLoginSession()) {
            if (orderId == null) {
                orderId = ""
            }
            if (Utility.isAppOnLine(this@MyCartActivity, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        getMyCartData(orderId!!)
                    }
                }))
                getMyCartData(orderId!!)
        } else {
            getLocalCartData()
        }

    }

    private fun getLocalCartData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.mainConstraintLayout.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            val listOfProduct =
                AppDataBase.getInstance(this@MyCartActivity).productDao().getProductList()
            lifecycleScope.launch(Dispatchers.Main) {
                if (listOfProduct.isNotEmpty()) {
                    binding.rvMycartList.layoutManager = LinearLayoutManager(this@MyCartActivity)
                    binding.rvMycartList.adapter =
                        LocalMyCartAdapter(this@MyCartActivity, listOfProduct, this@MyCartActivity)
                    binding.nestedscrollview.isNestedScrollingEnabled = false
                    // animation on recyclerview
                    binding.rvMycartList.viewTreeObserver.addOnPreDrawListener(
                        object : ViewTreeObserver.OnPreDrawListener {
                            override fun onPreDraw(): Boolean {
                                binding.rvMycartList.viewTreeObserver.removeOnPreDrawListener(this)
                                for (i in 0 until binding.rvMycartList.childCount) {
                                    val v: View = binding.rvMycartList.getChildAt(i)
                                    v.alpha = 0.0f
                                    v.animate().alpha(1.0f)
                                        .setDuration(300)
                                        .setStartDelay((i * 50).toLong())
                                        .start()
                                }
                                return true
                            }
                        })

                    binding.progressBar.visibility = View.GONE
                    binding.mainConstraintLayout.visibility = View.VISIBLE
                    binding.finalCheckoutLayout.visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed({ calculateAmount() }, 200)
                    Handler(Looper.getMainLooper()).postDelayed({ loadSavedAmount() }, 200)
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.finalCheckoutLayout.visibility = View.GONE
                    binding.constraintCartAmountLayout.visibility = View.GONE
                    binding.constraintOfferLayout.visibility = View.GONE
                    binding.materialAddressCardview.visibility = View.GONE
                    binding.rvMycartList.visibility = View.GONE
                    binding.emptyCartLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getMyCartData(orderId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.mainConstraintLayout.visibility = View.GONE
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.ORDER_ID, orderId)

        RetrofitClient.instance.getMyCartData(app.user.getUserDetails()!!.userId, Constant.DEVICE_TYPE, jsonObject)
            .enqueue(object : Callback<MyCartModel> {
                override fun onResponse(
                    call: Call<MyCartModel>,
                    response: Response<MyCartModel>
                ) {
                    if(response.isSuccessful){
                        cartList = response.body()!!.cartList
                        myCartModel = response.body()!!
                        if (response.body()!!.cartList.size > 0) {
                            myCartAdapter = MyCartAdapter(
                                this@MyCartActivity,
                                response.body()!!.cartList,
                                couponList,
                                this@MyCartActivity,
                                this@MyCartActivity,
                                app,
                                promoId,
                                this@MyCartActivity
                            )
                            binding.rvMycartList.layoutManager = LinearLayoutManager(this@MyCartActivity)
                            binding.nestedscrollview.isNestedScrollingEnabled = false
                            binding.rvMycartList.adapter = myCartAdapter
                            binding.progressBar.visibility = View.GONE

                            binding.mainConstraintLayout.visibility = View.VISIBLE
                            binding.offerCardLayout.visibility = View.VISIBLE
                            try {
                                setAddressDetail(myCartModel.address.address)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                binding.btnChangeAddress.text = "Add Address"
                                binding.tvAddressDetail.text = "No Address Selected"
                                addressId = null
                            }

                            binding.materialAddressCardview.visibility = View.VISIBLE
                            Handler(Looper.getMainLooper()).postDelayed(Runnable { calculateAmount() }, 200)
                            Handler(Looper.getMainLooper()).postDelayed(Runnable { loadSavedAmount() }, 200)
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.emptyCartLayout.visibility = View.VISIBLE
                            Thread {
                                AppDataBase.getInstance(this@MyCartActivity).productDao()
                                    .deleteAllSavedAmount()
                                AppDataBase.getInstance(this@MyCartActivity).productDao()
                                    .deleteAllCartData()
                                AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllProduct()
                            }.start()
                        }
                    }else{
                        Utility.showToast(this@MyCartActivity,response.message())
                    }
                }

                override fun onFailure(call: Call<MyCartModel>, t: Throwable) {
                    Utility.serverNotResponding(this@MyCartActivity,t.message.toString())
                }
            })



    }

    private fun setAddressDetail(address: MyCartModel.Address.Address) {

        addressId = myCartModel.address.id
        val addressString =
            address.houseNo + "," + address.apartName + "," + address.street + "," + address.landmark + "," +
                    address.city + "-" + address.pincode
        binding.tvAddressDetail.text = addressString
        binding.btnChangeAddress.text = "Change Address"
    }

    override fun onClickAddOrRemoveItem(
        action: String,
        productDetail: ProductDetail,
        addRemovTextView: TextView
    ) {
        when (action) {
            Constant.INSERT -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().insertProduct(this@MyCartActivity, productDetail)
                }
                if (app.user.getLoginSession()) {
                    Utility().addRemoveItem(
                        app.user.getUserDetails()?.userId,
                        "add",
                        productDetail.productId,
                        productDetail.variantId,
                        ""
                    )
                }
            }
            Constant.ADD -> {
                if(app.user.getLoginSession()){
                    val jsonObject = JsonObject()
                    jsonObject.addProperty(Constant.PRODUCT_ID, productDetail.productId)
                    jsonObject.addProperty(Constant.ACTION, action)
                    jsonObject.addProperty(Constant.VARIANT_ID, productDetail.variantId)
                    jsonObject.addProperty(Constant.PROMO_CODE, promoId)

                    RetrofitClient.instance.addremoveItemRequest(
                        app.user.getUserDetails()?.userId,
                        Constant.DEVICE_TYPE,
                        jsonObject
                    )
                        .enqueue(object : Callback<AddRemoveModel> {
                            override fun onResponse(
                                call: Call<AddRemoveModel>,
                                response: Response<AddRemoveModel>
                            ) {
                                addRemovTextView.isEnabled = true
                                if(response.isSuccessful) {
                                    if (response.body()!!.status)
                                        if (isCouponApplied) {
                                            if (response.body()!!.updatedCartList != null) {
                                                couponList = response.body()!!.updatedCartList!!
                                            } else {
                                                couponList.clear()
                                            }
                                            couponPrice = response.body()!!.promoCodeDiscountAmount
                                            getMyCartData("")
                                            setCouponData()
                                        }
                                    if (response.body()!!.promoCodeDiscountAmount <= 0) {
                                        isCouponApplied = false
                                        promoId = ""
                                    }
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        AppDataBase.getInstance(this@MyCartActivity).productDao()
                                            .updateProduct(
                                                productDetail.itemQuantity,
                                                productDetail.productId,
                                                productDetail.variantId
                                            )
                                    }
                                }else{
                                    Utility.serverNotResponding(this@MyCartActivity,response.message())
                                }
                            }
                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                addRemovTextView.isEnabled = true
                                Utility.serverNotResponding(this@MyCartActivity,t.message.toString())
                            }
                        })
                }else{
                    addRemovTextView.isEnabled=true
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDataBase.getInstance(this@MyCartActivity).productDao().updateProduct(productDetail.itemQuantity, productDetail.productId, productDetail.variantId)
                    }
                }
            }
            Constant.REMOVE -> {
                if (app.user.getLoginSession()) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty(Constant.PRODUCT_ID, productDetail.productId)
                    jsonObject.addProperty(Constant.ACTION, action)
                    jsonObject.addProperty(Constant.VARIANT_ID, productDetail.variantId)
                    jsonObject.addProperty(Constant.PROMO_CODE, promoId)

                    RetrofitClient.instance.addremoveItemRequest(
                        app.user.getUserDetails()?.userId,
                        Constant.DEVICE_TYPE,
                        jsonObject
                    )
                        .enqueue(object : Callback<AddRemoveModel> {
                            override fun onResponse(
                                call: Call<AddRemoveModel>,
                                response: Response<AddRemoveModel>
                            ) {
                                if (response.isSuccessful) {
                                    if (response.body()!!.status)
                                        addRemovTextView.isEnabled = true

                                    if (response.body()!!.status)
                                        if (isCouponApplied) {
                                            if (response.body()!!.updatedCartList != null) {
                                                couponList = response.body()!!.updatedCartList!!
                                            } else {
                                                couponList.clear()
                                            }

                                            couponPrice = response.body()!!.promoCodeDiscountAmount
                                            getMyCartData("")
                                            setCouponData()
                                        }
                                    if (response.body()!!.promoCodeDiscountAmount <= 0) {
                                        isCouponApplied = false
                                        promoId = ""
                                    }

                                    if (productDetail.itemQuantity > 0) {
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            AppDataBase.getInstance(this@MyCartActivity)
                                                .productDao().updateProduct(
                                                    productDetail.itemQuantity,
                                                    productDetail.productId,
                                                    productDetail.variantId
                                                )
                                        }
                                    } else {
                                        Log.e("TAG", "onResponse: Item Deleted")
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            AppDataBase.getInstance(this@MyCartActivity)
                                                .productDao().deleteProduct(
                                                    productDetail.productId,
                                                    productDetail.variantId
                                                )
                                        }
                                    }

                                }else{
                                    Utility.serverNotResponding(this@MyCartActivity,response.message())
                                }
                            }

                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(this@MyCartActivity,t.message.toString())
                            }
                        })
                }else{
                    addRemovTextView.isEnabled=true
                    if (productDetail.itemQuantity > 0) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            Utility().updateProduct(
                                this@MyCartActivity,
                                productDetail.productId,
                                productDetail.variantId,
                                productDetail.itemQuantity
                            )
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            Utility().deleteProduct(
                                this@MyCartActivity,
                                productDetail.productId,
                                productDetail.variantId
                            )
                        }
                    }
                }
            }
            Constant.DELETE_COUPON_ITEM -> {
                couponList.clear()
                promoId = ""
                couponPrice = 0.0
                getMyCartData("")
                setCouponData()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({ calculateAmount() }, 200)
        Handler(Looper.getMainLooper()).postDelayed({ loadSavedAmount() }, 200)
    }

    private fun calculateAmount() {
        var cartAmount = 0.0
        var totalAmount: Double

        lifecycleScope.launch(Dispatchers.IO) {
            if (app.user.getLoginSession()) {
                cartAmount = async {
                    val cartList = AppDataBase.getInstance(this@MyCartActivity).productDao()
                        .getCartItemList()
                    Log.e("TAG", "calculateAmount: " + cartList)
                    if (cartList.isNotEmpty()) {
                        for (item in cartList) {
                            cartAmount += item.cartAmount
                        }
                    }
                    return@async cartAmount
                }.await()

            } else {
                cartAmount = async {
                    val listOfProduct = Utility().getAllProductList(this@MyCartActivity)
                    if (listOfProduct.isNotEmpty()) {
                        for (item in listOfProduct) {
                            cartAmount += (item.vPrice - (item.vPrice * item.vDiscount) / 100) * item.itemQuantity
                        }
                        return@async cartAmount
                    } else {
                        return@async 0.0
                    }

                }.await()
            }
            Log.e("TAG", "Cart Amount: " + cartAmount)

            withContext(Dispatchers.Main) {
                if (cartAmount <= 0) {
                    binding.finalCheckoutLayout.visibility = View.GONE
                    binding.constraintCartAmountLayout.visibility = View.GONE
                    binding.constraintOfferLayout.visibility = View.GONE
                    binding.materialAddressCardview.visibility = View.GONE
                    binding.rvMycartList.visibility = View.GONE
                    binding.emptyCartLayout.visibility = View.VISIBLE
                    binding.mainConstraintLayout.visibility = View.GONE
                    Thread {
                        AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllSavedAmount()
                        AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllCartData()
                        AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllProduct()
                    }.start()
                } else {
                    cartAmount = Utility().formatTotalAmount(cartAmount)
                    if (app.user.getLoginSession()) {
                        if (cartAmount > myCartModel.deliveryChargesThreshHold) {
                            binding.tvDeliveryCharges.text = "0.0"
                            totalAmount = cartAmount

                        } else {
                            totalAmount = cartAmount + myCartModel.deliveryCharges
                            binding.tvDeliveryCharges.text =
                                resources.getString(R.string.Rs) + " " + myCartModel.deliveryCharges.toString()
                        }
                        if (isCouponApplied) {
                            totalAmount -= couponPrice
                        }
                        binding.deliveryCardLayout.visibility = View.VISIBLE

                    } else {
                        totalAmount = cartAmount
                        if (isCouponApplied) {
                            totalAmount -= couponPrice
                        }
                    }

                    binding.tvCartAmount.text =resources.getString(R.string.Rs)+" "+ cartAmount
                    binding.tvTotalAmount.text =resources.getString(R.string.Rs)+" "+ totalAmount
                    binding.tvFinalAmount.text =resources.getString(R.string.Rs)+" "+ Utility().formatTotalAmount(totalAmount)

                    binding.mainConstraintLayout.visibility = View.VISIBLE
                    binding.finalCheckoutLayout.visibility = View.VISIBLE
                    binding.constraintCartAmountLayout.visibility = View.VISIBLE
                    binding.constraintOfferLayout.visibility = View.VISIBLE

                    binding.rvMycartList.visibility = View.VISIBLE
                    binding.emptyCartLayout.visibility = View.GONE
                }
            }

        }


    }

    private fun loadSavedAmount() {
        var finalSaveAmount = 0.0
        lifecycleScope.launch(Dispatchers.IO) {
            val savedAmountList =
                AppDataBase.getInstance(this@MyCartActivity).productDao().getSavedAmountList()
            if (savedAmountList.isNotEmpty()) {
                for (item in savedAmountList) {
                    finalSaveAmount += item.totalAmount
                }
                runOnUiThread {
                    binding.tvFinalSavedAmount.text =
                        Utility().formatTotalAmount(finalSaveAmount).toString()
                }
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        );
    }

    private fun setCouponData() {

        if (couponPrice == 0.0) {
            binding.tvCouponName.visibility = View.GONE
            binding.tvCouponDes.visibility = View.VISIBLE
            binding.tvCouponAmount.visibility = View.GONE
            binding.tvCouponDes.text = "Select a coupon code"
            binding.tvViewOffer.text = "View Offer"
            binding.rlCouponAmountLayout.visibility = View.GONE
        } else {
            binding.rlCouponAmountLayout.visibility = View.VISIBLE
            binding.tvCoupon.text = "Coupon-($codeName):"
            binding.tvFinalCouponAmount.text =
                "- " + resources.getString(R.string.Rs) + " " + couponPrice.toString()
            binding.tvCouponName.text = couponCode
            binding.tvCouponDes.text = couponDes
            binding.tvViewOffer.text = "Change Offer"
            binding.tvCouponAmount.text =
                resources.getString(R.string.Rs) + " " + couponPrice.toString()

            binding.tvCouponName.visibility = View.VISIBLE
            binding.tvCouponDes.visibility = View.VISIBLE
            binding.tvCouponAmount.visibility = View.VISIBLE
        }
    }

    override fun removeProductFromList(
        cartList: MutableList<MyCartModel.Cart>,
        position: Int,
        pId: String,
        vId: String,
        promoCode: String
    ) {
        itemDeleteDialog(cartList, position, pId, vId, promoCode)
    }

    private fun itemDeleteDialog(
        cartList: MutableList<MyCartModel.Cart>,
        position: Int,
        pId: String,
        vId: String,
        promoCode: String
    ) {
        MaterialAlertDialogBuilder(this@MyCartActivity)
            .setTitle("Delete Item? ")
            .setMessage("Are you sure, you want to delete this product?")
            .setPositiveButton(
                "Yes"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()

                val jsonObject = JsonObject()
                jsonObject.addProperty(Constant.PRODUCT_ID, pId)
                jsonObject.addProperty(Constant.VARIANT_ID, vId)
                jsonObject.addProperty(Constant.promoCodeId, promoCode)

                RetrofitClient.instance.removeProduct(
                    app.user.getUserDetails()?.userId,
                    Constant.DEVICE_TYPE,
                    jsonObject
                )
                    .enqueue(object : Callback<ApiResponseModel> {
                        override fun onResponse(
                            call: Call<ApiResponseModel>,
                            response: Response<ApiResponseModel>
                        ) {
                            if (response.body()!!.status) {
                                Thread {
                                    AppDataBase.getInstance(this@MyCartActivity).productDao()
                                        .deleteProduct(pId, vId)
                                    AppDataBase.getInstance(this@MyCartActivity).productDao()
                                        .deleteSavedAmount(pId, vId.toInt())
                                    AppDataBase.getInstance(this@MyCartActivity).productDao()
                                        .deleteCartItem(pId, vId)
                                }.start()

                                cartList.removeAt(position)
                                myCartAdapter.notifyItemRemoved(position)
                                Handler(Looper.getMainLooper()).postDelayed(
                                    { calculateAmount() },
                                    200
                                )
                                Handler(Looper.getMainLooper()).postDelayed(
                                    { loadSavedAmount() },
                                    200
                                )

                                if (response.body()!!.promoCodeDiscountAmount <= 0) {
                                    promoId = ""
                                    couponPrice = 0.0
                                    couponList.clear()
                                    // myCartAdapter.notifyDataSetChanged()
                                    setCouponData()
                                }

                            }
                        }

                        override fun onFailure(call: Call<ApiResponseModel>, t: Throwable) {
                            Toast.makeText(this@MyCartActivity, t.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    })


            }
            .setNegativeButton(
                "NO"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
    }

}