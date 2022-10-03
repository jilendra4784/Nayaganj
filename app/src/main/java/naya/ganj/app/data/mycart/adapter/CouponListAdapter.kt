package naya.ganj.app.data.mycart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.R
import naya.ganj.app.data.mycart.adapter.CouponListAdapter.MyViewHolder
import naya.ganj.app.data.mycart.model.CouponModel
import naya.ganj.app.data.mycart.view.OfferBottomSheetDetail
import naya.ganj.app.databinding.CouponItemLayoutBinding

class CouponListAdapter(
    val context: Context,
    val applyCouponInterface: ApplyCouponInterface,
    private val couponList: List<CouponModel.PromoCode>,
    val promoCodeId: String,
    val amount: Double,
   val supportFragmentManager: FragmentManager,

) :
    RecyclerView.Adapter<MyViewHolder>() {
    var isApplyOfferVisible=false

    interface  ApplyCouponInterface
    {
        fun applyCoupon(couponId:String,apply_offer: TextView)
    }

    class MyViewHolder(val binding: CouponItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return couponList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            CouponItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item: CouponModel.PromoCode = couponList[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvOffer.text = item.offer
        holder.binding.tvDescription.text = item.description
        holder.binding.tvCouponCode.text = item.codeName
        holder.binding.tvViewDetail.visibility=View.VISIBLE

        if (amount >= item.min) {
            holder.binding.tvApply.visibility=View.VISIBLE
            isApplyOfferVisible=true
        }else{
            holder.binding.tvApply.visibility=View.GONE
            isApplyOfferVisible=false
        }
        if(promoCodeId == item.id)
        {
            holder.binding.tvApply.text = "APPLIED"
            holder.binding.tvApply.setTextColor(ContextCompat.getColor(context, R.color.green_color))
            holder.binding.tvApply.isEnabled=false
        }
        holder.binding.tvApply.setOnClickListener{
            applyCouponInterface.applyCoupon(item.id,holder.binding.tvApply)
        }

        holder.binding.tvViewDetail.setOnClickListener{

            val modalBottomSheet = OfferBottomSheetDetail(isApplyOfferVisible,couponList[position])
            modalBottomSheet.show(supportFragmentManager, OfferBottomSheetDetail.TAG)
        }
    }

}