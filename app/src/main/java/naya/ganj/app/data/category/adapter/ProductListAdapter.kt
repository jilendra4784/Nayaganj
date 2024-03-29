package naya.ganj.app.data.category.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.model.CheckProductInCartModel
import naya.ganj.app.data.category.model.ProductListModel
import naya.ganj.app.data.category.view.ProductDetailActivity
import naya.ganj.app.databinding.ProductListLayoutBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.DEVICE_TYPE
import naya.ganj.app.utility.Constant.PRODUCT_ID
import naya.ganj.app.utility.Constant.VARIANT_ID
import naya.ganj.app.utility.ImageManager
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProductListAdapter(
    val activity: Activity,
    val context: Context,
    private val productList: List<ProductListModel.Product>,
    private val onclickAddOrRemoveItemListener: OnclickAddOremoveItemListener,
    val app: Nayaganj
) :
    RecyclerView.Adapter<ProductListAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: ProductListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            ProductListLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //setFadeAnimation(holder.itemView)
        val product = productList[holder.absoluteAdapterPosition]
        setUpData(holder, product, holder.absoluteAdapterPosition)
    }

    private fun setUpData(holder: MyViewHolder, product: ProductListModel.Product, position: Int) {
        try {
            if (product.imgUrl.isNotEmpty()) {
                ImageManager.onLoadingImage(
                    app,
                    context,
                    product.imgUrl[0],
                    holder.binding.ivImagview
                )
            } else {
                holder.binding.ivImagview.setImageDrawable(null);
                holder.binding.ivImagview.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.default_image
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.binding.tvProductTitle.text =
            Utility.convertLanguage(product.productName.trimStart(), app)
        holder.binding.tvProductDetail.text = Utility.convertLanguage(product.description, app)
        if (app.user.getAppLanguage() == 1) {
            if (product.productName.contains("$")) {
                holder.binding.tvProductTitle.typeface =
                    Typeface.createFromAsset(context.assets, "agrawide.ttf")
            }
        }
        Thread {
            val listOfProduct =
                AppDataBase.getInstance(context).productDao().getProductListByProductId(product.id)
            if (listOfProduct.isNotEmpty()) {
                Log.e("TAG", "setUpData: " + listOfProduct)
                activity.runOnUiThread {
                    val price = listOfProduct[0].vPrice
                    var vDiscountPrice: Double
                    if (listOfProduct[0].vDiscount > 0) {
                        vDiscountPrice =
                            price - ((price * listOfProduct[0].vDiscount) / 100)
                        holder.binding.tvPrice.text = price.toString()
                        holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()
                    } else {
                        vDiscountPrice = price
                        holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()
                        holder.binding.tvPrice.visibility = View.INVISIBLE
                        holder.binding.tvRupee.visibility = View.GONE
                        holder.binding.tvOff.visibility = View.GONE

                    }
                    holder.binding.tvQuantity.text = listOfProduct[0].itemQuantity.toString()
                    holder.binding.tvUnitQuantity.text = listOfProduct[0].vUnitQuantity
                    holder.binding.tvUnit.text = listOfProduct[0].vUnit

                    if (app.user.getAppLanguage() == 1) {
                        holder.binding.tvOff.text =
                            listOfProduct[0].vDiscount.toString() + "% " + context.resources.getString(
                                R.string.off_h
                            )
                    } else {
                        holder.binding.tvOff.text = listOfProduct[0].vDiscount.toString() + "% off"
                    }
                    holder.binding.addItem.visibility = View.GONE
                    holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
                }
            } else {
                var index = 0
                for ((i, variant) in product.variant.withIndex()) {
                    if (variant.vQuantityInCart > 0) {
                        index = i
                    }
                }
                activity.runOnUiThread { setVariantData(holder, product.variant, index, 0) }

                onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                    "", ProductDetail(
                        "",
                        "",
                        0,
                        "",
                        "",
                        0.0,
                        0,
                        "0",
                        "",
                        0
                    ), holder.binding.tvMinus
                )
            }
        }.start()

        holder.binding.llVariantLayout.setOnClickListener {
            showVariantDialog(product, holder)
        }
        holder.binding.ivImagview.setOnClickListener {
            var variantID = ""
            val unitQuantity: String = holder.binding.tvUnitQuantity.text.toString()
            for (item in product.variant) {
                if (item.vUnitQuantity == unitQuantity) {
                    variantID = item.vId
                }
            }
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra(PRODUCT_ID, product.id)
            intent.putExtra(VARIANT_ID, variantID)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                (holder.binding.ivImagview as View?)!!, "profile"
            )
            context.startActivity(intent, options.toBundle())


        }

        holder.binding.tvProductTitle.setOnClickListener {
            var variantID = ""
            val unitQuantity: String = holder.binding.tvUnitQuantity.text.toString()
            for (item in product.variant) {
                if (item.vUnitQuantity == unitQuantity) {
                    variantID = item.vId
                }
            }

            holder.binding.ivImagview.transitionName = "product_image"

            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra(PRODUCT_ID, product.id)
            intent.putExtra(VARIANT_ID, variantID)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                (holder.binding.ivImagview as View?)!!, "product_image"
            )
            context.startActivity(intent, options.toBundle())

        }


        holder.binding.addItem.setOnClickListener {
            if (Utility.isAppOnLine(context, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        updateItemToLocalDB("add", holder, product, holder.binding.addItem)
                    }
                }))
                updateItemToLocalDB("add", holder, product, holder.binding.addItem)
        }
        holder.binding.tvPlus.setOnClickListener {
            if (Utility.isAppOnLine(context, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        updateItemToLocalDB(Constant.INSERT, holder, product, holder.binding.tvPlus)
                    }
                }))
                updateItemToLocalDB("plus", holder, product, holder.binding.tvPlus)
        }
        holder.binding.tvMinus.setOnClickListener {
            if (Utility.isAppOnLine(context, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        updateItemToLocalDB("minus", holder, product, holder.binding.tvMinus)
                    }
                }))
                updateItemToLocalDB("minus", holder, product, holder.binding.tvMinus)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setVariantData(
        holder: MyViewHolder,
        variant: List<ProductListModel.Product.Variant>,
        vPosition: Int,
        updatedQuantityinCart: Int
    ) {
        val quantityInCart: Int = if (updatedQuantityinCart > 0) {
            updatedQuantityinCart
        } else {
            variant[vPosition].vQuantityInCart
        }
        if (quantityInCart > 0) {
            Thread {
                val isProductExist = AppDataBase.getInstance(context).productDao().isProductExist(
                    productList[holder.adapterPosition].id,
                    variant[vPosition].vId
                )
                if (isProductExist) {
                    val singleProduct = AppDataBase.getInstance(context).productDao()
                        .getSingleProduct(
                            productList.get(holder.adapterPosition).id,
                            variant[vPosition].vId
                        )
                    activity.runOnUiThread {
                        holder.binding.addItem.visibility = View.GONE
                        holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
                        holder.binding.tvUnitQuantity.text = singleProduct.vUnitQuantity.toString()
                        holder.binding.tvUnit.text = singleProduct.vUnit
                        holder.binding.tvQuantity.text = singleProduct.itemQuantity.toString()
                        var vMaxQuantity = singleProduct.totalVariantQuantity

                        val price = singleProduct.vPrice
                        val vDiscountPrice: Double
                        if (singleProduct.vDiscount > 0) {
                            holder.binding.tvPrice.text = price.toString()
                            vDiscountPrice =
                                price - ((price * singleProduct.vDiscount) / 100)
                        } else {
                            vDiscountPrice = price
                            holder.binding.tvPrice.visibility = View.INVISIBLE
                            holder.binding.tvRupee.visibility = View.GONE
                            holder.binding.tvOff.visibility = View.GONE
                        }
                        holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()

                        if (app.user.getAppLanguage() == 1) {
                            holder.binding.tvOff.text =
                                singleProduct.vDiscount.toString() + "% " + context.resources.getString(
                                    R.string.off_h
                                )
                        } else {
                            holder.binding.tvOff.text = singleProduct.vDiscount.toString() + "% off"
                        }


                    }
                } else {
                    activity.runOnUiThread {
                        holder.binding.addItem.visibility = View.VISIBLE
                        holder.binding.llPlusMinusLayout.visibility = View.GONE
                        holder.binding.tvUnitQuantity.text =
                            variant[vPosition].vUnitQuantity.toString()
                        holder.binding.tvUnit.text = variant[vPosition].vUnit
                        holder.binding.tvQuantity.text = quantityInCart.toString()

                        val price = variant[vPosition].vPrice
                        val vDiscountPrice: Double
                        if (variant[vPosition].vDiscount > 0) {
                            holder.binding.tvPrice.text = price.toString()
                            vDiscountPrice =
                                price - ((price * variant[vPosition].vDiscount) / 100)
                        } else {
                            vDiscountPrice = price
                            holder.binding.tvPrice.visibility = View.INVISIBLE
                            holder.binding.tvRupee.visibility = View.INVISIBLE
                            holder.binding.tvOff.visibility = View.GONE
                        }
                        holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()
                        if (app.user.getAppLanguage() == 1) {
                            holder.binding.tvOff.text =
                                variant[vPosition].vDiscount.toString() + "% " + context.resources.getString(
                                    R.string.off_h
                                )
                        } else {
                            holder.binding.tvOff.text =
                                variant[vPosition].vDiscount.toString() + "% off"
                        }

                        var vMaxQuantity = variant[vPosition].vQuantity

                    }
                }
            }.start()
        } else {

            holder.binding.addItem.visibility = View.VISIBLE
            holder.binding.llPlusMinusLayout.visibility = View.GONE
            holder.binding.tvUnitQuantity.text =
                variant[vPosition].vUnitQuantity.toString()
            holder.binding.tvUnit.text = variant[vPosition].vUnit
            holder.binding.tvQuantity.text =
                variant[vPosition].vQuantityInCart.toString()

            val price = variant[vPosition].vPrice
            val vDiscountPrice: Double
            if (variant[vPosition].vDiscount > 0) {
                holder.binding.tvPrice.text = price.toString()
                vDiscountPrice =
                    price - ((price * variant[vPosition].vDiscount) / 100)
            } else {
                vDiscountPrice = price
                holder.binding.tvPrice.visibility = View.INVISIBLE
                holder.binding.tvRupee.visibility = View.GONE
                holder.binding.tvOff.visibility = View.GONE
            }
            holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()

            if (app.user.getAppLanguage() == 1) {
                holder.binding.tvOff.text =
                    variant[vPosition].vDiscount.toString() + "% " + context.resources.getString(R.string.off_h)
                holder.binding.tvNetWet.text = context.resources.getString(R.string.net_weight_h)
                holder.binding.addItem.text = context.resources.getString(R.string.add_h)
            } else {
                holder.binding.tvOff.text = variant[vPosition].vDiscount.toString() + "% off"
            }

            var vMaxQuantity = variant[vPosition].vQuantity
        }

    }

    private fun showVariantDialog(product: ProductListModel.Product, holder: MyViewHolder) {
        var alertDialog: AlertDialog? = null

        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_variant_dialog, null, false)

        val variantList = view.findViewById(R.id.rv_variant_list) as RecyclerView
        variantList.layoutManager = LinearLayoutManager(context)

        val customVariantAdapter =
            CustomVariantAdapter(context, app, product.variant, object : OnitemClickListener {
                @SuppressLint("SetTextI18n")
                override fun onclick(vPos: Int, data: String) {
                    // Variant Click Handle

                    if (app.user.getLoginSession()) {
                        activity.runOnUiThread {
                            checkProductInCart(
                                holder,
                                product.variant,
                                productList[holder.adapterPosition].id,
                                data,
                                vPos
                            )
                        }

                    } else {
                        Thread {
                            val isProductExist = AppDataBase.getInstance(context).productDao()
                                .isProductExist(product.id, data)
                            if (isProductExist) {
                                val singleProduct = AppDataBase.getInstance(context).productDao()
                                    .getSingleProduct(product.id, data)
                                activity.runOnUiThread {
                                    val price = singleProduct.vPrice
                                    val vDiscountPrice: Double =
                                        price - ((price * singleProduct.vDiscount) / 100)

                                    holder.binding.tvPrice.text = price.toString()
                                    holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()

                                    if (app.user.getAppLanguage() == 1) {
                                        holder.binding.tvOff.text =
                                            singleProduct.vDiscount.toString() + "% " + context.resources.getString(
                                                R.string.off_h
                                            )
                                    } else {
                                        holder.binding.tvOff.text =
                                            singleProduct.vDiscount.toString() + "% off"
                                    }

                                    holder.binding.tvQuantity.text =
                                        singleProduct.itemQuantity.toString()
                                    holder.binding.tvUnitQuantity.text =
                                        singleProduct.vUnitQuantity.toString()
                                    holder.binding.tvUnit.text = singleProduct.vUnit

                                    holder.binding.addItem.visibility = View.GONE
                                    holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
                                }
                            } else {
                                activity.runOnUiThread {
                                    setVariantData(
                                        holder,
                                        product.variant,
                                        vPos, 0
                                    )
                                }
                            }

                        }.start()
                    }
                    alertDialog?.dismiss()
                }
            })
        variantList.adapter = customVariantAdapter

        val title = view.findViewById(R.id.tv_title) as TextView
        val description = view.findViewById(R.id.tv_description) as TextView
        val ivClose = view.findViewById(R.id.ivclose) as ImageView

        title.text = product.productName //Utility.convertLanguage(product.productName,app)
        //title.typeface = Typeface.createFromAsset(context.assets, "agrawide.ttf")
        description.text = Utility.convertLanguage(product.description, app)

        materialAlertDialogBuilder.setView(view)
        alertDialog = materialAlertDialogBuilder.create()
        alertDialog.show()

        ivClose.setOnClickListener {
            alertDialog.dismiss()
        }


    }

    // On Variant Selection
    private fun checkProductInCart(
        holder: MyViewHolder,
        variant: List<ProductListModel.Product.Variant>,
        product_Id: String,
        variant_id: String,
        position: Int
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(PRODUCT_ID, product_Id)
        jsonObject.addProperty(VARIANT_ID, variant_id)

        RetrofitClient.instance.checkProductInCartRequest(
            app.user.getUserDetails()?.userId,
            DEVICE_TYPE,
            jsonObject
        )
            .enqueue(object : Callback<CheckProductInCartModel> {
                override fun onResponse(
                    call: Call<CheckProductInCartModel>,
                    response: Response<CheckProductInCartModel>
                ) {
                    if (response.body()!!.status) {
                        setVariantData(holder, variant, position, response.body()!!.productQuantity)
                    } else {
                        holder.binding.llPlusMinusLayout.visibility = View.GONE
                        holder.binding.addItem.visibility = View.VISIBLE
                        setVariantData(holder, variant, position, 0)
                    }
                }

                override fun onFailure(call: Call<CheckProductInCartModel>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun updateItemToLocalDB(
        action: String,
        holder: MyViewHolder,
        product: ProductListModel.Product,
        textview: MaterialTextView
    ) {
        var variantID = ""
        var vPrice = 0.0
        var vDiscount = 0
        var vUnitQuantity = "0"
        var vUnit = ""
        var totalMaxQuantity = 0

        val unitQuantity: String = holder.binding.tvUnitQuantity.text.toString()
        for (item in product.variant) {
            if (item.vUnitQuantity == unitQuantity) {
                variantID = item.vId
                vPrice = item.vPrice
                vDiscount = item.vDiscount
                vUnitQuantity = item.vUnitQuantity
                vUnit = item.vUnit
                totalMaxQuantity = item.vQuantity
            }
        }

        when (action) {
            "add" -> {
                if (Utility.isAppOnLine(context, object : OnInternetCheckListener {
                        override fun onInternetAvailable() {

                        }
                    })) {

                    holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
                    holder.binding.addItem.visibility = View.GONE
                    holder.binding.tvQuantity.text = "1"
                    val quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
                    // Refresh Cart List

                    val imgURL = try {
                        product.imgUrl[0]
                    } catch (e: Exception) {
                        ""
                    }

                    onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                        Constant.INSERT, ProductDetail(
                            product.id, variantID, quantity, product.productName, imgURL,
                            vPrice, vDiscount, vUnitQuantity, vUnit, totalMaxQuantity
                        ), textview
                    )
                }
            }
            "plus" -> {

                if (Utility.isAppOnLine(context, object : OnInternetCheckListener {
                        override fun onInternetAvailable() {

                        }
                    })) {
                    var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
                    if (quantity >= totalMaxQuantity) {
                        Toast.makeText(
                            context,
                            "Sorry, you can not add more quantity of this product",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    quantity++
                    holder.binding.tvQuantity.text = quantity.toString()

                    val imgURL = try {
                        product.imgUrl[0]
                    } catch (e: Exception) {
                        ""
                    }
                    onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                        Constant.ADD, ProductDetail(
                            product.id, variantID, quantity, product.productName, imgURL,
                            vPrice, vDiscount, vUnitQuantity, vUnit, totalMaxQuantity
                        ), textview
                    )
                }
            }
            "minus" -> {

                if (Utility.isAppOnLine(context, object : OnInternetCheckListener {
                        override fun onInternetAvailable() {

                        }
                    })) {
                    var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
                    quantity--

                    if (quantity == 0) {
                        holder.binding.llPlusMinusLayout.visibility = View.GONE
                        holder.binding.addItem.visibility = View.VISIBLE

                        val imgURL = try {
                            product.imgUrl[0]
                        } catch (e: Exception) {
                            ""
                        }

                        onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                            Constant.REMOVE, ProductDetail(
                                product.id,
                                variantID,
                                quantity,
                                product.productName,
                                imgURL,
                                vPrice,
                                vDiscount,
                                vUnitQuantity,
                                vUnit,
                                totalMaxQuantity
                            ), textview
                        )
                        return
                    }
                    holder.binding.tvQuantity.text = quantity.toString()
                    val imgURL = try {
                        product.imgUrl[0]
                    } catch (e: Exception) {
                        ""
                    }
                    onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                        Constant.REMOVE, ProductDetail(
                            product.id, variantID, quantity, product.productName, imgURL,
                            vPrice, vDiscount, vUnitQuantity, vUnit, totalMaxQuantity
                        ), textview
                    )
                }
            }
        }
    }

    fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 500
        anim.backgroundColor = context.resources.getColor(R.color.gray)
        view.startAnimation(anim)
    }
}