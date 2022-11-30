package naya.ganj.app.data.sidemenu.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import naya.ganj.app.databinding.ProductDetailAdapterRowBinding
import naya.ganj.app.utility.Utility

class ProductDetailListAdapter(val context: Context,val products: List<OrderDetailModel.OrderDetails.Product>,val app:Nayaganj) :
    RecyclerView.Adapter<ProductDetailListAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: ProductDetailAdapterRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return products.size
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            ProductDetailAdapterRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val productItem: OrderDetailModel.OrderDetails.Product = products.get(position)
        if (app.user.getAppLanguage() == 1) {
            holder.binding.tvNetWet.text = context.resources.getString(R.string.net_weight_h)
            try {
                holder.binding.tvProductTitle.setTypeface(
                    Typeface.createFromAsset(
                        context.assets,
                        "agrawide.ttf"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            holder.binding.tvProductTitle.textSize =
                context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
            holder.binding.tvNetWet.textSize =
                context.resources.getDimension(com.intuit.sdp.R.dimen._8sdp)
        }
        try {
            if (productItem.img != "") {
                val imgURL = app.user.getUserDetails()?.configObj?.productImgUrl + productItem.img
                Picasso.get().load(imgURL).error(R.drawable.default_image)
                    .into(holder.binding.ivImagview)
            } else {
                holder.binding.ivImagview.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.default_image
                    )
                )
            }

        } catch (e: Exception) {

        }
        holder.binding.tvProductTitle.text = Utility.convertLanguage(productItem.productName, app)
        holder.binding.tvUnit.text =
            productItem.variantUnitQuantity.toString() + " " + productItem.variantUnit
        holder.binding.tvPrice.text = productItem.price.toString()
        holder.binding.tvSaveAmount.text = productItem.discount.toString() + "% off"
        holder.binding.itemCount.text = productItem.quantity.toString()
    }

}