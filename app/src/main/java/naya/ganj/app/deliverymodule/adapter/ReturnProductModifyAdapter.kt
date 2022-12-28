package naya.ganj.app.deliverymodule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.ReturnProductAdapterLayoutBinding
import naya.ganj.app.deliverymodule.model.Product
import naya.ganj.app.interfaces.ProductSelectListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ReturnProduct

class ReturnProductModifyAdapter(
    val context: Context,
    val app: Nayaganj,
    val productSelectListener: ProductSelectListener,
    val productList: List<Product>?
): RecyclerView.Adapter<ReturnProductModifyAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: ReturnProductAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            ReturnProductAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productList?.size ?: 0
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productList!!.get(position)
        Picasso.get().load(app.user.getUserDetails()?.configObj?.productImgUrl + product.img)
            .error(R.drawable.default_image).into(holder.binding.ivImagview)
        holder.binding.tvProductTitle.text = product.productName
        holder.binding.tvUnit.text =product.variantUnitQuantity.toString() +" "+product.variantUnit
        holder.binding.tvQuantity.text = product.quantity.toString()
        holder.binding.tvCount.text = product.quantity.toString()
        holder.binding.tvAmount.text = context.resources.getString(R.string.Rs) + product.price.toString()

        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                if(product.quantity>1){
                    holder.binding.llPlusMinusLayout.visibility= View.VISIBLE
                    val returnProduct= ReturnProduct(product.productId,product.variantId,product.quantity)
                    Thread {
                        AppDataBase.getInstance(context).productDao()
                            .insertReturnProduct(returnProduct)
                    }.start()
                    holder.binding.tvQuantity.text = product.quantity.toString()
                }else{
                    // if Quantity is One
                    holder.binding.llPlusMinusLayout.visibility= View.GONE
                    val returnProduct= ReturnProduct(product.productId,product.variantId,1)
                    Thread {
                        AppDataBase.getInstance(context).productDao()
                            .insertReturnProduct(returnProduct)
                    }.start()
                }
            }else{
                holder.binding.llPlusMinusLayout.visibility= View.GONE
                Thread {
                    AppDataBase.getInstance(context).productDao()
                        .deleteReturnProduct(product.productId, product.variantId)
                }.start()
            }
        }

        holder.binding.tvMinus.setOnClickListener {
            var  itemQuantity=holder.binding.tvQuantity.text.toString().toInt()
            if(itemQuantity==1){
                return@setOnClickListener
            }
            itemQuantity--
            holder.binding.tvQuantity.text = itemQuantity.toString()
            productSelectListener.onSelectProduct(product.productId,product.variantId,itemQuantity)
        }

        holder.binding.tvPlus.setOnClickListener {
            var  itemQuantity=holder.binding.tvQuantity.text.toString().toInt()
            if(itemQuantity==product.quantity){
                Toast.makeText(context,"Sorry you can not add more quantity of this product!",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            itemQuantity++
            holder.binding.tvQuantity.text = itemQuantity.toString()
            productSelectListener.onSelectProduct(product.productId,product.variantId,itemQuantity)
        }
    }
}