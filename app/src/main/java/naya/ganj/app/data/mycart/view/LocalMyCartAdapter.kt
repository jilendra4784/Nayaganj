package naya.ganj.app.data.mycart.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import naya.ganj.app.R
import naya.ganj.app.databinding.AdapterLocalMycartLayoutBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.roomdb.entity.SavedAmountModel
import naya.ganj.app.utility.Utility

class LocalMyCartAdapter(
    val context: Context,
    private val listOfProduct: MutableList<ProductDetail>,
    val listener: OnclickAddOremoveItemListener
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

        val productPrice = productDetail.vPrice * productDetail.itemQuantity
        val productDiscountPrice =
            (productPrice - (productPrice * productDetail.vDiscount) / 100)

        holder.binding.tvProductTitle.text = productDetail.productName.split("$")[0]
        holder.binding.tvUnitQuantity.text =
            productDetail.vUnitQuantity.toString() + productDetail.vUnit
        holder.binding.tvQuantity.text = productDetail.itemQuantity.toString()

        holder.binding.tvOff.text =
            "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                productPrice - productDiscountPrice
            )
        setSavedAmount(
            productDetail.productId,
            productDetail.variantId,
            (productPrice - productDiscountPrice).toString()
        )

        holder.binding.tvPrice.text = Utility().formatTotalAmount(productPrice).toString()
        holder.binding.tvDiscountPrice.text =
            Utility().formatTotalAmount(productDiscountPrice).toString()


        holder.binding.tvPlus.setOnClickListener {
            var itemQuantity = holder.binding.tvQuantity.text.toString().toInt()
            itemQuantity++
            holder.binding.tvQuantity.text = itemQuantity.toString()

            val price = productDetail.vPrice
            val discountPrice = (price - (price * productDetail.vDiscount) / 100)

            holder.binding.tvPrice.text =
                Utility().formatTotalAmount(price * itemQuantity).toString()
            holder.binding.tvDiscountPrice.text =
                Utility().formatTotalAmount(discountPrice * itemQuantity).toString()

            holder.binding.tvOff.text =
                "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                    (price - discountPrice) * itemQuantity
                )

            setSavedAmount(
                productDetail.productId,
                productDetail.variantId,
                ((price - discountPrice) * itemQuantity).toString()
            )
            Thread {
                AppDataBase.getInstance(context).productDao()
                    .updateProduct(itemQuantity, productDetail.productId, productDetail.variantId)
            }.start()
            listener.onClickAddOrRemoveItem("", productDetail,holder.binding.tvPlus)

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

                Thread {
                    AppDataBase.getInstance(context).productDao()
                        .deleteSavedAmount(productDetail.productId, productDetail.variantId.toInt())
                }.start()
                listener.onClickAddOrRemoveItem("LOCAL_CART", productDetail,holder.binding.tvMinus)
                return@setOnClickListener
            }

            val price = productDetail.vPrice
            val discountPrice = (price - (price * productDetail.vDiscount) / 100)

            holder.binding.tvPrice.text =
                Utility().formatTotalAmount(price * itemQuantity).toString()
            holder.binding.tvDiscountPrice.text =
                Utility().formatTotalAmount(discountPrice * itemQuantity).toString()

            holder.binding.tvOff.text =
                "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                    (price - discountPrice) * itemQuantity
                )

            setSavedAmount(
                productDetail.productId,
                productDetail.variantId,
                ((price - discountPrice) * itemQuantity).toString()
            )

            Thread {
                AppDataBase.getInstance(context).productDao()
                    .updateProduct(itemQuantity, productDetail.productId, productDetail.variantId)
            }.start()

            listener.onClickAddOrRemoveItem("", productDetail,holder.binding.tvMinus)
        }
        holder.binding.ivDelete.setOnClickListener {
            itemDeleteDialog(holder, productDetail)
        }

    }

    private fun setSavedAmount(
        productId: String,
        variantId: String,
        amount: String,
    ) {
        Thread {
            val isSavedAmountExist = AppDataBase.getInstance(context).productDao()
                .isSavedItemIsExist(productId, variantId.toInt())
            if (isSavedAmountExist) {
                AppDataBase.getInstance(context).productDao().updateSavedAmount(
                    productId,
                    variantId.toInt(),
                    amount.toDouble()
                )
            } else {
                AppDataBase.getInstance(context).productDao().insertSavedAmount(
                    SavedAmountModel(
                        productId,
                        variantId.toInt(),
                        amount.toDouble()
                    )
                )
            }

        }.start()
    }


    private fun itemDeleteDialog(holder: MyViewHolder, productDetail: ProductDetail) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Delete Item? ")
            .setMessage("Are you sure, you want to delete this product?")
            .setPositiveButton(
                "Yes"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
                Thread {
                    AppDataBase.getInstance(context).productDao()
                        .deleteProduct(productDetail.productId, productDetail.variantId)
                    AppDataBase.getInstance(context).productDao()
                        .deleteSavedAmount(productDetail.productId, productDetail.variantId.toInt())
                    AppDataBase.getInstance(context).productDao()
                        .deleteCartItem(productDetail.productId, productDetail.variantId)
                }.start()
                listOfProduct.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                listener.onClickAddOrRemoveItem(
                    "Refresh List",
                    ProductDetail(
                        productDetail.productId,
                        productDetail.variantId,
                        0,
                        "",
                        "",
                        0.0,
                        0,
                        0,
                        "",
                        0
                    ),holder.binding.tvMinus
                )
            }
            .setNegativeButton(
                "NO"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
    }
}