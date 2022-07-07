package naya.ganj.app.data.category.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import naya.ganj.app.R
import naya.ganj.app.data.category.model.CheckProductInCartModel
import naya.ganj.app.data.category.model.ProductListModel
import naya.ganj.app.data.category.view.ProductDetailActivity
import naya.ganj.app.databinding.ProductListLayoutBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant.DEVICE_TYPE
import naya.ganj.app.utility.Constant.PRODUCT_ID
import naya.ganj.app.utility.Constant.USER_ID
import naya.ganj.app.utility.Constant.VARIANT_ID
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductListAdapter(
    val context: Context,
    private val productList: List<ProductListModel.Product>,
    private val onclickAddOrRemoveItemListener: OnclickAddOremoveItemListener
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
        val product = productList.get(position)
        setUpData(holder, product, holder.adapterPosition)
    }

    private fun setUpData(holder: MyViewHolder, product: ProductListModel.Product, position: Int) {

        Picasso.get().load(product.imgUrl[0]).into(holder.binding.ivImagview)
        val list = product.productName.split("$")
        holder.binding.tvProductTitle.text = list[0]
        holder.binding.tvProductDetail.text = product.description
        var isCartEmpty = true

        // set data for variant which has item in cart
        for ((i, item) in product.variant.withIndex()) {
            if (item.vQuantityInCart > 0) {
                setVariantData(holder, product.variant, i)
                isCartEmpty = false
                break
            }
        }

        // set data for variant at 0 position
        if (isCartEmpty) {
            setVariantData(holder, product.variant, 0)
            holder.binding.addItem.visibility = View.VISIBLE
            holder.binding.llPlusMinusLayout.visibility = View.GONE
        }

        holder.binding.llVariantLayout.setOnClickListener {
            showVariantDialog(product, holder)
        }
        holder.binding.ivImagview.setOnClickListener {

            var variantID = ""
            val unitQuantity: Int = holder.binding.tvUnitQuantity.text.toString().toInt()
            for (item in product.variant) {
                if (item.vUnitQuantity == unitQuantity) {
                    variantID = item.vId
                }
            }
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra(PRODUCT_ID, product.id)
            intent.putExtra(VARIANT_ID, variantID)
            context.startActivity(intent)

        }
        holder.binding.addItem.setOnClickListener {
            holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
            holder.binding.addItem.visibility = View.GONE
            holder.binding.tvQuantity.text = "1"

            val unitQuantity: Int = holder.binding.tvUnitQuantity.text.toString().toInt()
            var variantID = ""
            for (item in product.variant) {
                if (item.vUnitQuantity == unitQuantity) {
                    variantID = item.vId
                }
            }
            onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                "add",
                product.id,
                variantID,
                "",
                holder.binding.tvDiscountPrice.text.toString().toDouble()
            )
        }
        holder.binding.tvPlus.setOnClickListener {
            var variantID = ""
            val unitQuantity: Int = holder.binding.tvUnitQuantity.text.toString().toInt()
            for (item in product.variant) {
                if (item.vUnitQuantity == unitQuantity) {
                    variantID = item.vId
                }
            }
            var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
            quantity++
            holder.binding.tvQuantity.text = quantity.toString()
            onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                "add",
                product.id,
                variantID,
                "",
                holder.binding.tvDiscountPrice.text.toString().toDouble() * quantity
            )
        }
        holder.binding.tvMinus.setOnClickListener {
            var variantID = ""
            val unitQuantity: Int = holder.binding.tvUnitQuantity.text.toString().toInt()
            for (item in product.variant) {
                if (item.vUnitQuantity == unitQuantity) {
                    variantID = item.vId
                }
            }

            var quantity: Int = holder.binding.tvQuantity.text.toString().toInt()
            quantity--
            holder.binding.tvQuantity.text = quantity.toString()

            onclickAddOrRemoveItemListener.onClickAddOrRemoveItem(
                "remove",
                product.id,
                variantID,
                "",
                holder.binding.tvDiscountPrice.text.toString().toDouble() * quantity
            )

            if (quantity == 0) {
                holder.binding.llPlusMinusLayout.visibility = View.GONE
                holder.binding.addItem.visibility = View.VISIBLE
            }
        }
    }


    private fun setVariantData(
        holder: MyViewHolder,
        variant: List<ProductListModel.Product.Variant>,
        position: Int
    ) {

        val price = variant[position].vPrice
        val vDiscountPrice: Double

        if (variant.get(position).vDiscount > 0) {
            holder.binding.tvPrice.text = price.toString()
            vDiscountPrice =
                variant.get(position).vPrice - ((variant.get(position).vPrice * variant.get(position).vDiscount) / 100)
        } else {
            vDiscountPrice = price
            holder.binding.tvPrice.visibility = View.INVISIBLE
        }
        holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()

        var vMaxQuantity = variant.get(position).vQuantity
        holder.binding.tvUnitQuantity.text = variant.get(position).vUnitQuantity.toString()
        holder.binding.tvUnit.text = variant.get(position).vUnit

        if (variant.get(position).vQuantityInCart > 0) {
            holder.binding.addItem.visibility = View.GONE
            holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
            holder.binding.tvQuantity.text = variant.get(position).vQuantityInCart.toString()
        } else {
            holder.binding.addItem.visibility = View.VISIBLE
            holder.binding.llPlusMinusLayout.visibility = View.GONE
        }
    }

    private fun showVariantDialog(product: ProductListModel.Product, holder: MyViewHolder) {
        var alertDialog: AlertDialog? = null
        val productName = product.productName.let {
            product.productName.split("$")
        }
        val productDetail = product.description.let {
            product.description.split("$")
        }

        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_variant_dialog, null, false)

        val variantList = view.findViewById(R.id.rv_variant_list) as RecyclerView
        variantList.layoutManager = LinearLayoutManager(context)

        val customVariantAdapter =
            CustomVariantAdapter(product.variant, object : OnitemClickListener {
                @SuppressLint("SetTextI18n")
                override fun onclick(position: Int, data: String) {
                    // Variant Click Handle
                    alertDialog?.dismiss()
                    checkProductInCart(
                        holder,
                        product.variant,
                        product.id,
                        product.variant.get(position).vId,
                        position
                    )
                }
            })
        variantList.adapter = customVariantAdapter
        val title = view.findViewById(R.id.tv_title) as TextView
        val description = view.findViewById(R.id.tv_description) as TextView
        val ivClose = view.findViewById(R.id.ivclose) as ImageView
        title.text = productName[0]
        description.text = productDetail[0]

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

        RetrofitClient.instance.checkProductInCartRequest(USER_ID, DEVICE_TYPE, jsonObject)
            .enqueue(object : Callback<CheckProductInCartModel> {
                override fun onResponse(
                    call: Call<CheckProductInCartModel>,
                    response: Response<CheckProductInCartModel>
                ) {
                    if (response.isSuccessful) {
                        val price = variant.get(position).vPrice
                        val vDiscountPrice: Double
                        if (variant.get(position).vDiscount > 0) {
                            vDiscountPrice =
                                variant.get(position).vPrice - ((variant.get(position).vPrice * variant.get(
                                    position
                                ).vDiscount) / 100)

                        } else {
                            vDiscountPrice = price
                            holder.binding.tvPrice.visibility = View.GONE
                        }

                        var vMaxQuantity = variant.get(position).vQuantity
                        holder.binding.tvPrice.text = price.toString()
                        holder.binding.tvDiscountPrice.text = vDiscountPrice.toString()
                        holder.binding.tvUnitQuantity.text =
                            variant.get(position).vUnitQuantity.toString()
                        holder.binding.tvUnit.text = variant.get(position).vUnit
                        if (response.body()?.status == true) {
                            holder.binding.tvQuantity.text =
                                response.body()!!.productQuantity.toString()
                            holder.binding.llPlusMinusLayout.visibility = View.VISIBLE
                            holder.binding.addItem.visibility = View.GONE

                            // Save Count in Local
                            Thread(Runnable {
                                Utility().updateProduct(
                                    context,
                                    product_Id,
                                    variant_id,
                                    response.body()!!.productQuantity * holder.binding.tvDiscountPrice.text.toString()
                                        .toDouble()
                                )
                            }).start()

                        } else {
                            holder.binding.llPlusMinusLayout.visibility = View.GONE
                            holder.binding.addItem.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<CheckProductInCartModel>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}