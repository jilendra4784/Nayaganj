package naya.ganj.app.data.mycart.view

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import naya.ganj.app.R
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.PaymentOptionsViewModel
import naya.ganj.app.data.sidemenu.view.MyOrderActivity
import naya.ganj.app.databinding.ActivityPaymentOptionBinding
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import naya.ganj.app.utility.Utility
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentOptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityPaymentOptionBinding
    lateinit var viewmodel: PaymentOptionsViewModel

    var amount = ""
    var addressId = ""
    var promocode = ""
    var walletBalance = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.include3.ivBackArrow.setOnClickListener { finish() }
        binding.include3.toolbarTitle.text = "Make Payment"

        viewmodel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        ).get(PaymentOptionsViewModel::class.java)

        intent.extras.let {
            amount = intent.getStringExtra("TOTAL_AMOUNT").toString()
            addressId = intent.getStringExtra("ADDRESS_ID").toString()
            promocode = intent.getStringExtra("PROMO_CODE").toString()
            walletBalance = intent.getStringExtra("WALLET_BALANCE").toString()
            setUI()
        }


    }

    private fun setUI() {
        binding.tvFinalAmount.text = resources.getString(R.string.Rs) + amount
        binding.radioButtonCashOnDelivery.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                binding.radioButtonWallet.isChecked = false
                binding.finalSubmitButton.text = "Place Order"
                binding.finalSubmitButton.visibility = View.VISIBLE
            }
        }

        binding.radioButtonWallet.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                binding.radioButtonCashOnDelivery.isChecked = false
                binding.finalSubmitButton.text = "Place Order & Pay"
                binding.finalSubmitButton.visibility = View.VISIBLE
            }
        }

        binding.finalSubmitButton.setOnClickListener {
            placeOrderRequest(0, "COD")
        }
    }

    private fun placeOrderRequest(paymentMode: Int, mode: String) {

        val jsonObject = JsonObject()

        jsonObject.addProperty(Constant.addressId, addressId)
        jsonObject.addProperty(Constant.mode, mode)
        jsonObject.addProperty(Constant.promoCodeId, "")
        jsonObject.addProperty(Constant.cashBackAmount, 0)

        viewmodel.orderPlaceRequest(jsonObject).observe(this) {
            Log.e("TAG", "placeOrderRequest: $it")
            it.let {
                if (it.status) {
                    if (paymentMode == 0) {
                        Handler(Looper.getMainLooper()).postDelayed(Runnable {
                            orderPlacedDialog()
                        }, 1000)
                    } else {
                        // For Paytm Transaction
                    }
                }
            }
        }
    }

    private fun orderPlacedDialog() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.order_placed_dialog)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        val checkOrders: TextView = dialog.findViewById(R.id.check_orders)

        checkOrders.setOnClickListener {
            // Clear Database
            lifecycleScope.launch(Dispatchers.IO) {
                Utility().deleteAllProduct(this@PaymentOptionActivity)
            }
            
            val intent = Intent(applicationContext, MyOrderActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}