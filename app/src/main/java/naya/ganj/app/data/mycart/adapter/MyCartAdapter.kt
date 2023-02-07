package naya.ganj.app.data.mycart.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.mycart.model.MyCartModel
import naya.ganj.app.data.mycart.model.UpdatedCart
import naya.ganj.app.databinding.MycartAdapterLayoutBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.CartModel
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.roomdb.entity.SavedAmountModel
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import java.text.DecimalFormat


class MyCartAdapter(
    var context: Context,
    private val cartList: MutableList<MyCartModel.Cart>,
    var couponList: ArrayList<UpdatedCart>,
    private val addOremoveItemListener: OnclickAddOremoveItemListener,
    private val activity: Activity,
    val app: Nayaganj,
    val promoId: String,
    val removeProductListener: RemoveProduct
) :
    RecyclerView.Adapter<MyCartAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return cartList.size
    }

    interface RemoveProduct {
        fun removeProductFromList(
            cartList: MutableList<MyCartModel.Cart>,
            position: Int,
            pId: String,
            vId: String,
            promoCode: String
        )
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
        val cart = cartList[position]
        for (i in 0 until couponList.size) {
            if (cart.productId.equals(couponList.get(i).productId) && cart.variantId.equals(
                    couponList.get(i).variantId
                )
            ) {
                if (couponList[i].actualPriceAfterPromoCode <= 0) {
                    holder.binding.afterCouponApplyLayout.visibility = View.VISIBLE
                    holder.binding.afterCouponApplyPrice.visibility = View.GONE
                    holder.binding.couponNotApplied.visibility = View.VISIBLE
                    holder.binding.couponNotApplied.setText("Coupon not applicable on this product")
                } else {
                    holder.binding.afterCouponApplyLayout.visibility = View.VISIBLE
                    holder.binding.couponNotApplied.visibility = View.VISIBLE
                    if (Utility.equalsToZero(couponList.get(i).actualPriceAfterPromoCode.toString())) {
                        holder.binding.afterCouponApplyPrice.text =
                            "₹" + DecimalFormat("#").format((couponList.get(i).actualPriceAfterPromoCode))
                    } else {
                        holder.binding.afterCouponApplyPrice.text =
                            "₹" + DecimalFormat("##.##").format((couponList.get(i).actualPriceAfterPromoCode))
                    }
                }
            }
        }

        if (couponList.size == 0) {
            holder.binding.afterCouponApplyLayout.visibility = View.GONE
        }

        if (cart.discountPrice != null && cart.discountPrice.toDouble() > 0) {
            if (cart.discountPrice.toDouble() == cart.price.toDouble()) {
                holder.binding.tvRupeeSmall.visibility = View.GONE
                holder.binding.tvPrice.visibility = View.GONE
            }
            if (app.user.getAppLanguage() == 1) {
                holder.binding.tvSaveAmount.text =
                    context.resources.getString(R.string.saved_h) + " " + context.resources.getString(
                        R.string.Rs
                    ) + cart.discountPrice
            } else {
                holder.binding.tvSaveAmount.text =
                    "SAVED " + context.resources.getString(R.string.Rs) + cart.discountPrice
            }

            holder.binding.tvSaveAmount.visibility = View.VISIBLE
            setSavedAmount(cart.productId, cart.variantId, cart.discountPrice)
        } else {
            holder.binding.tvSaveAmount.visibility = View.GONE
            holder.binding.tvRupeeSmall.visibility = View.GONE
            holder.binding.tvPrice.visibility = View.GONE
        }

        holder.binding.tvPlus.setOnClickListener {
            holder.binding.tvPlus.isEnabled = false
            if (Utility.isAppOnLine(context, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {

                    }
                })) {
                var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()

                if (quantity >= cart.variantQuantity) {
                    Toast.makeText(
                        context,
                        "Sorry! you can not add more quantity for this product",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                quantity++
                val priceAmount = (cart.price / cart.quantity) * quantity
                val discountAmount = (cart.actualPrice.toDouble() / cart.quantity) * quantity

                if (couponList.size <= 0) {
                    holder.binding.tvQuantity.text = quantity.toString()
                    holder.binding.tvPrice.text =
                        Utility().formatTotalAmount(priceAmount.toDouble()).toString()
                    holder.binding.tvDiscountPrice.text =
                        Utility().formatTotalAmount(discountAmount).toString()
                }
                val itemSavedAmount = (priceAmount - discountAmount)
                if (app.user.getAppLanguage() == 1) {
                    holder.binding.tvSaveAmount.text =
                        context.resources.getString(R.string.saved_h) + " " + context.resources.getString(
                            R.string.Rs
                        ) + Utility().formatTotalAmount(itemSavedAmount).toString()
                } else {
                    holder.binding.tvSaveAmount.text =
                        "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                            itemSavedAmount
                        ).toString()
                }

                Thread {
                    AppDataBase.getInstance(context).productDao()
                        .updateSavedAmount(cart.productId, cart.variantId.toInt(), itemSavedAmount)
                }.start()

                addOremoveItemListener.onClickAddOrRemoveItem(
                    Constant.ADD,
                    ProductDetail(
                        cart.productId,
                        cart.variantId,
                        quantity,
                        "",
                        "",
                        0.0,
                        0,
                        "0",
                        "",
                        0
                    ),
                    holder.binding.tvPlus
                )

                if (app.user.getLoginSession()) {
                    Thread {
                        AppDataBase.getInstance(context).productDao()
                            .updateCart(cart.productId, cart.variantId, discountAmount)
                    }.start()
                }
            }

        }
        holder.binding.tvMinus.setOnClickListener {

            if (Utility.isAppOnLine(context, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {

                    }
                })) {
                holder.binding.tvMinus.isEnabled = false
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
                            "0",
                            "",
                            0
                        ), holder.binding.tvMinus
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
                    val priceAmount = (cart.price / cart.quantity) * quantity
                    val discountAmount = (cart.actualPrice.toDouble() / cart.quantity) * quantity
                    if (couponList.size <= 0) {
                        holder.binding.tvQuantity.text = quantity.toString()
                        holder.binding.tvPrice.text =
                            Utility().formatTotalAmount(priceAmount.toDouble()).toString()
                        holder.binding.tvDiscountPrice.text =
                            Utility().formatTotalAmount(discountAmount).toString()
                    }
                    val itemSavedAmount = (priceAmount - discountAmount)
                    if (app.user.getAppLanguage() == 1) {
                        holder.binding.tvSaveAmount.text =
                            context.resources.getString(R.string.saved_h) + " " + context.resources.getString(
                                R.string.Rs
                            ) + Utility().formatTotalAmount(itemSavedAmount).toString()
                    } else {
                        holder.binding.tvSaveAmount.text =
                            "SAVED " + context.resources.getString(R.string.Rs) + Utility().formatTotalAmount(
                                itemSavedAmount
                            ).toString()
                    }

                    Thread {
                        AppDataBase.getInstance(context).productDao()
                            .updateSavedAmount(
                                cart.productId,
                                cart.variantId.toInt(),
                                itemSavedAmount
                            )
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
                            "0",
                            "",
                            0
                        ), holder.binding.tvMinus
                    )
                    if (app.user.getLoginSession()) {
                        Thread {
                            AppDataBase.getInstance(context).productDao()
                                .updateCart(cart.productId, cart.variantId, discountAmount)
                        }.start()
                    }
                }
            }


        }
        holder.binding.ivDelete.setOnClickListener {
            removeProductListener.removeProductFromList(
                cartList,
                holder.absoluteAdapterPosition,
                cart.productId,
                cart.variantId,
                promoId
            )
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
        val imgURL = app.user.getUserDetails()?.configObj?.productImgUrl + cart.img
        try {
            Glide.with(context).load(imgURL).error(R.drawable.default_image)
                .into(holder.ivImagview)
        } catch (e: Exception) {
            e.printStackTrace()
            holder.ivImagview.setBackgroundResource(R.drawable.default_image)
        }
        holder.tvProductTitle.text = Utility.convertLanguage(cart.productName, app)
        if (app.user.getAppLanguage() == 1) {
            if (cart.productName.contains("$")) {
                try {
                    holder.tvProductTitle.setTypeface(
                        Typeface.createFromAsset(
                            context.assets,
                            "agrawide.ttf"
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        holder.tvPrice.text = cart.price.toString()
        holder.tvDiscountPrice.text = cart.actualPrice
        holder.tvUnit.text = cart.variantUnitQuantity + cart.variantUnit
        holder.tvQuantity.text = cart.quantity.toString()

        if (app.user.getAppLanguage() == 1) {
            holder.tvNetWet.text = context.resources.getString(R.string.net_weight_h)
        }
        var discountPrice = 0;
        if (cart.discountPrice != null) {
            discountPrice = ((cart.discountPrice.toDouble() * 100) / cart.price).toInt()
        }


        Thread {

            val list: List<ProductDetail> =
                AppDataBase.getInstance(context).productDao().getProductList()
            Log.e("TAG", "list Size: " + list)

            val isProductExist = AppDataBase.getInstance(context).productDao()
                .isProductExist(cart.productId, cart.variantId)
            Log.i("nayaganj_app", "setListData: isProductExist " + isProductExist)
            if (isProductExist) {
                val price = cart.price / cart.quantity
                Thread {
                    AppDataBase.getInstance(context).productDao().syncCart(
                        cart.quantity,
                        cart.productName,
                        "",
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
                    holder.tvQuantity.text = cart.quantity.toString()
                }
            } else {
                val price = cart.price / cart.quantity
                Utility().insertProduct(
                    context, ProductDetail(
                        cart.productId,
                        cart.variantId,
                        cart.quantity,
                        cart.productName,
                        "",
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
}