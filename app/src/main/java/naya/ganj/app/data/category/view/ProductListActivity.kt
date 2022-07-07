package naya.ganj.app.data.category.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import naya.ganj.app.data.category.adapter.ProductListAdapter
import naya.ganj.app.data.category.viewmodel.ProductListViewModel
import naya.ganj.app.databinding.ActivityProductListBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.CATEGORY_ID
import naya.ganj.app.utility.Utility
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProductListActivity : AppCompatActivity(), OnclickAddOremoveItemListener {
    lateinit var viewModel: ProductListViewModel
    lateinit var binding: ActivityProductListBinding
    var productId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ProductListViewModel::class.java)
        if (intent.extras != null) {
            productId = intent.getStringExtra("PRODUCT_ID").toString()
        }
    }

    private fun getProductList(productId: String) {
        val json = JsonObject()
        json.addProperty(CATEGORY_ID, productId)
        json.addProperty("text", "")
        json.addProperty(Constant.pageIndex, "0")

        viewModel.getProductList("61cc52c880b1d508d650b5b4", "A", json).observe(this, Observer {
            it.let {
                val productListAdapter = ProductListAdapter(
                    this@ProductListActivity,
                    it.productList,
                    this@ProductListActivity
                )
                binding.productList.layoutManager = LinearLayoutManager(this)
                binding.productList.adapter = productListAdapter
                calculateAmount()
            }
        })
    }

    override fun onClickAddOrRemoveItem(
        action: String,
        productId: String,
        variantId: String,
        promoCode: String,
        totalAmount: Double
    ) {
        Utility().addRemoveItem(action, productId, variantId, promoCode)
        // Delete Product Detail
        if (totalAmount > 0) {
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                val isProductExist = async {
                    Utility().isProductAvailable(
                        this@ProductListActivity,
                        productId,
                        variantId
                    )
                }.await()

                if (isProductExist) {
                    Utility().updateProduct(
                        this@ProductListActivity,
                        productId,
                        variantId,
                        totalAmount
                    )
                } else {
                    Utility().insertProduct(
                        this@ProductListActivity,
                        productId,
                        variantId,
                        totalAmount
                    )
                }
            }
        } else {
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                Utility().deleteProduct(this@ProductListActivity, productId, variantId)
            }

        }

        calculateAmount()
    }

    override fun onResume() {
        super.onResume()
        getProductList(productId)

    }

    private fun calculateAmount() {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(300)
            val listofProduct = Utility().getAllProductList(applicationContext)
            runOnUiThread {
                if (listofProduct.isNotEmpty()) {
                    binding.llCartLayout.visibility = View.VISIBLE
                    binding.cartLayout.tvItems.text = listofProduct.size.toString()
                    var amount = 0.0;
                    for (item in listofProduct) {
                        amount += item.totalAmount
                    }
                    binding.cartLayout.tvTotalAmount.text =
                        Utility().formatTotalAmount(amount).toString()

                } else {
                    binding.llCartLayout.visibility = View.GONE
                }
            }
        }
    }
}