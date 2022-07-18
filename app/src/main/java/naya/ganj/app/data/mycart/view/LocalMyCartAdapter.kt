package naya.ganj.app.data.mycart.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import naya.ganj.app.databinding.AdapterLocalMycartLayoutBinding
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail

class LocalMyCartAdapter(
    val context: Context,
    private val listOfProduct: MutableList<ProductDetail>
) :
    RecyclerView.Adapter<LocalMyCartAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: AdapterLocalMycartLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        return MyViewHolder(
            AdapterLocalMycartLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = listOfProduct.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val productDetail = listOfProduct[position]
        Glide.with(context).load(productDetail.imageUrl).into(holder.binding.ivImageview)
        val price = productDetail.vPrice
        val discountPrice = (price - (price * productDetail.vDiscount) / 100)
        holder.binding.tvProductTitle.text = productDetail.productName.split("$")[0]
        holder.binding.tvPrice.text = price.toString()
        holder.binding.tvDiscountPrice.text = discountPrice.toString()
        holder.binding.tvUnitQuantity.text =
            productDetail.vUnitQuantity.toString() + productDetail.vUnit

        holder.binding.tvQuantity.text = productDetail.itemQuantity.toString()

        holder.binding.tvPlus.setOnClickListener {
            var itemQuantity = holder.binding.tvQuantity.text.toString().toInt()
            itemQuantity++
            holder.binding.tvQuantity.text = itemQuantity.toString()
            Thread {
                AppDataBase.getInstance(context).productDao()
                    .updateProduct(itemQuantity, productDetail.productId, productDetail.variantId)
            }.start()
        }
        holder.binding.tvMinus.setOnClickListener {
            var itemQuantity = holder.binding.tvQuantity.text.toString().toInt()
            itemQuantity--
            holder.binding.tvQuantity.text = itemQuantity.toString()
            if (itemQuantity == 0) {
                listOfProduct.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                Thread {
                    AppDataBase.getInstance(context).productDao()
                        .deleteProduct(productDetail.productId, productDetail.variantId)
                }.start()
                return@setOnClickListener
            }
            Thread {
                AppDataBase.getInstance(context).productDao()
                    .updateProduct(itemQuantity, productDetail.productId, productDetail.variantId)
            }.start()
        }
    }
}