package naya.ganj.app.data.category.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.adapter.ProductDetailVariantAdapter
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.category.model.CheckProductInCartModel
import naya.ganj.app.data.category.model.ProductDetailModel
import naya.ganj.app.data.category.viewmodel.ProductDetailViewModel
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.databinding.ActivityProductDetailBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
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
        binding.include.ivBackArrow.setOnClickListener {finish() }
        if(app.user.getAppLanguage()==1){
            binding.include.toolbarTitle.text =resources.getString(R.string.product_details_h)
        }else
        {
            binding.include.toolbarTitle.text = "Product Detail"
        }

        viewModel = ViewModelProvider(this)[ProductDetailViewModel::class.java]
        if (intent.extras != null) {
            productId = intent.getStringExtra(PRODUCT_ID).toString()
            variantId = intent.getStringExtra(VARIANT_ID).toString()
        }

        binding.addButton.setOnClickListener {

            if(Utility.isAppOnLine(this@ProductDetailActivity,object : OnInternetCheckListener{
                    override fun onInternetAvailable() {
                        updateItemToLocalDB("insert", productDetailModel.productDetails,binding.addButton)
                    }
                }))
            updateItemToLocalDB("insert", productDetailModel.productDetails,binding.addButton)
        }
        binding.tvPlus.setOnClickListener {
            binding.tvPlus.isEnabled=true
            if(Utility.isAppOnLine(this@ProductDetailActivity,object : OnInternetCheckListener{
                    override fun onInternetAvailable() {
                        updateItemToLocalDB("plus", productDetailModel.productDetails,binding.tvPlus)
                    }
                }))
            updateItemToLocalDB("plus", productDetailModel.productDetails,binding.tvPlus)
        }
        binding.tvMinus.setOnClickListener {
            if(Utility.isAppOnLine(this@ProductDetailActivity,object : OnInternetCheckListener{
                    override fun onInternetAvailable() {
                        updateItemToLocalDB("minus", productDetailModel.productDetails,binding.tvMinus)
                    }
                }))
            binding.tvMinus.isEnabled=false
            updateItemToLocalDB("minus", productDetailModel.productDetails,binding.tvMinus)
        }

        binding.llVariantLayout.setOnClickListener {
            showVariantDialog(productDetailModel)
        }

        binding.llCartLayout.setOnClickListener { v ->
            startActivity(Intent(this@ProductDetailActivity, MyCartActivity::class.java))
            finish()
        }

        if(Utility.isAppOnLine(this@ProductDetailActivity,object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getProductData(productId)
                }
            })){
            getProductData(productId)
        }

    }


    private fun getProductData(productId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.rootLayout.visibility = View.GONE

        val jsonObject = JsonObject()
        jsonObject.addProperty(PRODUCT_ID, productId)

        viewModel.getProductDetai(this@ProductDetailActivity, jsonObject)
            .observe(this) {
                setProductData(it.productDetails)
                productDetailModel = it
            }
    }

    private fun setProductData(pModel: ProductDetailModel.ProductDetails) {
        try {
            Log.e("TAG", "ImageURL: " + app.user.getUserDetails()?.configObj?.productImgUrl)

            val imgURL = app.user.getUserDetails()?.configObj?.productImgUrl + pModel.imgUrl[0]
            Glide.with(this@ProductDetailActivity).load(imgURL).error(R.drawable.default_image)
                .into(binding.ivProductImage)
        } catch (e: Exception) {
            e.printStackTrace()
            binding.ivProductImage.setBackgroundResource(R.drawable.default_image)
        }

        binding.tvProductName.text = Utility.convertLanguage(pModel.productName, app)
        if (pModel.description != null) {
            binding.tvDescription.text = Utility.convertLanguage(pModel.description, app)
            binding.tvDescription.visibility = View.VISIBLE
        } else {
            binding.tvDescription.visibility = View.GONE
        }

        if (app.user.getAppLanguage() == 1) {
            if (pModel.productName.contains("$"))
                binding.tvProductName.setTypeface(Typeface.createFromAsset(assets, "agrawide.ttf"))
        }

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

                    if(app.user.getAppLanguage()==1){
                        binding.tvOff.text = item.vDiscount.toString() + "% "+resources.getString(R.string.off_h)
                        binding.addButton.text=resources.getString(R.string.add_h)
                        binding.tvNetWet.text=resources.getString(R.string.net_weight_h)
                    }else{
                        binding.tvOff.text = item.vDiscount.toString() + "% off"
                    }

                    binding.addButton.visibility = View.GONE
                    binding.llMinusPlusLayout.visibility = View.VISIBLE


                }
            } else {
                runOnUiThread {
                    for ((i, item) in pModel.variant.withIndex()) {
                        if (pModel.variant[i].vId == variantId) {
                            val price = item.vPrice
                            val vDiscountPrice: Double =
                                (price - ((price * item.vDiscount) / 100))
                            binding.tvPrice.text =
                                resources.getString(R.string.Rs) + price.toString()
                            binding.tvDiscountPrice.text =
                                resources.getString(R.string.Rs) + vDiscountPrice.toString()
                            binding.tvQuantity.text = item.vQuantityInCart.toString()
                            binding.tvUnit.text = item.vUnitQuantity.toString() + item.vUnit

                            if(app.user.getAppLanguage()==1){
                                binding.tvOff.text = item.vDiscount.toString() + "% "+resources.getString(R.string.off_h)
                                binding.addButton.text=resources.getString(R.string.add_h)
                                binding.tvNetWet.text=resources.getString(R.string.net_weight_h)
                            }else{
                                binding.tvOff.text = item.vDiscount.toString() + "% off"
                            }

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

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            binding.progressBar.visibility = View.GONE
            binding.rootLayout.visibility = View.VISIBLE
        }, 200)
    }

    private fun showVariantDialog(
        productDetailModel: ProductDetailModel,
    ) {
        var alertDialog: AlertDialog? = null

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
                    if(Utility.isAppOnLine(this@ProductDetailActivity,object : OnInternetCheckListener {
                            override fun onInternetAvailable() {
                                variantId = productDetailModel.productDetails.variant[position].vId
                                if (app.user.getLoginSession()) {
                                    checkProductInCart(
                                        productDetailModel.productDetails.variant,
                                        productId,
                                        variantId,
                                        position
                                    )
                                } else {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val isVariantExist =
                                            AppDataBase.getInstance(this@ProductDetailActivity).productDao()
                                                .isProductExist(productId, variantId)
                                        if (isVariantExist) {
                                            val singleProduct =
                                                AppDataBase.getInstance(this@ProductDetailActivity).productDao()
                                                    .getSingleProduct(productId, variantId)
                                            runOnUiThread {
                                                val price = singleProduct.vPrice
                                                val vDiscountPrice: Double =
                                                    price - ((price * singleProduct.vDiscount) / 100)
                                                binding.tvPrice.text =
                                                    resources.getString(R.string.Rs) + price.toString()
                                                binding.tvDiscountPrice.text =
                                                    resources.getString(R.string.Rs) + vDiscountPrice.toString()
                                                binding.tvQuantity.text = singleProduct.itemQuantity.toString()
                                                binding.tvUnit.text =
                                                    singleProduct.vUnitQuantity.toString() + singleProduct.vUnit
                                                binding.tvOff.text =
                                                    singleProduct.vDiscount.toString() + "% off"
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
                                }
                                alertDialog?.dismiss()
                            }
                        })){
                        variantId = productDetailModel.productDetails.variant[position].vId
                        if (app.user.getLoginSession()) {
                            checkProductInCart(
                                productDetailModel.productDetails.variant,
                                productId,
                                variantId,
                                position
                            )
                        } else {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val isVariantExist =
                                    AppDataBase.getInstance(this@ProductDetailActivity).productDao()
                                        .isProductExist(productId, variantId)
                                if (isVariantExist) {
                                    val singleProduct =
                                        AppDataBase.getInstance(this@ProductDetailActivity).productDao()
                                            .getSingleProduct(productId, variantId)
                                    runOnUiThread {
                                        val price = singleProduct.vPrice
                                        val vDiscountPrice: Double =
                                            price - ((price * singleProduct.vDiscount) / 100)
                                        binding.tvPrice.text =
                                            resources.getString(R.string.Rs) + price.toString()
                                        binding.tvDiscountPrice.text =
                                            resources.getString(R.string.Rs) + vDiscountPrice.toString()
                                        binding.tvQuantity.text = singleProduct.itemQuantity.toString()
                                        binding.tvUnit.text =
                                            singleProduct.vUnitQuantity.toString() + singleProduct.vUnit
                                        binding.tvOff.text =
                                            singleProduct.vDiscount.toString() + "% off"
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
                        }
                        alertDialog?.dismiss()
                    }
                }
            },app,this@ProductDetailActivity)
        variantList.adapter = customVariantAdapter
        val title = view.findViewById(R.id.tv_title) as TextView
        val description = view.findViewById(R.id.tv_description) as TextView
        val ivClose = view.findViewById(R.id.ivclose) as ImageView
        title.text = Utility.convertLanguage(productDetailModel.productDetails.productName,app)

        if(app.user.getAppLanguage()==1){
            if (productDetailModel.productDetails.productName.contains("$"))
                title.setTypeface(Typeface.createFromAsset(assets, "agrawide.ttf"))
        }

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
            app.user.getUserDetails()?.userId,
            Constant.DEVICE_TYPE, jsonObject
        )
            .enqueue(object : Callback<CheckProductInCartModel> {
                override fun onResponse(
                    call: Call<CheckProductInCartModel>,
                    response: Response<CheckProductInCartModel>
                ) {
                    if (response.body()!!.status) {
                        setVariantData(variant, position, response)
                    } else {
                        setVariantData(variant, position, response)
                    }
                }
                override fun onFailure(call: Call<CheckProductInCartModel>, t: Throwable) {
                    Utility.serverNotResponding(this@ProductDetailActivity,t.message.toString())
                }
            })
    }

    private fun calculateAmount() {
        Log.e("TAG", "calculateAmount: " )
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
        product: ProductDetailModel.ProductDetails,
        addremoveText: TextView
    ) {

        var vPrice = 0.0
        var vDiscount = 0
        var vUnitQuantity = "0"
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
            "insert" -> {
                if(app.user.getLoginSession()){
                    val jsonObject = JsonObject()
                    jsonObject.addProperty(Constant.PRODUCT_ID, productId)
                    jsonObject.addProperty(Constant.ACTION, "add")
                    jsonObject.addProperty(Constant.VARIANT_ID, variantId)
                    jsonObject.addProperty(Constant.PROMO_CODE, "")

                    RetrofitClient.instance.addremoveItemRequest(
                        app.user.getUserDetails()?.userId,
                        Constant.DEVICE_TYPE,
                        jsonObject
                    )
                        .enqueue(object : Callback<AddRemoveModel> {
                            override fun onResponse(
                                call: Call<AddRemoveModel>,
                                response: Response<AddRemoveModel>
                            ) {
                                if (response.isSuccessful) {
                                    if (response.body()!!.status) {
                                        addremoveText.isEnabled = true
                                        binding.llMinusPlusLayout.visibility = View.VISIBLE
                                        binding.addButton.visibility = View.GONE
                                        binding.tvQuantity.text = "1"
                                        val quantity = 1

                                        val imgURL = try {
                                            product.imgUrl[0]
                                        } catch (e: Exception) {
                                            ""
                                        }

                                        lifecycleScope.launch(Dispatchers.IO) {
                                            AppDataBase.getInstance(this@ProductDetailActivity)
                                                .productDao().insert(
                                                    ProductDetail(
                                                        productId,
                                                        variantId,
                                                        quantity,
                                                        product.productName,
                                                        imgURL,
                                                        vPrice,
                                                        vDiscount,
                                                        vUnitQuantity.toString(),
                                                        vUnit,
                                                        totalMaxQuantity
                                                    )
                                                )
                                        }
                                    }else{
                                        Utility.serverNotResponding(this@ProductDetailActivity,response.message())
                                    }
                                }else{
                                    Utility.serverNotResponding(this@ProductDetailActivity,response.message())
                                }
                            }

                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(this@ProductDetailActivity,t.message.toString())
                            }
                        })

                }else{
                    binding.llMinusPlusLayout.visibility = View.VISIBLE
                    binding.addButton.visibility = View.GONE
                    binding.tvQuantity.text = "1"
                    val quantity = 1
                    lifecycleScope.launch(Dispatchers.IO) {
                        val imgURL = try {
                            product.imgUrl[0]
                        } catch (e: Exception) {
                            ""
                        }
                        AppDataBase.getInstance(this@ProductDetailActivity).productDao().insert(
                            ProductDetail(
                                productId,
                                variantId,
                                quantity,
                                product.productName,
                                imgURL,
                                vPrice,
                                vDiscount,
                                vUnitQuantity.toString(),
                                vUnit,
                                totalMaxQuantity
                            )
                        )

                        if (app.user.getLoginSession()) {
                            Utility().addRemoveItem(
                                app.user.getUserDetails()?.userId,
                                "add",
                                productId,
                                variantId,
                                ""
                            )
                        }

                    }
                }
            }
            "plus" -> {
                    if(app.user.getLoginSession()){
                        var quantity: Int = binding.tvQuantity.text.toString().toInt()
                        if(quantity>=totalMaxQuantity){
                            addremoveText.isEnabled=true
                            Utility.showToast(this@ProductDetailActivity,"Sorry! you can not add more item for this product")
                            return
                        }

                        val jsonObject = JsonObject()
                        jsonObject.addProperty(Constant.PRODUCT_ID, productId)
                        jsonObject.addProperty(Constant.ACTION, "add")
                        jsonObject.addProperty(Constant.VARIANT_ID, variantId)
                        jsonObject.addProperty(Constant.PROMO_CODE, "")

                        RetrofitClient.instance.addremoveItemRequest(
                            app.user.getUserDetails()?.userId,
                            Constant.DEVICE_TYPE,
                            jsonObject
                        )
                            .enqueue(object : Callback<AddRemoveModel> {
                                override fun onResponse(
                                    call: Call<AddRemoveModel>,
                                    response: Response<AddRemoveModel>
                                ) {
                                    if(response.isSuccessful){
                                        if(response.body()!!.status){
                                            addremoveText.isEnabled=true
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
                                        }else{
                                            Utility.serverNotResponding(this@ProductDetailActivity,response.message())
                                        }
                                    }else{
                                        Utility.serverNotResponding(this@ProductDetailActivity,response.message())
                                    }
                                }

                                override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                    Utility.serverNotResponding(this@ProductDetailActivity,t.message.toString())
                                }
                            })
                    }else{
                        var quantity: Int = binding.tvQuantity.text.toString().toInt()
                        if(quantity>=totalMaxQuantity){
                            addremoveText.isEnabled=true
                            Utility.showToast(this@ProductDetailActivity,"Sorry! you can not add more item for this product")
                            return
                        }

                        quantity++
                        binding.tvQuantity.text = quantity.toString()
                        addremoveText.isEnabled=true
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
            "minus" -> {
                if (app.user.getLoginSession()) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty(Constant.PRODUCT_ID, productId)
                    jsonObject.addProperty(Constant.ACTION, "remove")
                    jsonObject.addProperty(Constant.VARIANT_ID, variantId)
                    jsonObject.addProperty(Constant.PROMO_CODE, "")

                    RetrofitClient.instance.addremoveItemRequest(
                        app.user.getUserDetails()?.userId,
                        Constant.DEVICE_TYPE,
                        jsonObject
                    )
                        .enqueue(object : Callback<AddRemoveModel> {
                            override fun onResponse(
                                call: Call<AddRemoveModel>,
                                response: Response<AddRemoveModel>
                            ) {
                                if(response.isSuccessful){

                                    if(response.body()!!.status){
                                        addremoveText.isEnabled=true
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

                                    }else{
                                        Utility.serverNotResponding(this@ProductDetailActivity,response.message())
                                    }

                                }else{
                                    Utility.serverNotResponding(this@ProductDetailActivity,response.message())
                                }
                            }
                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(this@ProductDetailActivity,t.message.toString())
                            }
                        })
                }else{
                    addremoveText.isEnabled=true
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
        }
        Handler(Looper.getMainLooper()).postDelayed(Runnable { calculateAmount() },300)
    }

    override fun onResume() {
        super.onResume()
        calculateAmount()
    }

    fun setVariantData(
        variant: List<ProductDetailModel.ProductDetails.Variant>,
        position: Int,
        response: Response<CheckProductInCartModel>
    ) {
        val price = variant[position].vPrice
        val vDiscountPrice: Double
        if (variant[position].vDiscount > 0) {
            vDiscountPrice =
                (variant[position].vPrice - ((variant.get(position).vPrice * variant.get(
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

        if (response.body()!!.status) {
            binding.tvQuantity.text = response.body()!!.productQuantity.toString()
            binding.llMinusPlusLayout.visibility = View.VISIBLE
            binding.addButton.visibility = View.GONE
            lifecycleScope.launch(Dispatchers.IO) {
                AppDataBase.getInstance(this@ProductDetailActivity).productDao()
                    .updateProduct(
                        response.body()!!.productQuantity,
                        productId,
                        variantId
                    )
            }
        } else {
            binding.llMinusPlusLayout.visibility = View.GONE
            binding.addButton.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}