package naya.ganj.app.data.category.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.category.adapter.ProductListAdapter
import naya.ganj.app.data.category.viewmodel.ProductListViewModel
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.databinding.ActivityProductListBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
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

        val userDetail = app.user.getUserDetails()
        val categoryId = intent.getStringExtra(Constant.CATEGORY_ID)
        if (userDetail == null) {
            getProductList("", categoryId)
        } else {
            getProductList(
                app.user.getUserDetails()?.userId, categoryId,
            )
        }

        binding.llCartLayout.setOnClickListener{
            val intent = Intent(this@ProductListActivity, MyCartActivity::class.java)
            intent.putExtra("ORDER_ID", "")
            intent.putExtra(isListActivity,true)
            startActivity(intent)
        }
        showCartLayout()
    }

    private fun getProductList(
        userId: String?,
        productId: String?,
    ) {
        val json = JsonObject()
        json.addProperty(Constant.CATEGORY_ID, productId)
        json.addProperty(Constant.TEXT, "")
        json.addProperty(Constant.pageIndex, "0")

        viewModel.getProductList(userId, Constant.DEVICE_TYPE, json)
            .observe(this) {
                adapter = ProductListAdapter(
                    this@ProductListActivity,
                    this@ProductListActivity,
                    it.productList, this@ProductListActivity
                )
                binding.productList.layoutManager = LinearLayoutManager(this@ProductListActivity)
                binding.productList.isNestedScrollingEnabled = false
                binding.productList.adapter = adapter
            }

    }


    override fun onClickAddOrRemoveItem(
        action: String,
        productDetail: ProductDetail
    ) {
        when (action) {
            "add" -> {
                if (app.user.getLoginSession()) {
                    Utility().addRemoveItem(
                        app.user.getUserDetails()?.userId,
                        action,
                        productDetail.productId,
                        productDetail.variantId,
                        ""
                    )
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        Utility().updateProduct(
                            this@ProductListActivity,
                            productDetail.productId,
                            productDetail.variantId,
                            productDetail.itemQuantity
                        )
                    }
                }
            }
            "remove" -> {
                if (app.user.getLoginSession()) {
                    // HIT Minus Plus API
                    Utility().addRemoveItem(
                        app.user.getUserDetails()?.userId,
                        action,
                        productDetail.productId,
                        productDetail.variantId,
                        ""
                    )
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        Utility().updateProduct(
                            this@ProductListActivity,
                            productDetail.productId,
                            productDetail.variantId,
                            productDetail.itemQuantity
                        )
                    }
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(Runnable { showCartLayout() }, 100)

        //Utility().addRemoveItem(action, productDetail)
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
    }
}