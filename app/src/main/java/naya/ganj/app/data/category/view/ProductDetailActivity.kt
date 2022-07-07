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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import naya.ganj.app.R
import naya.ganj.app.data.category.adapter.ProductDetailVariantAdapter
import naya.ganj.app.data.category.model.CheckProductInCartModel
import naya.ganj.app.data.category.model.ProductDetailModel
import naya.ganj.app.data.category.viewmodel.ProductDetailViewModel
import naya.ganj.app.databinding.ActivityProductDetailBinding
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.include.ivBackArrow.setOnClickListener { finish() }
        binding.include.toolbarTitle.text = "Product Detail"
        calculateAmount()

        viewModel = ViewModelProvider(this)[ProductDetailViewModel::class.java]
        if (intent.extras != null) {
            productId = intent.getStringExtra(PRODUCT_ID).toString()
            variantId = intent.getStringExtra(VARIANT_ID).toString()
            getProductData(productId)
        }

        binding.addButton.setOnClickListener {
            binding.addButton.visibility = View.INVISIBLE
            binding.llMinusPlusLayout.visibility = View.VISIBLE
            binding.tvQuantity.text = "1"

            lifecycleScope.launch(Dispatchers.IO) {
                Utility().addRemoveItem("add", productId, variantId, "")
            }
            Thread {
                Utility().insertProduct(
                    this,
                    productId,
                    variantId,
                    binding.tvDiscountPrice.text.toString().toDouble()
                )
            }.start()
            calculateAmount()
        }

        binding.tvPlus.setOnClickListener {
            var quantity: Int = binding.tvQuantity.text.toString().toInt()
            quantity++
            binding.tvQuantity.text = quantity.toString()

            lifecycleScope.launch(Dispatchers.IO) {
                Utility().addRemoveItem("add", productId, variantId, "")
            }
            Thread {
                Utility().updateProduct(
                    this,
                    productId,
                    variantId,
                    quantity * binding.tvDiscountPrice.text.toString().toDouble()
                )
            }.start()

            calculateAmount()
        }


        binding.tvMinus.setOnClickListener {
            var quantity: Int = binding.tvQuantity.text.toString().toInt()
            quantity--

            if (quantity == 0) {
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().addRemoveItem("remove", productId, variantId, "")
                }
                Thread { Utility().deleteProduct(this, productId, variantId) }.start()
                binding.addButton.visibility = View.VISIBLE
                binding.llMinusPlusLayout.visibility = View.GONE
                return@setOnClickListener
            }
            binding.tvQuantity.text = quantity.toString()

            lifecycleScope.launch(Dispatchers.IO) {
                Utility().addRemoveItem("remove", productId, variantId, "")
            }
            Thread {
                Utility().updateProduct(
                    this,
                    productId,
                    variantId,
                    quantity * binding.tvDiscountPrice.text.toString().toDouble()
                )
            }.start()

            calculateAmount()
        }

        binding.llVariantLayout.setOnClickListener {
            showVariantDialog(productDetailModel)
        }


        binding.llCartLayout.setOnClickListener { v ->
            val intent = Intent(this@ProductDetailActivity, naya.ganj.app.MainActivity::class.java)
            intent.putExtra("ORDER_ID", "FROM_PRODUCT_DETAIL")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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

        for ((i, item) in pModel.variant.withIndex()) {
            if (item.vId.equals(variantId)) {
                binding.tvDiscountPrice.text =
                    (pModel.variant.get(i).vPrice - (pModel.variant.get(i).vPrice * pModel.variant.get(
                        i
                    ).vDiscount) / 100).toString()
                binding.tvPrice.text = pModel.variant.get(i).vPrice.toString()
                binding.tvUnit.text =
                    pModel.variant.get(i).vUnitQuantity.toString() + pModel.variant.get(i).vUnit
                binding.tvOff.text = pModel.variant.get(i).vDiscount.toString() + "% off"
                if (pModel.variant.get(i).vQuantityInCart > 0) {
                    binding.tvQuantity.text = pModel.variant.get(i).vQuantityInCart.toString()
                    binding.addButton.visibility = View.GONE
                    binding.llMinusPlusLayout.visibility = View.VISIBLE
                } else {
                    binding.addButton.visibility = View.VISIBLE
                    binding.llMinusPlusLayout.visibility = View.GONE
                }
                break
            }
        }
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
                    variantId = productDetailModel.productDetails.variant.get(position).vId
                    checkProductInCart(
                        productDetailModel.productDetails.variant,
                        productId,
                        variantId,
                        position
                    )
                    calculateAmount()

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
            Constant.USER_ID,
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
                            lifecycleScope.launch(Dispatchers.IO) {
                                Utility().updateProduct(
                                    this@ProductDetailActivity,
                                    product_Id,
                                    variant_id,
                                    response.body()!!.productQuantity * binding.tvDiscountPrice.text.toString()
                                        .toDouble()
                                )
                            }

                        } else {
                            binding.llMinusPlusLayout.visibility = View.GONE
                            binding.addButton.visibility = View.VISIBLE
                        }
                        calculateAmount()
                    }
                }

                override fun onFailure(call: Call<CheckProductInCartModel>, t: Throwable) {
                    Toast.makeText(this@ProductDetailActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun calculateAmount() {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(300)
            val listofProduct = Utility().getAllProductList(applicationContext)
            runOnUiThread {
                if (listofProduct.isNotEmpty()) {
                    binding.llCartLayout.visibility = View.VISIBLE
                    binding.cartView.tvItems.text = listofProduct.size.toString()
                    var amount = 0.0;
                    for (item in listofProduct) {
                        amount += item.totalAmount
                    }
                    binding.cartView.tvTotalAmount.text = amount.toString()

                } else {
                    binding.llCartLayout.visibility = View.GONE
                }
            }
        }
    }


}