package naya.ganj.app.data.sidemenu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import naya.ganj.app.databinding.ProductDetailAdapterRowBinding
import com.squareup.picasso.Picasso

class ProductDetailListAdapter(val products: List<OrderDetailModel.OrderDetails.Product>) :
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

        Picasso.get().load(productItem.img).into(holder.binding.ivImagview)
        val productname = productItem.productName.split("$")
        holder.binding.tvProductTitle.text = productname[0]
        holder.binding.tvUnit.text =
            productItem.variantUnitQuantity.toString() + productItem.variantUnit
        holder.binding.tvPrice.text = productItem.price.toString()
        holder.binding.tvSaveAmount.text = productItem.discount.toString() + "% off"
        holder.binding.itemCount.text = productItem.quantity.toString()
    }

}