package naya.ganj.app.data.category.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.adapter.ProductDetailVariantAdapter
import naya.ganj.app.data.category.model.CheckProductInCartModel
import naya.ganj.app.data.category.model.ProductDetailModel
import naya.ganj.app.data.category.viewmodel.ProductDetailViewModel
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.databinding.ActivityProductDetailBinding
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.PRODUCT_ID
import naya.ganj.app.utility.Constant.VARIANT_ID
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : AppCompatActivity() {

    lateinit var viewModel: ProductDetailViewModel
    lateinit var binding: ActivityProductDetailBinding
    var variantId = ""
    var productId = ""
    lateinit var productDetailModel: ProductDetailModel
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as Nayaganj

        binding.include.ivBackArrow.setOnClickListener { finish() }
        binding.include.toolbarTitle.text = "Product Detail"


        viewModel = ViewModelProvider(this)[ProductDetailViewModel::class.java]
        if (intent.extras != null) {
            productId = intent.getStringExtra(PRODUCT_ID).toString()
            variantId = intent.getStringExtra(VARIANT_ID).toString()
            getProductData(productId)
        }

        binding.addButton.setOnClickListener {
            updateItemToLocalDB("add", productDetailModel.productDetails)
        }
        binding.tvPlus.setOnClickListener {
            updateItemToLocalDB("plus", productDetailModel.productDetails)
        }
        binding.tvMinus.setOnClickListener {
            updateItemToLocalDB("minus", productDetailModel.productDetails)
        }

        binding.llVariantLayout.setOnClickListener {
            showVariantDialog(productDetailModel)
        }


        binding.llCartLayout.setOnClickListener { v ->
            startActivity(Intent(this@ProductDetailActivity, MyCartActivity::class.java))
            finish()
        }

    }

    private fun getProductData(productId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(PRODUCT_ID, productId)

        viewModel.getProductDetai(jsonObject)
            .observe(this) {
                setProductData(it.productDetails)
                productDetailModel = it
            }
    }

    private fun setProductData(pModel: ProductDetailModel.ProductDetails) {
        Picasso.get().load(pModel.imgUrl[0]).into(binding.ivProductImage)
        val titleArray = pModel.productName.split("$")
        val titleDescriptionArray = pModel.description.split("$")
        binding.tvProductName.text = titleArray[0]
        binding.tvDescription.text = titleDescriptionArray[0]


        Thread {
            val isProductExist = AppDataBase.getInstance(this).productDao()
                .isProductExist(productId, variantId)
            if (isProductExist) {
                val item = AppDataBase.getInstance(this).productDao()
                    .getSingleProduct(productId, variantId)
                runOnUiThread {
                    val price = item.vPrice
                    val vDiscountPrice: Double =
                        price - ((price * item.vDiscount) / 100)
                    binding.tvPrice.text = resources.getString(R.string.Rs) + price.toString()
                    binding.tvDiscountPrice.text =
                        resources.getString(R.string.Rs) + vDiscountPrice.toString()
                    binding.tvQuantity.text = item.itemQuantity.toString()
                    binding.tvUnit.text = item.vUnitQuantity.toString() + item.vUnit
                    binding.tvOff.text = item.vDiscount.toString() + "% off"
                    binding.addButton.visibility = View.GONE
                    binding.llMinusPlusLayout.visibility = View.VISIBLE

                }
            } else {
                runOnUiThread {
                    for ((i, item) in pModel.variant.withIndex()) {
                        if (pModel.variant[i].vId == variantId) {
                            val price = item.vPrice
                            val vDiscountPrice: Double =
                                (price - ((price * item.vDiscount) / 100)).toDouble()
                            binding.tvPrice.text =
                                resources.getString(R.string.Rs) + price.toString()
                            binding.tvDiscountPrice.text =
                                resources.getString(R.string.Rs) + vDiscountPrice.toString()
                            binding.tvQuantity.text = item.vQuantityInCart.toString()
                            binding.tvUnit.text = item.vUnitQuantity.toString() + item.vUnit
                            binding.tvOff.text = item.vDiscount.toString() + "% off"

                            if (item.vQuantityInCart > 0) {
                                binding.addButton.visibility = View.GONE
                                binding.llMinusPlusLayout.visibility = View.VISIBLE
                            } else {
                                binding.addButton.visibility = View.VISIBLE
                                binding.llMinusPlusLayout.visibility = View.GONE
                            }
                        }
                    }
                }

            }
        }.start()
    }

    private fun showVariantDialog(
        productDetailModel: ProductDetailModel,
    ) {
        var alertDialog: AlertDialog? = null
        val productName = productDetailModel.productDetails.productName.split("$")
        val prductDescription = productDetailModel.productDetails.description.split("$")

        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.custom_variant_dialog, null, false)

        val variantList = view.findViewById(R.id.rv_variant_list) as RecyclerView
        variantList.layoutManager = LinearLayoutManager(this)

        val customVariantAdapter = ProductDetailVariantAdapter(
            productDetailModel.productDetails.variant,
            object : OnitemClickListener {
                @SuppressLint("SetTextI18n")
                override fun onclick(position: Int, data: String) {
                    // Variant Click Handle
                    variantId = productDetailModel.productDetails.variant[position].vId

                    lifecycleScope.launch(Dispatchers.IO) {
                        val isVariantExist =
                            AppDataBase.getInstance(this@ProductDetailActivity).productDao()
                                .isProductExist(productId, variantId)
                        if (isVariantExist) {
                            val item =
                                AppDataBase.getInstance(this@ProductDetailActivity).productDao()
                                    .getSingleProduct(productId, variantId)
                            runOnUiThread {
                                val price = item.vPrice
                                val vDiscountPrice: Double =
                                    price - ((price * item.vDiscount) / 100)
                                binding.tvPrice.text =
                                    resources.getString(R.string.Rs) + price.toString()
                                binding.tvDiscountPrice.text =
                                    resources.getString(R.string.Rs) + vDiscountPrice.toString()
                                binding.tvQuantity.text = item.itemQuantity.toString()
                                binding.tvUnit.text = item.vUnitQuantity.toString() + item.vUnit
                                binding.tvOff.text = item.vDiscount.toString() + "% off"
                                binding.addButton.visibility = View.GONE
                                binding.llMinusPlusLayout.visibility = View.VISIBLE

                            }
                        } else {
                            runOnUiThread {
                                val price =
                                    productDetailModel.productDetails.variant[position].vPrice
                                val vDiscountPrice: Double =
                                    price - ((price * productDetailModel.productDetails.variant[position].vDiscount) / 100).toDouble()
                                binding.tvPrice.text =
                                    resources.getString(R.string.Rs) + price.toString()
                                binding.tvDiscountPrice.text =
                                    resources.getString(R.string.Rs) + vDiscountPrice.toString()
                                binding.tvQuantity.text =
                                    productDetailModel.productDetails.variant[position].vQuantityInCart.toString()
                                binding.tvUnit.text =
                                    productDetailModel.productDetails.variant[position].vUnitQuantity.toString() + productDetailModel.productDetails.variant[position].vUnit
                                binding.tvOff.text =
                                    productDetailModel.productDetails.variant[position].vDiscount.toString() + "% off"
                                binding.addButton.visibility = View.GONE
                                binding.llMinusPlusLayout.visibility = View.VISIBLE

                                if (productDetailModel.productDetails.variant[position].vQuantityInCart > 0) {
                                    binding.addButton.visibility = View.GONE
                                    binding.llMinusPlusLayout.visibility = View.VISIBLE
                                } else {
                                    binding.addButton.visibility = View.VISIBLE
                                    binding.llMinusPlusLayout.visibility = View.GONE
                                }
                            }
                        }
                    }


                    /* checkProductInCart(
                         productDetailModel.productDetails.variant,
                         productId,
                         variantId,
                         position
                     )
                     calculateAmount()*/

                    alertDialog?.dismiss()
                }
            })
        variantList.adapter = customVariantAdapter
        val title = view.findViewById(R.id.tv_title) as TextView
        val description = view.findViewById(R.id.tv_description) as TextView
        val ivClose = view.findViewById(R.id.ivclose) as ImageView
        title.text = productName[0]
        description.text = prductDescription[0]

        materialAlertDialogBuilder.setView(view)
        alertDialog = materialAlertDialogBuilder.create()
        alertDialog.show()
        ivClose.setOnClickListener { alertDialog.dismiss() }
    }


    // On Variant Selection
    private fun checkProductInCart(
        variant: List<ProductDetailModel.ProductDetails.Variant>,
        product_Id: String,
        variant_id: String,
        position: Int
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(PRODUCT_ID, product_Id)
        jsonObject.addProperty(VARIANT_ID, variant_id)

        RetrofitClient.instance.checkProductInCartRequest(
            "",
            Constant.DEVICE_TYPE, jsonObject
        )
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
                                (variant.get(position).vPrice - ((variant.get(position).vPrice * variant.get(
                                    position
                                ).vDiscount) / 100)).toDouble()

                        } else {
                            vDiscountPrice = price.toDouble()
                            binding.tvPrice.visibility = View.GONE
                        }

                        var vMaxQuantity = variant.get(position).vQuantity
                        binding.tvPrice.text = price.toString()
                        binding.tvDiscountPrice.text = vDiscountPrice.toString()
                        binding.tvUnit.text =
                            variant.get(position).vUnitQuantity.toString() + variant.get(position).vUnit

                        if (response.body()?.status == true) {
                            binding.tvQuantity.text =
                                response.body()!!.productQuantity.toString()
                            binding.llMinusPlusLayout.visibility = View.VISIBLE
                            binding.addButton.visibility = View.GONE

                            // Save Count in Local
                            /*lifecycleScope.launch(Dispatchers.IO) {
                                Utility().updateProduct(
                                    this@ProductDetailActivity,
                                    product_Id,
                                    variant_id,
                                    response.body()!!.productQuantity * binding.tvDiscountPrice.text.toString()
                                        .toDouble()
                                )
                            }*/

                        } else {
                            binding.llMinusPlusLayout.visibility = View.GONE
                            binding.addButton.visibility = View.VISIBLE
                        }

                    }
                }

                override fun onFailure(call: Call<CheckProductInCartModel>, t: Throwable) {
                    Toast.makeText(this@ProductDetailActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun calculateAmount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listofProduct = Utility().getAllProductList(applicationContext)
            runOnUiThread {
                if (listofProduct.isNotEmpty()) {
                    binding.llCartLayout.visibility = View.VISIBLE
                    binding.cartView.tvItems.text = listofProduct.size.toString()
                    var amount = 0.0
                    for (item in listofProduct) {
                        amount += (item.vPrice - (item.vPrice * item.vDiscount) / 100) * item.itemQuantity
                    }
                    binding.cartView.tvTotalAmount.text = amount.toString()
                    binding.cartView.tvTotalAmount.text = Utility().formatTotalAmount(amount).toString()
                } else {
                    binding.llCartLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun updateItemToLocalDB(
        action: String,
        product: ProductDetailModel.ProductDetails
    ) {

        var vPrice = 0.0
        var vDiscount = 0
        var vUnitQuantity = 0
        var vUnit = ""
        var totalMaxQuantity = 0

        for (item in product.variant) {
            if (item.vId == variantId) {
                vPrice = item.vPrice.toDouble()
                vDiscount = item.vDiscount
                vUnitQuantity = item.vUnitQuantity
                vUnit = item.vUnit
                totalMaxQuantity = item.vQuantity
            }
        }
        when (action) {
            "add" -> {
                binding.llMinusPlusLayout.visibility = View.VISIBLE
                binding.addButton.visibility = View.GONE
                binding.tvQuantity.text = "1"
                val quantity = 1
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDataBase.getInstance(this@ProductDetailActivity).productDao().insert(
                        ProductDetail(
                            productId,
                            variantId,
                            quantity,
                            product.productName,
                            product.imgUrl.get(0),
                            vPrice,
                            vDiscount,
                            vUnitQuantity,
                            vUnit,
                            totalMaxQuantity
                        )
                    )
                }

            }
            "plus" -> {
                var quantity: Int = binding.tvQuantity.text.toString().toInt()
                quantity++
                binding.tvQuantity.text = quantity.toString()
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().updateProduct(
                        this@ProductDetailActivity,
                        productId,
                        variantId,
                        quantity
                    )
                }
            }
            "minus" -> {
                var quantity: Int = binding.tvQuantity.text.toString().toInt()
                quantity--
                if (quantity == 0) {
                    binding.llMinusPlusLayout.visibility = View.GONE
                    binding.addButton.visibility = View.VISIBLE
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDataBase.getInstance(this@ProductDetailActivity).productDao()
                            .deleteProduct(product.id, variantId)
                    }
                    calculateAmount()
                    return
                }
                binding.tvQuantity.text = quantity.toString()
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().updateProduct(
                        this@ProductDetailActivity,
                        productId,
                        variantId,
                        quantity
                    )
                }
            }
        }
        calculateAmount()
    }

    override fun onResume() {
        super.onResume()
        calculateAmount()
    }
}