package naya.ganj.app.data.category.view

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.adapter.ProductListAdapter
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.category.viewmodel.ProductListViewModel
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.databinding.ActivityProductListBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.isListActivity
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProductListActivity : AppCompatActivity(), OnclickAddOremoveItemListener {

    lateinit var viewModel: ProductListViewModel
    lateinit var binding: ActivityProductListBinding
    lateinit var app: Nayaganj
    var adapter: ProductListAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductListViewModel::class.java]
        app = applicationContext as Nayaganj

        binding.include7.ivBackArrow.setOnClickListener { finish() }
        if (app.user.getAppLanguage() == 1) {
            binding.include7.toolbarTitle.text = resources.getString(R.string.product_list_h)
        } else {
            binding.include7.toolbarTitle.text = "Product List"
        }

        val categoryId = intent.getStringExtra(Constant.CATEGORY_ID)
        val categoryName = intent.getStringExtra(Constant.CATEGORY_NAME)
        binding.include7.toolbarTitle.text = categoryName

        if (categoryId == null || categoryId == "") {
            // It will act as a Search Activity
            binding.frameLayout.visibility = View.VISIBLE

            binding.editText.setOnEditorActionListener { v, actionId, event ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        if (binding.editText.text.toString().length >= 3) {
                            if (app.user.getLoginSession()) {
                                getProductList(
                                    binding.editText.text.toString(),
                                    app.user.getUserDetails()?.userId,
                                    ""
                                )
                            } else {
                                getProductList(binding.editText.text.toString(), "", "")
                            }
                        } else {
                            binding.tvNoProduct.visibility = View.VISIBLE
                            binding.productList.visibility = View.GONE
                            binding.llCartLayout.visibility = View.GONE
                        }
                        true
                    }
                    else -> false
                }
            }
            /*  binding.editText.addTextChangedListener(object : TextWatcher {
                  override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                  }

                  override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                  }

                  override fun afterTextChanged(p0: Editable?) {
                      if (p0.toString().length >= 3) {
                          if (app.user.getLoginSession()) {
                              Log.e("TAG", "getProductListRequestData: Search" + "")
                              getProductList(p0.toString(), app.user.getUserDetails()?.userId, "")
                          } else {
                              getProductList(p0.toString(), "", "")
                          }
                      } else {
                          if (p0.toString().isEmpty()) {
                              binding.tvNoProduct.visibility = View.VISIBLE
                              binding.productList.visibility = View.GONE
                              binding.llCartLayout.visibility = View.GONE
                          }
                      }
                  }
              })*/

            if (app.user.getAppLanguage() == 1) {
                binding.editText.hint = resources.getString(R.string.search_here_h)
            } else {
                binding.editText.hint = "Search 100+ product"
            }

        } else {
            binding.include7.customToolbar.visibility = View.VISIBLE
            if (app.user.getLoginSession()) {
                getProductList("", app.user.getUserDetails()?.userId, categoryId)
            } else {
                getProductList("", "", categoryId)
            }
        }
        binding.llCartLayout.setOnClickListener {
            val intent = Intent(this@ProductListActivity, MyCartActivity::class.java)
            intent.putExtra("ORDER_ID", "")
            intent.putExtra(isListActivity, true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        binding.arrowIcon.setOnClickListener {
            finish()
        }
    }

    private fun getProductList(
        text: String,
        userId: String?,
        cateId: String?,
    ) {

        if(Utility.isAppOnLine(this@ProductListActivity,object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getProductListRequestData(text,userId,cateId)
                }
            })){
            getProductListRequestData(text,userId,cateId)
        }
    }

    override fun onClickAddOrRemoveItem(
        action: String,
        productDetail: ProductDetail,
        addremoveText:TextView
    ) {
        when (action) {
            Constant.INSERT -> {
                if (app.user.getLoginSession()) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty(Constant.PRODUCT_ID, productDetail.productId)
                    jsonObject.addProperty(Constant.ACTION, "add")
                    jsonObject.addProperty(Constant.VARIANT_ID, productDetail.variantId)
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
                                    if(response.body()!!.status)
                                        addremoveText.isEnabled=true
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        Utility().insertProduct(this@ProductListActivity, productDetail)
                                    }
                                }else{
                                    Utility.serverNotResponding(this@ProductListActivity,response.message())
                                }
                            }

                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(this@ProductListActivity,t.message.toString())
                            }
                        })


                }else{
                    lifecycleScope.launch(Dispatchers.IO) {
                        Utility().insertProduct(this@ProductListActivity, productDetail)
                    }
                }
            }
            Constant.ADD -> {
                if(app.user.getLoginSession()){
                    val jsonObject = JsonObject()
                    jsonObject.addProperty(Constant.PRODUCT_ID, productDetail.productId)
                    jsonObject.addProperty(Constant.ACTION, action)
                    jsonObject.addProperty(Constant.VARIANT_ID, productDetail.variantId)
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
                                    if(response.body()!!.status)
                                        addremoveText.isEnabled=true
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        AppDataBase.getInstance(this@ProductListActivity).productDao().updateProduct(productDetail.itemQuantity, productDetail.productId, productDetail.variantId)
                                    }
                                }else{
                                    Utility.serverNotResponding(this@ProductListActivity,response.message())
                                }
                            }

                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(this@ProductListActivity,t.message.toString())
                            }
                        })
                }else{
                    addremoveText.isEnabled=true
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDataBase.getInstance(this@ProductListActivity).productDao().updateProduct(productDetail.itemQuantity, productDetail.productId, productDetail.variantId)
                    }
                }
            }
            Constant.REMOVE -> {
                if (app.user.getLoginSession()) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty(Constant.PRODUCT_ID, productDetail.productId)
                    jsonObject.addProperty(Constant.ACTION, action)
                    jsonObject.addProperty(Constant.VARIANT_ID, productDetail.variantId)
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
                                    if(response.body()!!.status)
                                        addremoveText.isEnabled=true
                                    if(productDetail.itemQuantity>0){
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            AppDataBase.getInstance(this@ProductListActivity).productDao().updateProduct(productDetail.itemQuantity, productDetail.productId, productDetail.variantId)
                                        }
                                    }else{
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            AppDataBase.getInstance(this@ProductListActivity).productDao().deleteProduct(productDetail.productId, productDetail.variantId)
                                        }
                                    }

                                }else{
                                    Utility.serverNotResponding(this@ProductListActivity,response.message())
                                }
                            }

                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(this@ProductListActivity,t.message.toString())
                            }
                        })
                }else{
                    addremoveText.isEnabled=true
                    if (productDetail.itemQuantity > 0) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            Utility().updateProduct(
                                this@ProductListActivity,
                                productDetail.productId,
                                productDetail.variantId,
                                productDetail.itemQuantity
                            )
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            Utility().deleteProduct(
                                this@ProductListActivity,
                                productDetail.productId,
                                productDetail.variantId
                            )
                        }
                    }
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(Runnable { showCartLayout() }, 200)
    }

    private fun showCartLayout() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listofProduct = Utility().getAllProductList(applicationContext)
            runOnUiThread {
                if (listofProduct.isNotEmpty()) {
                    binding.llCartLayout.visibility = View.VISIBLE
                    binding.cartLayout.tvItems.text = listofProduct.size.toString()
                    var amount = 0.0
                    for (item in listofProduct) {
                        amount += (item.vPrice - (item.vPrice * item.vDiscount) / 100) * item.itemQuantity
                    }
                    binding.cartLayout.tvTotalAmount.text = Utility().formatTotalAmount(amount).toString()
                } else {
                    binding.llCartLayout.visibility = View.GONE
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()

        adapter?.notifyDataSetChanged()
        Thread {
            Log.e(TAG, "onResume: " + Utility().getAllProductList(this@ProductListActivity))
        }.start()

        Handler(Looper.getMainLooper()).postDelayed(Runnable { showCartLayout() }, 400)
    }
    fun getProductListRequestData(text: String, userId: String?, cateId: String?) {
        val json = JsonObject()
        json.addProperty(Constant.CATEGORY_ID, cateId)
        json.addProperty(Constant.TEXT, text)
        json.addProperty(Constant.pageIndex, "0")

        binding.progressBar.visibility = View.VISIBLE
        binding.productList.visibility = View.GONE
        viewModel.getProductList(this@ProductListActivity, userId, Constant.DEVICE_TYPE, json)
            .observe(this) {
                if (it.productList.isNotEmpty()) {
                    for (product in it.productList) {
                        for ((i, variant) in product.variant.withIndex()) {
                            if (variant.vQuantityInCart > 0) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val isProductExist = async {
                                        AppDataBase.getInstance(this@ProductListActivity)
                                            .productDao()
                                            .isProductExist(product.id, variant.vId)
                                    }.await()
                                    if (isProductExist) {
                                        val singleProduct = async {
                                            AppDataBase.getInstance(this@ProductListActivity)
                                                .productDao()
                                                .getSingleProduct(product.id, variant.vId)
                                        }.await()
                                        if (variant.vQuantityInCart > singleProduct.itemQuantity) {
                                            AppDataBase.getInstance(this@ProductListActivity)
                                                .productDao().updateProduct(
                                                    variant.vQuantityInCart,
                                                    product.id,
                                                    variant.vId
                                                )
                                        }
                                    }
                                }

                            }
                        }
                    }
                    adapter = ProductListAdapter(
                        this@ProductListActivity,
                        this@ProductListActivity,
                        it.productList, this@ProductListActivity, app
                    )
                    binding.productList.layoutManager =
                        LinearLayoutManager(this@ProductListActivity)
                    binding.productList.isNestedScrollingEnabled = false
                    binding.productList.adapter = adapter

                    binding.productList.setHasFixedSize(true)
                    binding.tvNoProduct.visibility = View.GONE
                    binding.productList.visibility = View.VISIBLE

                    binding.progressBar.visibility = View.GONE
                    binding.productList.visibility = View.VISIBLE

                } else {
                    binding.tvNoProduct.visibility = View.VISIBLE
                    binding.productList.visibility = View.GONE
                    binding.llCartLayout.visibility = View.GONE
                }
            }
    }
}