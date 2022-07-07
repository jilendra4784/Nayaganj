package naya.ganj.app.data.mycart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.R
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.databinding.MycartAdapterLayoutBinding
import naya.ganj.app.interfaces.OnSavedAmountListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.utility.Utility
import com.squareup.picasso.Picasso

class MyCartAdapter(
    var context: Context,
    private val cartList: MutableList<MyCartModel.Cart>,
    private val addOremoveItemListener: OnclickAddOremoveItemListener,
    private val listener: OnSavedAmountListener
) :
    RecyclerView.Adapter<MyCartAdapter.MyViewHolder>() {


    override fun getItemCount(): Int {
        return cartList.size
    }

    class MyViewHolder(val binding: MycartAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            MycartAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cart = cartList.get(position)
        if (cart.discountPrice.toDouble() > 0) {
            holder.binding.tvSaveAmount.text =
                "SAVED " + context.resources.getString(R.string.Rs) + cart.discountPrice
            holder.binding.tvSaveAmount.visibility = View.VISIBLE
        }

        holder.binding.tvPlus.setOnClickListener {
            var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
            quantity++

            holder.binding.tvQuantity.text = quantity.toString()
            val priceAmount = (cart.price / cart.quantity) * quantity
            val discountAmount = (cart.actualPrice.toDouble() / cart.quantity) * quantity

            holder.binding.tvPrice.text =
                Utility().formatTotalAmount(priceAmount.toDouble()).toString()
            holder.binding.tvDiscountPrice.text =
                Utility().formatTotalAmount(discountAmount).toString()

            addOremoveItemListener.onClickAddOrRemoveItem(
                "add",
                cart.productId,
                cart.variantId,
                "",
                holder.binding.tvDiscountPrice.text.toString().toDouble()
            )

            val itemSavedAmount = (priceAmount - discountAmount)
            holder.binding.tvSaveAmount.text =
                "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                    itemSavedAmount
                ).toString()

            listener.onSavedAmount(cart.productId, cart.variantId.toInt(), itemSavedAmount)
        }
        holder.binding.tvMinus.setOnClickListener {
            var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
            quantity--

            if (quantity == 0) {
                addOremoveItemListener.onClickAddOrRemoveItem(
                    "remove",
                    cart.productId,
                    cart.variantId,
                    "",
                    0.0
                )
                cartList.remove(cart)
                notifyItemRemoved(holder.adapterPosition)
                notifyItemRangeChanged(position, cartList.size)

                // Deletting Saved Amount
                listener.onSavedAmount(cart.productId, cart.variantId.toInt(), 0.0)

            } else {
                holder.binding.tvQuantity.text = quantity.toString()
                val priceAmount = (cart.price / cart.quantity) * quantity
                val discountAmount = (cart.actualPrice.toDouble() / cart.quantity) * quantity

                holder.binding.tvPrice.text =
                    Utility().formatTotalAmount(priceAmount.toDouble()).toString()
                holder.binding.tvDiscountPrice.text =
                    Utility().formatTotalAmount(discountAmount).toString()
                addOremoveItemListener.onClickAddOrRemoveItem(
                    "remove",
                    cart.productId,
                    cart.variantId,
                    "",
                    holder.binding.tvDiscountPrice.text.toString().toDouble()
                )
                val itemSavedAmount = (priceAmount - discountAmount)
                holder.binding.tvSaveAmount.text =
                    "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                        itemSavedAmount
                    ).toString()
                listener.onSavedAmount(cart.productId, cart.variantId.toInt(), itemSavedAmount)
            }
        }
        setCardData(cart, holder.binding)
    }

    private fun setCardData(
        cart: MyCartModel.Cart,
        holder: MycartAdapterLayoutBinding,
    ) {
        Picasso.get().load(cart.img).into(holder.ivImagview)
        val productname = cart.productName.split("$")
        holder.tvProductTitle.text = productname[0]
        holder.tvUnit.text = cart.variantUnitQuantity.toString() + cart.variantUnit
        holder.tvPrice.text = cart.price.toString()
        holder.tvDiscountPrice.text = cart.actualPrice
        holder.tvQuantity.text = cart.quantity.toString()
    }

    private fun setSavedAmount(
        holder: MyViewHolder,
        quantity: Int,
        action: String,
        itemPosition: Int
    ) {
        /* val savedAmount = holder.binding.tvSaveAmount.text.toString().substring(7)
             .toDouble() / quantity
         // Insert Amount
         if (quantity != 1) {
             if (action == "P") {
                 listener.onclick(itemPosition, savedAmount.toString())
             } else {
                 listener.onclick(itemPosition, savedAmount.toString())
             }

         } else {
             Thread(Runnable {
                 AppDataBase.INSTANCE?.productDao()?.deleteAmount(itemPosition)
             }).start()
         }
         listener.onclick(itemPosition, savedAmount.toString())*/
    }
}