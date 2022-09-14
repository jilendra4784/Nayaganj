package naya.ganj.app.data.mycart.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoupenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = applicationContext as Nayaganj
        amount = intent.getStringExtra(Constant.cartAmount)!!.toDouble()
        if (app.user.getAppLanguage() == 1) {
            // set Hindi Labels
        } else {
            binding.includeLayout.toolbarTitle.text = "Available Coupons"
            binding.includeLayout.ivBackArrow.setOnClickListener { finish() }
        }
        Log.e("TAG", "onCreate: " + amount)

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
            binding.couponList.adapter = CouponListAdapter(
                this@CouponActivity,
                this,
                response.data!!.promoCodeList,
                "",
                amount
            )

            binding.couponList.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        binding.couponList.viewTreeObserver.removeOnPreDrawListener(this)
                        for (i in 0 until binding.couponList.childCount) {
                            val v: View = binding.couponList.getChildAt(i)
                            v.alpha = 0.0f
                            v.animate().alpha(1.0f)
                                .setDuration(300)
                                .setStartDelay((i * 50).toLong())
                                .start()
                        }
                        return true
                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun applyCoupon(couponId: String, apply_offer: TextView) {
        // apply coupon api
        if (Utility.isAppOnLine(this@CouponActivity, object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    applyCouponRequest()
                }
            })) {
            applyCouponRequest()
        }
    }

    private fun applyCouponRequest() {
        // apply coupon api

    }
}