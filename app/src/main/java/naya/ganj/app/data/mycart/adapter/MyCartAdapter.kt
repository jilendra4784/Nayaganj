package naya.ganj.app.data.mycart.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.databinding.MycartAdapterLayoutBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.CartModel
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.roomdb.entity.SavedAmountModel
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyCartAdapter(
    var context: Context,
    private val cartList: MutableList<MyCartModel.Cart>,
    private val addOremoveItemListener: OnclickAddOremoveItemListener,
    private val activity: Activity,
    val app: Nayaganj
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
            setSavedAmount(cart.productId, cart.variantId, cart.discountPrice)
        } else {
            holder.binding.tvSaveAmount.visibility = View.GONE
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
            val itemSavedAmount = (priceAmount - discountAmount)
            holder.binding.tvSaveAmount.text =
                "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                    itemSavedAmount
                ).toString()
            Thread {
                AppDataBase.getInstance(context).productDao()
                    .updateSavedAmount(cart.productId, cart.variantId.toInt(), itemSavedAmount)
            }.start()

            addOremoveItemListener.onClickAddOrRemoveItem(
                Constant.ADD,
                ProductDetail(cart.productId, cart.variantId, quantity, "", "", 0.0, 0, 0, "", 0)
            )

            if (app.user.getLoginSession()) {
                Thread {
                    AppDataBase.getInstance(context).productDao()
                        .updateCart(cart.productId, cart.variantId, discountAmount)
                }.start()
            }
        }
        holder.binding.tvMinus.setOnClickListener {
            var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
            quantity--

            if (quantity == 0) {
                cartList.remove(cart)
                notifyItemRemoved(holder.adapterPosition)
                notifyItemRangeChanged(position, cartList.size)

                addOremoveItemListener.onClickAddOrRemoveItem(
                    Constant.REMOVE,
                    ProductDetail(
                        cart.productId,
                        cart.variantId,
                        quantity,
                        "",
                        "",
                        0.0,
                        0,
                        0,
                        "",
                        0
                    )
                )
                Thread {
                    AppDataBase.getInstance(context).productDao()
                        .deleteSavedAmount(cart.productId, cart.variantId.toInt())
                }.start()

                if (app.user.getLoginSession()) {
                    Thread {
                        AppDataBase.getInstance(context).productDao()
                            .deleteCartItem(cart.productId, cart.variantId)
                    }.start()
                }

            } else {
                holder.binding.tvQuantity.text = quantity.toString()
                val priceAmount = (cart.price / cart.quantity) * quantity
                val discountAmount = (cart.actualPrice.toDouble() / cart.quantity) * quantity
                holder.binding.tvPrice.text =
                    Utility().formatTotalAmount(priceAmount.toDouble()).toString()
                holder.binding.tvDiscountPrice.text =
                    Utility().formatTotalAmount(discountAmount).toString()
                val itemSavedAmount = (priceAmount - discountAmount)
                holder.binding.tvSaveAmount.text =
                    "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                        itemSavedAmount
                    ).toString()

                Thread {
                    AppDataBase.getInstance(context).productDao()
                        .updateSavedAmount(cart.productId, cart.variantId.toInt(), itemSavedAmount)
                }.start()

                addOremoveItemListener.onClickAddOrRemoveItem(
                    Constant.REMOVE,
                    ProductDetail(
                        cart.productId,
                        cart.variantId,
                        quantity,
                        "",
                        "",
                        0.0,
                        0,
                        0,
                        "",
                        0
                    )
                )
                if (app.user.getLoginSession()) {
                    Thread {
                        AppDataBase.getInstance(context).productDao()
                            .updateCart(cart.productId, cart.variantId, discountAmount)
                    }.start()
                }
            }
        }
        holder.binding.ivDelete.setOnClickListener {
            itemDeleteDialog(holder, cart)
        }
        setListData(cart, holder.binding)

        // Set Cart Amount
        setCartAmountList(cart)
    }

    private fun setCartAmountList(cart: MyCartModel.Cart) {
        if (app.user.getLoginSession()) {
            Thread {
                val isCartItemExist = AppDataBase.getInstance(context).productDao()
                    .isCartItemIsExist(cart.productId, cart.variantId.toInt())
                if (isCartItemExist) {
                    AppDataBase.getInstance(context).productDao()
                        .updateCart(cart.productId, cart.variantId, cart.actualPrice.toDouble())
                } else {
                    AppDataBase.getInstance(context).productDao().insertCartDetail(
                        CartModel(
                            cart.productId,
                            cart.variantId,
                            cart.quantity,
                            cart.actualPrice.toDouble()
                        )
                    )
                }

            }.start()
        }
    }

    private fun setListData(
        cart: MyCartModel.Cart,
        holder: MycartAdapterLayoutBinding,
    ) {

        Picasso.get().load(cart.img).into(holder.ivImagview)
        val productName = cart.productName.split("$")
        holder.tvProductTitle.text = productName[0]
        holder.tvPrice.text = cart.price.toString()
        holder.tvDiscountPrice.text = cart.actualPrice
        holder.tvUnit.text = cart.variantUnitQuantity.toString() + cart.variantUnit
        holder.tvQuantity.text = cart.quantity.toString()
        val discountPrice: Int =
            ((cart.discountPrice.toDouble() * 100) / cart.price).toInt()

        Thread {
            val isProductExist = AppDataBase.getInstance(context).productDao()
                .isProductExist(cart.productId, cart.variantId)
            if (isProductExist) {
                val singleProduct = AppDataBase.getInstance(context).productDao()
                    .getSingleProduct(cart.productId, cart.variantId)
                val price = cart.price / cart.quantity
                Thread {
                    AppDataBase.getInstance(context).productDao().syncCart(
                        singleProduct.itemQuantity,
                        cart.productName,
                        cart.img,
                        price.toDouble(),
                        discountPrice,
                        cart.variantUnitQuantity,
                        cart.variantUnit,
                        cart.variantQuantity,
                        cart.productId,
                        cart.variantId,
                    )
                }.start()

                activity.runOnUiThread {
                    holder.tvQuantity.text = singleProduct.itemQuantity.toString()
                }
            } else {
                val price = cart.price / cart.quantity
                Utility().insertProduct(
                    context, ProductDetail(
                        cart.productId,
                        cart.variantId,
                        cart.quantity,
                        cart.productName,
                        cart.img,
                        price.toDouble(),
                        discountPrice,
                        cart.variantUnitQuantity,
                        cart.variantUnit,
                        cart.variantQuantity
                    )
                )
            }

        }.start()
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

    private fun itemDeleteDialog(holder: MyViewHolder, productDetail: MyCartModel.Cart) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Delete Item? ")
            .setMessage("Are you sure, you want to delete this product?")
            .setPositiveButton(
                "Yes"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()

                val jsonObject = JsonObject()
                jsonObject.addProperty(Constant.PRODUCT_ID, productDetail.productId)
                jsonObject.addProperty(Constant.VARIANT_ID, productDetail.variantId)
                jsonObject.addProperty(Constant.promoCodeId, "")

                RetrofitClient.instance.removeProduct(
                    app.user.getUserDetails()?.userId,
                    Constant.DEVICE_TYPE,
                    jsonObject
                )
                    .enqueue(object : Callback<ApiResponseModel> {
                        override fun onResponse(
                            call: Call<ApiResponseModel>,
                            response: Response<ApiResponseModel>
                        ) {
                            if (response.body()!!.status) {

                                Thread {
                                    AppDataBase.getInstance(context).productDao()
                                        .deleteProduct(
                                            productDetail.productId,
                                            productDetail.variantId
                                        )
                                    AppDataBase.getInstance(context).productDao()
                                        .deleteSavedAmount(
                                            productDetail.productId,
                                            productDetail.variantId.toInt()
                                        )
                                    AppDataBase.getInstance(context).productDao()
                                        .deleteCartItem(
                                            productDetail.productId,
                                            productDetail.variantId
                                        )
                                }.start()

                                cartList.removeAt(holder.adapterPosition)
                                notifyItemRemoved(holder.adapterPosition)

                                // Refresh List
                                addOremoveItemListener.onClickAddOrRemoveItem(
                                    "",
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
                                    )
                                )
                            }
                        }

                        override fun onFailure(call: Call<ApiResponseModel>, t: Throwable) {
                            Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })


            }
            .setNegativeButton(
                "NO"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
    }

}