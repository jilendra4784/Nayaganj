package naya.ganj.app.data.mycart.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.mycart.adapter.MyCartAdapter
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.mycart.viewmodel.MyCartViewModel
import naya.ganj.app.databinding.ActivityMyCartBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.ADDRESS_RADIO_SELECTION
import naya.ganj.app.utility.Utility

class MyCartActivity : AppCompatActivity(), OnclickAddOremoveItemListener {
    lateinit var app: Nayaganj
    lateinit var myCartViewModel: MyCartViewModel
    lateinit var binding: ActivityMyCartBinding
    lateinit var myCartModel: MyCartModel
    private var addressId: String? = null
    var orderId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ADDRESS_RADIO_SELECTION = 0

        myCartViewModel = ViewModelProvider(this).get(MyCartViewModel::class.java)
        app = applicationContext as Nayaganj
        orderId = intent.getStringExtra(Constant.orderId)
        binding.includeToolbar.ivBackArrow.setOnClickListener { finish() }
        binding.includeToolbar.toolbarTitle.text = "My Cart"

        binding.btnChangeAddress.setOnClickListener {

            val intent = Intent(this@MyCartActivity, AddressListActivity::class.java)
            intent.putExtra("ADDRESS_ID", addressId)
            startActivity(intent)
        }

        binding.btnLoginButton.setOnClickListener {
            if (binding.btnLoginButton.text.toString() == "Checkout") {
                if (addressId == null) {
                    startActivity(Intent(this@MyCartActivity, AddAddressActivity::class.java))
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
            }
        }

        binding.btnShopNow.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        Thread {
            AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllSavedAmount()
            AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllCartData()
        }.start()

        if (app.user.getLoginSession()) {
            binding.btnLoginButton.text = "Checkout"
            if(orderId==null){
                orderId=""
            }

            getMyCartData(app.user.getUserDetails()?.userId.toString(), orderId!!)
        } else {
            binding.btnLoginButton.text = "Login/SignUp"
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
                    binding.progressBar.visibility = View.GONE
                    binding.mainConstraintLayout.visibility = View.VISIBLE
                    binding.finalCheckoutLayout.visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed(Runnable { calculateAmount() }, 200)
                    Handler(Looper.getMainLooper()).postDelayed(Runnable { loadSavedAmount() }, 200)
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


    private fun getMyCartData(userId: String, orderId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.mainConstraintLayout.visibility = View.GONE
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.ORDER_ID, orderId)
        myCartViewModel.getMyCartData(userId, jsonObject).observe(this@MyCartActivity) {
            myCartModel = it
            if (it.cartList.size > 0) {
                val adapter = MyCartAdapter(
                    this@MyCartActivity,
                    it.cartList,
                    this@MyCartActivity,
                    this@MyCartActivity,
                    app
                )
                binding.rvMycartList.layoutManager = LinearLayoutManager(this@MyCartActivity)
                binding.nestedscrollview.isNestedScrollingEnabled = false
                binding.rvMycartList.adapter = adapter
                binding.progressBar.visibility = View.GONE

                binding.mainConstraintLayout.visibility = View.VISIBLE

                if (myCartModel.address != null) {
                    setAddressDetail(myCartModel.address.address)
                    binding.materialAddressCardview.visibility = View.VISIBLE
                }
                Handler(Looper.getMainLooper()).postDelayed(Runnable { calculateAmount() }, 200)
                Handler(Looper.getMainLooper()).postDelayed(Runnable { loadSavedAmount() }, 200)
            } else {
                binding.progressBar.visibility = View.GONE
                binding.emptyCartLayout.visibility = View.VISIBLE
                Thread {
                    AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllSavedAmount()
                    AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllCartData()
                    AppDataBase.getInstance(this@MyCartActivity).productDao().deleteAllProduct()
                }.start()
            }
        }
    }


    private fun setAddressDetail(address: MyCartModel.Address.Address) {
        addressId = myCartModel.address.id
        val addressString =
            address.houseNo + "," + address.apartName + "," + address.street + "," + address.landmark + "," +
                    address.city + "-" + address.pincode
        binding.tvAddressDetail.text = addressString
    }


    override fun onClickAddOrRemoveItem(
        action: String,
        productDetail: ProductDetail
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
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().updateProduct(
                        this@MyCartActivity,
                        productDetail.productId,
                        productDetail.variantId,
                        productDetail.itemQuantity
                    )
                }
                if (app.user.getLoginSession()) {
                    Utility().addRemoveItem(
                        app.user.getUserDetails()?.userId,
                        Constant.ADD,
                        productDetail.productId,
                        productDetail.variantId,
                        ""
                    )
                }

            }
            Constant.REMOVE -> {
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


                if (app.user.getLoginSession()) {
                    Utility().addRemoveItem(
                        app.user.getUserDetails()?.userId,
                        "remove",
                        productDetail.productId,
                        productDetail.variantId,
                        ""
                    )
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(Runnable { calculateAmount() }, 200)
        Handler(Looper.getMainLooper()).postDelayed(Runnable { loadSavedAmount() }, 200)

    }

    private fun calculateAmount() {
        var cartAmount = 0.0
        var totalAmount: Double

        lifecycleScope.launch(Dispatchers.IO) {
            if (app.user.getLoginSession()) {
                cartAmount = async {
                    val cartList = AppDataBase.getInstance(this@MyCartActivity).productDao()
                        .getCartItemList()
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
                    }
                    return@async cartAmount
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
                            binding.tvDeliveryCharges.text = myCartModel.deliveryCharges.toString()
                        }
                        binding.deliveryCardLayout.visibility = View.VISIBLE

                    } else {
                        totalAmount = cartAmount
                    }

                    binding.tvCartAmount.text = cartAmount.toString()
                    binding.tvTotalAmount.text = totalAmount.toString()
                    binding.tvFinalAmount.text = Utility().formatTotalAmount(totalAmount).toString()

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
    }
}