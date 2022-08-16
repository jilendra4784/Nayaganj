package naya.ganj.app.deliverymodule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

import naya.ganj.app.databinding.ProductDetailAdapterRowBinding
import naya.ganj.app.deliverymodule.model.DeliveryOrderDetailModel


class DeliveredOrderDetailAdapter(val products: List<DeliveryOrderDetailModel.OrderDetails.Product>) :
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
        val productItem: DeliveryOrderDetailModel.OrderDetails.Product = products.get(position)

        Picasso.get().load(productItem.img).into(holder.binding.ivImagview)
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