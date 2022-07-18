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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.mycart.adapter.MyCartAdapter
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.mycart.viewmodel.MyCartViewModel
import naya.ganj.app.databinding.ActivityMyCartBinding
import naya.ganj.app.interfaces.OnSavedAmountListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility

class MyCartActivity : AppCompatActivity(), OnclickAddOremoveItemListener, OnSavedAmountListener {
    lateinit var app: Nayaganj
    lateinit var myCartViewModel: MyCartViewModel
    lateinit var binding: ActivityMyCartBinding
    lateinit var myCartModel: MyCartModel
    private var addressId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myCartViewModel = ViewModelProvider(this).get(MyCartViewModel::class.java)
        app = applicationContext as Nayaganj

        binding.includeToolbar.ivBackArrow.setOnClickListener { finish() }
        binding.includeToolbar.toolbarTitle.text = "My Cart"

        Log.e("TAG", "onCreate: Login Session " + app.user.getLoginSession())
        if (app.user.getLoginSession()) {
            binding.btnLoginButton.text = "Checkout"
            getMyCartData(app.user.getUserDetails()?.userId.toString(), "")
        } else {
            binding.btnLoginButton.text = "Login/SignUp"
            getLocalCartData()
        }

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
                    val intent = Intent(this@MyCartActivity, PaymentOptionActivity::class.java)
                    intent.putExtra("TOTAL_AMOUNT", binding.tvFinalAmount.text.toString())
                    intent.putExtra("ADDRESS_ID", addressId)
                    intent.putExtra("PROMO_CODE", "")
                    intent.putExtra("WALLET_BALANCE", "0")
                    startActivity(intent)

                }
            } else {
                startActivity(Intent(this@MyCartActivity, LoginActivity::class.java))
            }
        }


        binding.btnShopNow.setOnClickListener {
            finish()
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
            setListData(it)

            /* Handler(Looper.getMainLooper()).postDelayed(Runnable {
                 if (isAdded)
                     calculateAmount()
                 // setSavedAmount()
             }, 300)*/

            // Set Saved Amount
            /* for (item in it.cartList) {
                 lifecycleScope.launch(Dispatchers.IO) {
                     val savedAmountModel =
                         SavedAmountModel(
                             item.productId,
                             item.variantId.toInt(),
                             item.discountPrice.toDouble()
                         )

                     val isItemExist = AppDataBase.getInstance(requireActivity()).productDao()
                         .isSavedItemIsExist(item.productId, item.variantId.toInt())

                     if (isItemExist) {
                         AppDataBase.getInstance(requireActivity()).productDao()
                             .updateAmount(
                                 item.discountPrice.toDouble(),
                                 item.productId,
                                 item.variantId.toInt()
                             )
                     } else {
                         AppDataBase.getInstance(requireActivity()).productDao()
                             .insertSavedAmount(savedAmountModel)
                     }
                 }
             }*/

        }
    }

    private fun setListData(mModel: MyCartModel?) {
        val myCartAdapter =
            mModel?.let {
                MyCartAdapter(
                    this@MyCartActivity,
                    it.cartList,
                    this@MyCartActivity,
                    this@MyCartActivity
                )
            }
        binding.rvMycartList.layoutManager = LinearLayoutManager(this@MyCartActivity)
        binding.rvMycartList.adapter = myCartAdapter
        binding.nestedscrollview.isNestedScrollingEnabled = false
        binding.progressBar.visibility = View.GONE
        binding.mainConstraintLayout.visibility = View.VISIBLE
        binding.finalCheckoutLayout.visibility = View.VISIBLE

        if (app.user.getLoginSession()) {
            if (myCartModel.address != null) {
                setAddressDetail(myCartModel.address.address)
                binding.materialAddressCardview.visibility = View.VISIBLE
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
        /* Utility().addRemoveItem(action, productId, variantId, promoCode)
         if (totalAmount > 0) {
             lifecycle.coroutineScope.launch(Dispatchers.IO) {
                 val isProductExist = async {
                     Utility().isProductAvailable(
                         requireActivity(),
                         productId,
                         variantId
                     )
                 }.await()

                 if (isProductExist) {
                     Utility().updateProduct(requireActivity(), productId, variantId, totalAmount)

                 } else {
                     Utility().insertProduct(requireActivity(), productId, variantId, totalAmount)
                 }
             }
         } else {
             lifecycle.coroutineScope.launch(Dispatchers.IO) {
                 Utility().deleteProduct(requireActivity(), productId, variantId)
             }
         }*/
        Handler(Looper.getMainLooper()).postDelayed(Runnable { calculateAmount() }, 200)
    }

    private fun calculateAmount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listOfProduct = Utility().getAllProductList(this@MyCartActivity)
            runOnUiThread {
                if (listOfProduct.isNotEmpty()) {
                    var cartAmount = 0.0
                    val totalAmount: Double
                    for (item in listOfProduct) {
                        cartAmount += (item.vPrice - (item.vPrice * item.vDiscount) / 100) * item.itemQuantity
                    }
                    cartAmount = Utility().formatTotalAmount(cartAmount)

                    if (app.user.getLoginSession()) {
                        if (cartAmount > myCartModel.deliveryChargesThreshHold) {
                            binding.tvDeliveryCharges.text = "0.0"
                            totalAmount = cartAmount
                        } else {
                            totalAmount = cartAmount + myCartModel.deliveryCharges
                            binding.tvDeliveryCharges.text = myCartModel.deliveryCharges.toString()
                        }
                    } else {
                        totalAmount = cartAmount
                    }

                    binding.tvCartAmount.text = cartAmount.toString()
                    binding.tvTotalAmount.text = totalAmount.toString()
                    binding.tvFinalAmount.text = Utility().formatTotalAmount(totalAmount).toString()
                } else {
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

    override fun onSavedAmount(productId: String, variantId: Int, amount: Double) {

        if (amount > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                val isItemExist = AppDataBase.getInstance(this@MyCartActivity).productDao()
                    .isSavedItemIsExist(productId, variantId)
                if (isItemExist) {
                    AppDataBase.getInstance(this@MyCartActivity).productDao()
                        .updateAmount(amount, productId, variantId)
                }
            }

        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                AppDataBase.INSTANCE?.productDao()
                    ?.deleteAmount(productId, variantId)
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            setSavedAmount()
        }, 300)
    }

    private fun setSavedAmount() {
        var finalSaveAmount = 0.0
        lifecycleScope.launch(Dispatchers.IO) {
            val listOfSavedAmount =
                AppDataBase.getInstance(this@MyCartActivity).productDao().getSavedAmountList()
            if (!listOfSavedAmount.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    if (listOfSavedAmount.isNotEmpty())
                        for (item in listOfSavedAmount) {
                            finalSaveAmount += item.totalAmount
                        }
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