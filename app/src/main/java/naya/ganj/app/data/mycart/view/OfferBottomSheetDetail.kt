package naya.ganj.app.data.mycart.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import naya.ganj.app.R
import naya.ganj.app.data.mycart.adapter.CouponListAdapter
import naya.ganj.app.data.mycart.adapter.OfferDetailAdapter
import naya.ganj.app.data.mycart.model.CouponModel

class OfferBottomSheetDetail(

    val isApplyOfferVisible: Boolean,
    val promoCode: CouponModel.PromoCode,
    val promoCodeId: String,
    val applyCouponInterface: CouponListAdapter.ApplyCouponInterface
) : BottomSheetDialogFragment() {

    var recyclerView: RecyclerView? = null
    private var tvName: TextView? = null
    var tvOffer: TextView? = null
    var tvDesription: TextView? = null
    var couponLayout: LinearLayout? = null
    var tvApplyOffer: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.coupon_item_layout, container, false)

        recyclerView = view.findViewById(R.id.rv_coupon_detail_list)
        tvName = view.findViewById(R.id.tv_name)
        tvOffer = view.findViewById(R.id.tv_offer)
        tvDesription = view.findViewById(R.id.tv_description)
        couponLayout = view.findViewById(R.id.coupon_details_layout)
        couponLayout?.visibility = View.VISIBLE
        tvApplyOffer = view.findViewById(R.id.tv_apply)

        if(promoCodeId == promoCode.id)
        {
            tvApplyOffer?.text = "APPLIED"
            tvApplyOffer?.setTextColor(ContextCompat.getColor(context!!, R.color.green_color))
            tvApplyOffer?.isEnabled=false
        }else{
            tvApplyOffer?.text = "APPLY"
            tvApplyOffer?.setTextColor(ContextCompat.getColor(context!!, R.color.red_color))
            tvApplyOffer?.isEnabled=true
        }


        if (isApplyOfferVisible) {
            tvApplyOffer?.visibility = View.VISIBLE
        }

        tvApplyOffer?.setOnClickListener{
            applyCouponInterface.applyCoupon(promoCode.id, tvApplyOffer!!)
            dismiss()
        }

        return view
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName?.text=promoCode.name
        tvOffer?.text=promoCode.offer
        tvDesription?.text=promoCode.description
        recyclerView?.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView?.adapter=OfferDetailAdapter(promoCode.details)
        recyclerView?.visibility = View.VISIBLE

        couponLayout?.setOnClickListener { dismiss() }

    }

}