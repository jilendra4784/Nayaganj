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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.paytm.pgsdk.TransactionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.model.PaytmTransactionResponse
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.PaymentOptionsViewModel
import naya.ganj.app.data.sidemenu.view.MyOrderActivity
import naya.ganj.app.databinding.ActivityPaymentOptionBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import naya.ganj.app.utility.NetworkResult
import naya.ganj.app.utility.Utility

class PaymentOptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityPaymentOptionBinding
    lateinit var viewmodel: PaymentOptionsViewModel
    lateinit var app: Nayaganj

    var amount = ""
    var addressId = ""
    var promocode = ""
    var walletBalance = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = applicationContext as Nayaganj

        binding.include3.ivBackArrow.setOnClickListener { finish() }
        binding.include3.toolbarTitle.text = "Make Payment"

        viewmodel = ViewModelProvider(
            this,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        )[PaymentOptionsViewModel::class.java]

        intent.extras.let {
            //amount = intent.getStringExtra("TOTAL_AMOUNT").toString()
            amount = "1"
            addressId = intent.getStringExtra("ADDRESS_ID").toString()
            promocode = intent.getStringExtra("PROMO_CODE").toString()
            walletBalance = intent.getStringExtra("WALLET_BALANCE").toString()
            setUI()
        }
    }

    private fun setUI() {
        binding.tvFinalAmount.text = amount
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

            if (binding.finalSubmitButton.text.toString().equals("Place Order")) {
                if (Utility.isAppOnLine(
                        this@PaymentOptionActivity,
                        object : OnInternetCheckListener {
                            override fun onInternetAvailable() {
                                placeOrderRequest(0, "COD")
                            }
                        })
                )
                    placeOrderRequest(0, "COD")
            } else {
                if (Utility.isAppOnLine(
                        this@PaymentOptionActivity,
                        object : OnInternetCheckListener {
                            override fun onInternetAvailable() {
                                placeOrderRequest(1, "PAYTM")
                            }
                        })
                )
                    placeOrderRequest(1, "PAYTM")
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

    private fun placeOrderRequest(paymentMode: Int, mode: String) {

        val jsonObject = JsonObject()

        jsonObject.addProperty(Constant.addressId, addressId)
        jsonObject.addProperty(Constant.mode, mode)
        jsonObject.addProperty(Constant.promoCodeId, "")
        jsonObject.addProperty(Constant.cashBackAmount, 0)

        viewmodel.orderPlaceRequest(app.user.getUserDetails()?.userId, jsonObject)
            .observe(this) { response ->

                when (response) {

                    is NetworkResult.Success -> {
                        val it = response.data!!
                        if (it.status) {
                            if (paymentMode == 0) {
                                Handler(Looper.getMainLooper()).postDelayed(
                                    Runnable { orderPlacedDialog() },
                                    1000
                                )
                            } else {
                                // For Paytm Transaction
                                startPaytmPayment(it.pToken, it.paymentOrderId)
                            }
                        } else {
                            Toast.makeText(this@PaymentOptionActivity, it.msg, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    is NetworkResult.Error -> {
                        Utility.serverNotResponding(
                            this@PaymentOptionActivity,
                            response.message.toString()
                        )
                    }

                }
            }
    }

    private fun startPaytmPayment(pToken: String, orderId: String) {
        // for production mode use it
        val host = "https://securegw.paytm.in/"
        // val callBackUrl = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId

        val callBackUrl = "http://stageapis.nayaganj.com/api/paytmPurchase"
        Log.d("callBackUrl", "" + callBackUrl)

        val paytmOrder = PaytmOrder(
            orderId,
            resources.getString(R.string.mid),
            pToken,
            amount,
            callBackUrl
        )

        val transactionManager = TransactionManager(paytmOrder, object :
            PaytmPaymentTransactionCallback {

            override fun onTransactionResponse(bundle: Bundle?) {

                Log.d("onTransactionResponse", "" + bundle.toString())
                var model: PaytmTransactionResponse = Gson().fromJson(
                    (bundle?.getString("response")).toString(),
                    PaytmTransactionResponse::class.java
                )

                if (model.STATUS.equals(Constant.TXN_SUCCESS)) {
                    verifyTransactionStatus(model.ORDERID)
                } else {
                    finish()
                    Toast.makeText(
                        this@PaymentOptionActivity,
                        resources.getString(R.string.transactions_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun networkNotAvailable() {
                finish();
            }

            override fun onErrorProceed(s: String) {
                Log.e(TAG, " onErrorProcess $s")

            }

            override fun clientAuthenticationFailed(s: String) {

                Log.e(TAG, "Clientauth $s")
            }

            override fun someUIErrorOccurred(s: String) {
                Log.e(TAG, " UI error $s")

            }

            override fun onErrorLoadingWebPage(i: Int, s: String, s1: String) {
                Log.e(TAG, " error loading web $s--$s1")

            }

            override fun onBackPressedCancelTransaction() {
                paytmPurchase(orderId)
                finish()
                Log.e(TAG, "backPress ")
                Toast.makeText(
                    this@PaymentOptionActivity,
                    resources.getString(R.string.transactions_failed),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onTransactionCancel(s: String, bundle: Bundle) {
                Log.e(TAG, " transaction cancel $s")
                // Toast.makeText(this@PaymentOptionActivity, resources.getString(R.string.transactions_failed), Toast.LENGTH_LONG).show()

            }
        })

        transactionManager.setAppInvokeEnabled(false)
        transactionManager.setShowPaymentUrl(host + "theia/api/v1/showPaymentPage")
        transactionManager.startTransaction(this, ActivityRequestCode)

    }

    private fun paytmPurchase(orderId: String) {

        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty("TXNID", "")
            jsonObject.addProperty("PAYMENTMODE", "")
            jsonObject.addProperty("TXNDATE", "")
            jsonObject.addProperty("ORDERID", orderId)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        viewmodel.paytmPurchaseRequest(app.user.getUserDetails()?.userId, jsonObject)
            .observe(this) { response ->
                when (response) {
                    is NetworkResult.Success -> {

                    }
                    is NetworkResult.Error -> {
                    }
                }
            }
    }

    private fun verifyTransactionStatus(orderId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.paymentOrderId, orderId)

        viewmodel.verifyTransactionStatus(app.user.getUserDetails()?.userId, jsonObject)
            .observe(this) { response ->
                when (response) {

                    is NetworkResult.Success -> {

                        if (response.data?.status.equals("SUCCESS"))
                            binding.finalSubmitButton.visibility = View.GONE
                        Handler(Looper.getMainLooper()).postDelayed(
                            Runnable { orderPlacedDialog() },
                            1000
                        )

                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(
                            this@PaymentOptionActivity,
                            "Payment Failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    companion object {
        private val TAG = "PaytmPayment"
        private const val ActivityRequestCode = 2

    }
}


/*
        HttpsRequest(ApiConstants.validateTxn, jsonObject, applicationContext, userId,ApiConstants.Android).stringPostJson(
            MainActivity.TAG1, object : Helper {
                @Throws(JSONException::class)

                override fun backResponse(response: JSONObject?) {

                    com.paytm.pgsdk.Log.i("validateResponseresponse", response.toString())

                    var model: ValidateTransResponse = Gson().fromJson(response.toString(), ValidateTransResponse::class.java)

                    if (model.status.equals("SUCCESS") || model.status.equals("PENDING")) {

                        orderPlacedDialog()
                        progressDialog!!.dismiss()

                    } else if (model.status.equals("FAILURE")) {
                        finish()
                        Toast.makeText(this@PaymentOptionActivity, resources.getString(R.string.transactions_failed), Toast.LENGTH_LONG).show()
                        progressDialog!!.dismiss()

                    } else {

                        progressDialog!!.dismiss()
                        Toast.makeText(this@PaymentOptionActivity, resources.getString(R.string.transactions_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun error(s: String?) {

                    progressDialog!!.dismiss()
                    CommonAlertDialog.serverNotResponding(this@PaymentOptionActivity)
                }
            })*/
