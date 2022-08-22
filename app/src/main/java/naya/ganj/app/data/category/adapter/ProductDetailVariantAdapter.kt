package naya.ganj.app.data.category.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.model.ProductDetailModel
import naya.ganj.app.databinding.CustomVariantAdapterRowBinding
import naya.ganj.app.interfaces.OnitemClickListener


class ProductDetailVariantAdapter (
    private val variantList: List<ProductDetailModel.ProductDetails.Variant>,
    private var onitemClickListener: OnitemClickListener,val app:Nayaganj,
    val context: Context
) :
    RecyclerView.Adapter<ProductDetailVariantAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: CustomVariantAdapterRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            CustomVariantAdapterRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try {
            holder.binding.tvQuantity.text =
                variantList.get(position).vUnitQuantity.toString() + " " + variantList.get(
                    position
                ).vUnit
            holder.binding.tvPrice.text = variantList.get(position).vPrice.toString()

            if(app.user.getAppLanguage()==1){
                holder.binding.tvDiscountPercent.text ="(" + variantList.get(position).vDiscount + "%"+ context.resources.getString(
                    R.string.off_h) +")"
            }else{
                holder.binding.tvDiscountPercent.text= "(" + variantList.get(position).vDiscount + "%" + " off )"
            }

            holder.itemView.setOnClickListener {
                onitemClickListener.onclick(holder.adapterPosition, "")
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return variantList.size
    }
}