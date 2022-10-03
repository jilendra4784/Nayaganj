package naya.ganj.app.data.mycart.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.mycart.adapter.CouponListAdapter
import naya.ganj.app.data.mycart.model.CouponModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.CouponViewModel
import naya.ganj.app.databinding.ActivityCoupenBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.MyViewModelFactory
import naya.ganj.app.utility.NetworkResult
import naya.ganj.app.utility.Utility

class CouponActivity : AppCompatActivity(), CouponListAdapter.ApplyCouponInterface {

    lateinit var binding: ActivityCoupenBinding
    lateinit var viewModel: CouponViewModel
    lateinit var app: Nayaganj
    var amount: Double = 0.0
    var promoId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoupenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = applicationContext as Nayaganj
        amount = intent.getStringExtra(Constant.cartAmount)?.substring(1)!!.toDouble()
        promoId = intent.getStringExtra(Constant.promoCodeId).toString()
        if (app.user.getAppLanguage() == 1) {
            // set Hindi Labels
        } else {
            binding.includeLayout.toolbarTitle.text = "Available Coupons"
            binding.includeLayout.ivBackArrow.setOnClickListener { finish() }
        }


        viewModel = ViewModelProvider(
            this@CouponActivity,
            MyViewModelFactory(AddressListRespositry(RetrofitClient.instance))
        )[CouponViewModel::class.java]
        if (Utility.isAppOnLine(this@CouponActivity, object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getCouponList()
                }
            })) {
            getCouponList()
        }
    }

    private fun getCouponList() {
        viewModel.getCouponList(app.user.getUserDetails()?.userId)
            .observe(this@CouponActivity) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        setListData(response)
                    }
                    is NetworkResult.Error -> {
                        Utility.showToast(this@CouponActivity, response.message.toString())
                    }
                }
            }
    }

    private fun setListData(response: NetworkResult.Success<CouponModel>) {
        try {

            binding.couponList.layoutManager = LinearLayoutManager(this@CouponActivity)
            binding.couponList.adapter = CouponListAdapter(this@CouponActivity, this, response.data!!.promoCodeList, promoId, amount,supportFragmentManager)
            Utility.listAnimation(binding.couponList)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun applyCoupon(couponId: String, apply_offer: TextView) {

        // apply coupon api
        if (Utility.isAppOnLine(this@CouponActivity, object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    applyCouponRequest(couponId, apply_offer)
                }
            })) {
            applyCouponRequest(couponId,apply_offer)
        }
    }

    private fun applyCouponRequest(couponId: String, apply_offer: TextView) {
        // apply coupon api
        apply_offer.isEnabled = false
        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty(Constant.promoCodeId, couponId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewModel.applyCouponRequest(app.user.getUserDetails()?.userId, jsonObject)
            .observe(this) { model ->
                if (model.data?.status == true) {
                    val returnIntent = Intent()
                    returnIntent.putExtra("couponCode", "Code " + model.data.codeName + " applied!")
                    returnIntent.putExtra("saveAmountText", model.data.offer)
                    returnIntent.putExtra("promoCodeId", model.data.promoCodeId)
                    returnIntent.putExtra("Couponprice", model.data.promoCodeDiscountAmount)
                    returnIntent.putExtra("codeName", model.data.codeName)
                    returnIntent.putParcelableArrayListExtra("productlist",model.data.updatedCartList)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }else{
                        Toast.makeText(this@CouponActivity, model.data?.msg, Toast.LENGTH_SHORT).show()
                }
            }
    }
}