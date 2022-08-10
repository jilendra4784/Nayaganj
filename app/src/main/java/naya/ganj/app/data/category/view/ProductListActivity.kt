package naya.ganj.app.data.category.view

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.category.adapter.ProductListAdapter
import naya.ganj.app.data.category.viewmodel.ProductListViewModel
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.databinding.ActivityProductListBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.isListActivity
import naya.ganj.app.utility.Utility

class ProductListActivity : AppCompatActivity(), OnclickAddOremoveItemListener {

    lateinit var viewModel: ProductListViewModel
    lateinit var binding: ActivityProductListBinding
    lateinit var app: Nayaganj
    var adapter: ProductListAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ProductListViewModel::class.java)
        app = applicationContext as Nayaganj

        binding.include7.ivBackArrow.setOnClickListener { finish() }
        binding.include7.toolbarTitle.setText("Product List")
        val categoryId = intent.getStringExtra(Constant.CATEGORY_ID)

        if (categoryId == null || categoryId.equals("")) {
            // It will act as a Search Activity
            binding.frameLayout.visibility = View.VISIBLE
            binding.editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0.toString().length >= 3) {
                        binding.productList.visibility = View.VISIBLE
                        if (app.user.getLoginSession()) {
                            getProductList(p0.toString(), app.user.getUserDetails()?.userId, "")
                        } else {
                            getProductList(p0.toString(), "", "")
                        }
                    } else {
                        if (p0.toString().isEmpty()) {
                            binding.tvNoProduct.visibility = View.GONE
                            binding.productList.visibility = View.GONE
                            binding.llCartLayout.visibility = View.GONE
                        }
                    }
                }
            })

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
        val json = JsonObject()
        json.addProperty(Constant.CATEGORY_ID, cateId)
        json.addProperty(Constant.TEXT, text)
        json.addProperty(Constant.pageIndex, "0")

        binding.progressBar.visibility=View.VISIBLE
        binding.productList.visibility=View.GONE
        viewModel.getProductList(userId, Constant.DEVICE_TYPE, json)
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
                    binding.tvNoProduct.visibility = View.GONE
                    binding.productList.visibility = View.VISIBLE

                    binding.progressBar.visibility=View.GONE
                    binding.productList.visibility=View.VISIBLE

                } else {
                    binding.tvNoProduct.visibility = View.VISIBLE
                    binding.productList.visibility = View.GONE
                    binding.llCartLayout.visibility = View.GONE
                }
            }

    }


    override fun onClickAddOrRemoveItem(
        action: String,
        productDetail: ProductDetail
    ) {
        when (action) {
            Constant.INSERT -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().insertProduct(this@ProductListActivity, productDetail)
                }
                if (app.user.getLoginSession()) {
                    Utility().addRemoveItem(
                        app.user.getUserDetails()?.userId,
                        "add",
                        productDetail.productId,
                        productDetail.variantId,
                        ""
                    )
                }
            }
            Constant.ADD -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    Utility().updateProduct(
                        this@ProductListActivity,
                        productDetail.productId,
                        productDetail.variantId,
                        productDetail.itemQuantity
                    )
                }
                if (app.user.getLoginSession()) {
                    Utility().addRemoveItem(
                        app.user.getUserDetails()?.userId,
                        Constant.ADD,
                        productDetail.productId,
                        productDetail.variantId,
                        ""
                    )
                }

            }
            Constant.REMOVE -> {
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


                if (app.user.getLoginSession()) {
                    Utility().addRemoveItem(
                        app.user.getUserDetails()?.userId,
                        "remove",
                        productDetail.productId,
                        productDetail.variantId,
                        ""
                    )
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
    }
}