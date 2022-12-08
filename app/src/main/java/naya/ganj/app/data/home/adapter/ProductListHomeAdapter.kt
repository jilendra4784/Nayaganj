package naya.ganj.app.data.home.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.model.CheckProductInCartModel
import naya.ganj.app.data.category.view.ProductDetailActivity
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.databinding.ProductListHomeAdapterBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductListHomeAdapter(val context : Context, val product: List<HomePageModel.Data.Product>, val app: Nayaganj
                             , val activity: Activity, private val onclickAddOrRemoveItemListener: OnclickAddOremoveItemListener,
) :
    RecyclerView.Adapter<ProductListHomeAdapter.MyViewHolder>() {

    var vMaxQuantity=0

    class MyViewHolder(val binding: ProductListHomeAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun getItemCount(): Int {
        return product.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view =
            ProductListHomeAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try {
            var imgURL =
                app.user.getUserDetails()?.configObj?.productImgUrl + product[position].imgUrl[0]
            Glide.with(context).load(imgURL).error(R.drawable.default_image)
                .into(holder.binding.imageView11)
        } catch (e: Exception) {
            holder.binding.imageView11.setBackgroundResource(R.drawable.default_image)
        }

        holder.binding.tvTitle.text = Utility.convertLanguage(product[position].productName, app)
        holder.binding.tvDescription.text =
            Utility.convertLanguage(product[position].description as String?, app)

        if (app.user.getAppLanguage() == 1) {
            holder.binding.tvTitle.typeface =
                Typeface.createFromAsset(context.assets, "agrawide.ttf")
            holder.binding.tvDescription.typeface =
                Typeface.createFromAsset(context.assets, "agrawide.ttf")
        }

        holder.binding.llVariantLayout.setOnClickListener {
            showVariantDialog(
                product[position].variant,
                holder,
                product.get(holder.adapterPosition).id
            )
        }

        holder.binding.ivImageview.setOnClickListener {

            val unit = holder.binding.tvUnitQuantity.text.toString()
            Log.e("TAG", "onBindViewHolder: " + unit)

            var variantId = ""
            for (item in product[position].variant) {
                if (item.vUnitQuantity.toString() == unit) {
                    variantId = item.vId
                }
            }

            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra(Constant.productId, product[position].id)
            intent.putExtra(Constant.variantId,variantId)
            context.startActivity(intent)

        }
        Thread {
            val listOfProduct =
                AppDataBase.getInstance(context).productDao().getProductListByProductId(product.get(position).id)
            if (listOfProduct.isNotEmpty()) {
                activity.runOnUiThread {
                    val price = listOfProduct[0].vPrice
                    val vDiscountPrice: Double =
                        price - ((price * listOfProduct[0].vDiscount) / 100)
                    holder.binding.tvPrice.text = price.toString()
                    holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()
                    holder.binding.tvQuantity.text = listOfProduct[0].itemQuantity.toString()
                    holder.binding.tvUnitQuantity.text = listOfProduct[0].vUnitQuantity.toString()
                    holder.binding.tvUnit.text = listOfProduct[0].vUnit

                    if (app.user.getAppLanguage() == 1) {
                        holder.binding.tvOffer.text =
                            listOfProduct[0].vDiscount.toString() + "% " + context.resources.getString(
                                R.string.off_h
                            )
                    } else {
                        holder.binding.tvOffer.text =
                            listOfProduct[0].vDiscount.toString() + "% off"
                    }
                    holder.binding.addItem.visibility = View.GONE
                    holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
                }
            } else {
                var index = 0
                for ((i, variant) in product.get(position).variant.withIndex()) {
                    if (variant.vQuantityInCart > 0) {
                        index = i
                    }
                }
                activity.runOnUiThread { setVariantData(holder, product.get(position).variant, index, 0) }

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
                    ),holder.binding.tvMinus
                )
            }
        }.start()
        holder.binding.addItem.setOnClickListener {
            if(Utility.isAppOnLine(context,object: OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        updateItemToLocalDB("add", holder, product[holder.adapterPosition],holder.binding.addItem)
                    }
                }))
                updateItemToLocalDB("add", holder, product.get(holder.adapterPosition),holder.binding.addItem)
        }
        holder.binding.tvPlus.setOnClickListener {
            if(Utility.isAppOnLine(context,object: OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        updateItemToLocalDB("plus", holder, product.get(holder.adapterPosition),holder.binding.tvPlus)
                    }
                }))
                updateItemToLocalDB("plus", holder, product.get(holder.adapterPosition),holder.binding.tvPlus)
        }
        holder.binding.tvMinus.setOnClickListener {
            if(Utility.isAppOnLine(context,object: OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        updateItemToLocalDB("minus", holder, product.get(holder.adapterPosition),holder.binding.tvMinus)
                    }
                }))
                updateItemToLocalDB("minus", holder, product.get(holder.adapterPosition),holder.binding.tvMinus)
        }


    }



    private fun showVariantDialog(
        variant: List<HomePageModel.Data.Product.Variant>,
        holder: MyViewHolder,
        productId: String
    ) {
        var alertDialog: AlertDialog?=null

        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_variant_dialog, null, false)

        val variantList = view.findViewById(R.id.rv_variant_list) as RecyclerView
        variantList.layoutManager = LinearLayoutManager(context)

        variantList.adapter=CustomHomeVariantAdapter(context,variant,app, object : OnitemClickListener{
            override fun onclick(vPos: Int, data: String) {
                if (app.user.getLoginSession()) {
                    activity.runOnUiThread {
                        checkProductInCart(
                            holder,
                            variant,
                            productId,
                            data,
                            vPos
                        )
                    }

                } else {
                    Thread {
                        val isProductExist = AppDataBase.getInstance(context).productDao()
                            .isProductExist(productId, data)
                        if (isProductExist) {
                            val singleProduct = AppDataBase.getInstance(context).productDao()
                                .getSingleProduct(productId, data)
                            activity.runOnUiThread {

                                val price = singleProduct.vPrice
                                val vDiscountPrice: Double =
                                    price - ((price * singleProduct.vDiscount) / 100)

                                holder.binding.tvPrice.text = price.toString()
                                holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()

                                if (app.user.getAppLanguage() == 1) {
                                    holder.binding.tvOffer.text =
                                        singleProduct.vDiscount.toString() + "% " + context.resources.getString(
                                            R.string.off_h
                                        )
                                } else {
                                    holder.binding.tvOffer.text =
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
                                    variant,
                                    vPos, 0
                                )
                            }
                        }

                    }.start()
                }
                alertDialog?.dismiss()
            }
        })

        val title = view.findViewById(R.id.tv_title) as TextView
        val description = view.findViewById(R.id.tv_description) as TextView
        val ivClose = view.findViewById(R.id.ivclose) as ImageView

        title.text = Utility.convertLanguage(product[holder.adapterPosition].productName,app)

        description.text = Utility.convertLanguage(product[holder.adapterPosition].description as String?,app)

        if(app.user.getAppLanguage()==1){
            title.typeface = Typeface.createFromAsset(context.assets, "agrawide.ttf")
            description.typeface = Typeface.createFromAsset(context.assets, "agrawide.ttf")
        }

        materialAlertDialogBuilder.setView(view)
        alertDialog = materialAlertDialogBuilder.create()
        alertDialog.show()

        ivClose.setOnClickListener {
            alertDialog.dismiss()
        }


    }

    private fun checkProductInCart(
        holder: MyViewHolder,
        variant: List<HomePageModel.Data.Product.Variant>,
        product_Id: String,
        variant_id: String,
        position: Int
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(Constant.PRODUCT_ID, product_Id)
        jsonObject.addProperty(Constant.VARIANT_ID, variant_id)

        RetrofitClient.instance.checkProductInCartRequest(
            app.user.getUserDetails()?.userId,
            Constant.DEVICE_TYPE,
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

    @SuppressLint("SetTextI18n")
    private fun setVariantData(
        holder: MyViewHolder,
        variant: List<HomePageModel.Data.Product.Variant>,
        vPosition: Int,
        updatedQuantityinCart: Int
    ) {
        val quantityInCart: Int? = if (updatedQuantityinCart > 0) {
            updatedQuantityinCart
        } else {
            variant[vPosition].vQuantityInCart
        }
        if (quantityInCart != null) {
            if (quantityInCart > 0) {
                Thread {
                    val isProductExist = AppDataBase.getInstance(context).productDao().isProductExist(
                        product.get(holder.adapterPosition).id,
                        variant[vPosition].vId
                    )
                    if (isProductExist) {
                        val singleProduct = AppDataBase.getInstance(context).productDao()
                            .getSingleProduct(
                                product.get(holder.adapterPosition).id,
                                variant[vPosition].vId
                            )
                        activity.runOnUiThread {
                            holder.binding.addItem.visibility = View.GONE
                            holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
                            holder.binding.tvUnitQuantity.text = singleProduct.vUnitQuantity.toString()
                            holder.binding.tvUnit.text = singleProduct.vUnit
                            holder.binding.tvQuantity.text = singleProduct.itemQuantity.toString()
                            vMaxQuantity = singleProduct.totalVariantQuantity

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
                            }
                            holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()

                            if(app.user.getAppLanguage()==1){
                                holder.binding.tvOffer.text = singleProduct.vDiscount.toString() + "% "+context.resources.getString(R.string.off_h)
                            }else{
                                holder.binding.tvOffer.text  = singleProduct.vDiscount.toString() + "% off"
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
                                    (price - ((price * variant[vPosition].vDiscount) / 100)).toDouble()
                            } else {
                                vDiscountPrice = price.toDouble()
                                holder.binding.tvPrice.visibility = View.INVISIBLE
                                holder.binding.tvRupee.visibility = View.GONE
                            }
                            holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()

                            if(app.user.getAppLanguage()==1){
                                holder.binding.tvOffer.text = variant[vPosition].vDiscount.toString() + "% "+context.resources.getString(R.string.off_h)
                            }else{
                                holder.binding.tvOffer.text = variant[vPosition].vDiscount.toString() + "% off"
                            }
                            vMaxQuantity = variant[vPosition].vQuantity
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
                        (price - ((price * variant[vPosition].vDiscount) / 100)).toDouble()
                } else {
                    vDiscountPrice = price.toDouble()
                    holder.binding.tvPrice.visibility = View.INVISIBLE
                    holder.binding.tvRupee.visibility = View.GONE
                }
                holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()

                if (app.user.getAppLanguage() == 1) {
                    holder.binding.tvOffer.text =
                        variant[vPosition].vDiscount.toString() + "% " + context.resources.getString(R.string.off_h)
                    holder.binding.netweight.text = context.resources.getString(R.string.net_weight_h)
                    holder.binding.addItem.text = context.resources.getString(R.string.add_h)
                } else {
                    holder.binding.tvOffer.text = variant[vPosition].vDiscount.toString() + "% off"
                }
                vMaxQuantity = variant[vPosition].vQuantity
            }
        }

    }

    private fun updateItemToLocalDB(
        action: String,
        holder: MyViewHolder,
        product: HomePageModel.Data.Product,
        textview: MaterialTextView
    ) {
        var variantID = ""
        var vPrice = 0.0
        var vDiscount = 0
        var vUnitQuantity = "0"
        var vUnit = ""
        var totalMaxQuantity = 0

        val unitQuantity: String = holder.binding.tvUnitQuantity.text.toString()
        Log.e("TAG", "updateItemToLocalDB: " + unitQuantity)

        Log.e("TAG", "updateItemToLocalDB: " + vMaxQuantity)
        /*if(unitQuantity>vMaxQuantity){
            Utility.showToast(context,"Sorry you can not add more item of this product.")
            return
        }*/

        for (item in product.variant) {
            if (item.vUnitQuantity == unitQuantity) {
                variantID = item.vId
                vPrice = item.vPrice.toDouble()
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
                            product.id, variantID, quantity, product.productName, imgURL as String,
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
                    Log.e("TAG", "totalMaxQuantity: "+totalMaxQuantity )
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

                    var imgURL = try {
                        product.imgUrl.get(0)
                    } catch (e: Exception) {
                        ""
                    }
                    onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                        Constant.ADD, ProductDetail(
                            product.id, variantID, quantity, product.productName, imgURL as String,
                            vPrice, vDiscount, vUnitQuantity, vUnit, totalMaxQuantity
                        ), textview
                    )
                }
            }
            "minus" -> {

                if(Utility.isAppOnLine(context,object : OnInternetCheckListener{
                        override fun onInternetAvailable() {

                        }
                    })){
                    var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
                    quantity--

                    if (quantity == 0) {
                        holder.binding.llPlusMinusLayout.visibility = View.GONE
                        holder.binding.addItem.visibility = View.VISIBLE

                        var imgURL = try {
                            product.imgUrl.get(0)
                        } catch (e: Exception) {
                            ""
                        }

                        onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                            Constant.REMOVE, ProductDetail(
                                product.id,
                                variantID,
                                quantity,
                                product.productName,
                                imgURL as String,
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
                    var imgURL = try {
                        product.imgUrl[0]
                    } catch (e: Exception) {
                        ""
                    }
                    onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                        Constant.REMOVE, ProductDetail(
                            product.id, variantID, quantity, product.productName, imgURL as String,
                            vPrice, vDiscount, vUnitQuantity, vUnit, totalMaxQuantity
                        ), textview
                    )
                }
            }
        }
    }
}