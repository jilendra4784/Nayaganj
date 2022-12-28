package naya.ganj.app.deliverymodule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import naya.ganj.app.Nayaganj

import naya.ganj.app.databinding.ProductDetailAdapterRowBinding
import naya.ganj.app.deliverymodule.model.DeliveryOrderDetailModel
import naya.ganj.app.deliverymodule.model.Product


class DeliveredOrderDetailAdapter(val app:Nayaganj,val products: List<Product>) :
    RecyclerView.Adapter<DeliveredOrderDetailAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: ProductDetailAdapterRowBinding) :
        RecyclerView.ViewHolder(binding.root)

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
        val productItem: Product = products.get(position)

        val imgURL=app.user.getUserDetails()?.configObj?.productImgUrl+productItem.img
        Picasso.get().load(imgURL).into(holder.binding.ivImagview)
        val productname = productItem.productName.split("$")
        holder.binding.tvProductTitle.text = productname[0]
        holder.binding.tvUnit.text =
            productItem.variantUnitQuantity.toString() + productItem.variantUnit
        holder.binding.tvPrice.text = productItem.price.toString()
        holder.binding.tvSaveAmount.text = productItem.discount.toString() + "% off"
        holder.binding.tvSaveAmount.visibility = View.GONE
        holder.binding.itemCount.text = productItem.quantity.toString()
    }

    override fun getItemCount(): Int {
       return products.size
    }
}