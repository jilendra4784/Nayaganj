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
import naya.ganj.app.roomdb.entity.ApiManager
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.LocalDBManager
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

        if (app.user.getLoginSession()) {
            getProductList("", app.user.getUserDetails()?.userId, categoryId)
        } else {
            getProductList("", "", categoryId)
        }
        binding.llCartLayout.setOnClickListener {
            val intent = Intent(this@ProductListActivity, MyCartActivity::class.java)
            intent.putExtra("ORDER_ID", "")
            intent.putExtra(isListActivity, true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

    }

    private fun getProductList(
        text: String,
        userId: String?,
        cateId: String?,
    ) {
        binding.productShimmer.showShimmer(true);
        binding.productShimmer.visibility = View.VISIBLE
        binding.tvNoProduct.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            if (Utility.isAppOnLine(this@ProductListActivity, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        getProductListRequestData(text, userId, cateId)
                    }
                })) {
                getProductListRequestData(text, userId, cateId)
            }
        }, 500)
    }

    override fun onClickAddOrRemoveItem(
        action: String, productDetail: ProductDetail, addremoveText: TextView
    ) {
        when (action) {
            Constant.INSERT -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(this@ProductListActivity,app,action,productDetail,addremoveText)
                } else {
                    LocalDBManager.insertItemInLocal(this@ProductListActivity, productDetail)
                }
            }
            Constant.ADD -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(this@ProductListActivity,app,action,productDetail,addremoveText)
                } else {
                    addremoveText.isEnabled = true
                    LocalDBManager.updateProduct(this@ProductListActivity,productDetail)
                }
            }
            Constant.REMOVE -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(this@ProductListActivity,app,action,productDetail,addremoveText)
                } else {
                    addremoveText.isEnabled = true
                    if (productDetail.itemQuantity > 0) {
                        LocalDBManager.updateProduct(this@ProductListActivity,productDetail)
                    } else {
                        LocalDBManager.deleteProduct(this@ProductListActivity,productDetail)
                    }
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({showCartLayout()},200)

    }

    fun showCartLayout() {
        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch(Dispatchers.IO) {
                val listofProduct = Utility().getAllProductList(applicationContext)
                runOnUiThread {
                    Log.e(TAG, "showCartLayout: listofProduct size"+listofProduct.size )
                    if (listofProduct.isNotEmpty()) {
                        binding.llCartLayout.visibility = View.VISIBLE
                        binding.cartLayout.tvItems.text = listofProduct.size.toString()
                        var amount = 0.0
                        for (item in listofProduct) {
                            amount += (item.vPrice - (item.vPrice * item.vDiscount) / 100) * item.itemQuantity
                        }
                        binding.cartLayout.tvTotalAmount.text =
                            Utility().formatTotalAmount(amount).toString()
                    } else {
                        binding.llCartLayout.visibility = View.GONE
                    }
                }
            }
        },200)

    }


    override fun onResume() {
        super.onResume()
         adapter?.notifyDataSetChanged()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { showCartLayout() }, 400)
    }

    fun getProductListRequestData(text: String, userId: String?, cateId: String?) {
        val json = JsonObject()
        json.addProperty(Constant.CATEGORY_ID, cateId)
        json.addProperty(Constant.TEXT, text)
        json.addProperty(Constant.pageIndex, "0")

        viewModel.getProductList(this@ProductListActivity, userId, Constant.DEVICE_TYPE, json)
            .observe(this) {
                if (it.productList.isNotEmpty()) {
                    for (product in it.productList) {
                        for ((i, variant) in product.variant.withIndex()) {
                            if (variant.vQuantityInCart > 0) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val isProductExist = async {
                                        AppDataBase.getInstance(this@ProductListActivity)
                                            .productDao().isProductExist(product.id, variant.vId)
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
                                                    variant.vQuantityInCart, product.id, variant.vId
                                                )
                                        }
                                    }
                                }

                            }
                        }
                    }

                    binding.productShimmer.stopShimmer()
                    binding.productShimmer.visibility = View.GONE
                    binding.productShimmer.hideShimmer()
                    adapter = ProductListAdapter(
                        this@ProductListActivity,
                        this@ProductListActivity,
                        it.productList,
                        this@ProductListActivity,
                        app
                    )
                    binding.productList.layoutManager =
                        LinearLayoutManager(this@ProductListActivity)
                    binding.productList.isNestedScrollingEnabled = false
                    binding.productList.adapter = adapter

                    binding.productList.setHasFixedSize(true)
                    binding.tvNoProduct.visibility = View.GONE
                    binding.productList.visibility = View.VISIBLE

                } else {
                    binding.productShimmer.stopShimmer()
                    binding.productShimmer.visibility = View.GONE
                    binding.productShimmer.hideShimmer()
                    binding.tvNoProduct.visibility = View.VISIBLE
                    binding.productList.visibility = View.GONE
                    binding.llCartLayout.visibility = View.GONE
                }
            }
    }

}