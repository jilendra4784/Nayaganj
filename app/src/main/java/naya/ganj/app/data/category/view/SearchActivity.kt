package naya.ganj.app.data.category.view

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.adapter.ProductListAdapter
import naya.ganj.app.data.category.adapter.RecentListAdapter
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.category.viewmodel.ProductListViewModel
import naya.ganj.app.data.mycart.view.MyCartActivity
import naya.ganj.app.databinding.SearchActivityLayoutBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.*
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Constant.isListActivity
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchActivity : AppCompatActivity(), OnclickAddOremoveItemListener, OnitemClickListener {

    lateinit var viewModel: ProductListViewModel
    lateinit var binding: SearchActivityLayoutBinding
    lateinit var app: Nayaganj
    var adapter: ProductListAdapter? = null
    private val SPEECH_REQUEST_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SearchActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProductListViewModel::class.java]
        app = applicationContext as Nayaganj

        loadRecentSuggestion()

        binding.frameLayout.visibility = View.VISIBLE
        binding.editText.requestFocus()
        binding.editText.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val isQueryExist = AppDataBase.getInstance(this@SearchActivity).productDao()
                            .isSuggestionExist(binding.editText.text.toString().trim())
                        if (!isQueryExist) {
                            AppDataBase.getInstance(this@SearchActivity).productDao()
                                .insertRecentQuery(
                                    RecentSuggestion(
                                        binding.editText.text.toString().trim()
                                    )
                                )
                        }
                    }
                    binding.productList.visibility = View.GONE
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
                        binding.llCartLayout.visibility = View.GONE
                    }
                    true
                }
                else -> false
            }
        }



        binding.editText.setOnClickListener {
            if (binding.tvNoProduct.visibility == View.VISIBLE) {
                loadRecentSuggestion()
            }

        }

        if (app.user.getAppLanguage() == 1) {
            binding.editText.hint = resources.getString(R.string.search_here_h)
        } else {
            binding.editText.hint = "Search 100+ product"
        }

        binding.llCartLayout.setOnClickListener {
            val intent = Intent(this@SearchActivity, MyCartActivity::class.java)
            intent.putExtra("ORDER_ID", "")
            intent.putExtra(isListActivity, true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        binding.voiceSearch.setOnClickListener{
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            }
            // This starts the activity and populates the intent with the speech text.
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        }

        binding.arrowIcon.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results?.get(0) ?: ""
                }

            binding.editText.setText(spokenText)
            // Do something with spokenText.
            Log.e("TAG", "onActivityResult: " + spokenText)

            lifecycleScope.launch(Dispatchers.IO) {
                val isQueryExist = AppDataBase.getInstance(this@SearchActivity).productDao()
                    .isSuggestionExist(spokenText)
                if (!isQueryExist) {
                    AppDataBase.getInstance(this@SearchActivity).productDao()
                        .insertRecentQuery(
                            RecentSuggestion(
                                spokenText
                            )
                        )
                }
            }
            binding.productList.visibility = View.GONE
            if (spokenText.length >= 3) {
                if (app.user.getLoginSession()) {
                    getProductList(
                        spokenText,
                        app.user.getUserDetails()?.userId,
                        ""
                    )
                } else {
                    getProductList(spokenText, "", "")
                }
            } else {
                binding.tvNoProduct.visibility = View.VISIBLE
                binding.llCartLayout.visibility = View.GONE
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadRecentSuggestion() {
        lifecycleScope.launch(Dispatchers.IO)
        {
            val list: MutableList<RecentSuggestion> =
                AppDataBase.getInstance(this@SearchActivity).productDao().getSuggestionList()
            if (list.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    binding.recentList.layoutManager = LinearLayoutManager(this@SearchActivity)
                    binding.recentList.adapter = RecentListAdapter(
                        this@SearchActivity,
                        this@SearchActivity,
                        list,
                        this@SearchActivity
                    )
                    binding.tvRecent.visibility = View.VISIBLE
                    binding.recentList.visibility = View.VISIBLE
                    binding.tvNoProduct.visibility = View.GONE
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.tvNoProduct.visibility = View.VISIBLE
                    binding.tvRecent.visibility = View.GONE
                    binding.recentList.visibility = View.GONE
                }

            }
        }
    }

    private fun getProductList(
        text: String,
        userId: String?,
        cateId: String?,
    ) {
        binding.tvRecent.visibility = View.GONE
        binding.recentList.visibility = View.GONE
        binding.productShimmer.showShimmer(true);
        binding.productShimmer.visibility = View.VISIBLE
        binding.tvNoProduct.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if (Utility.isAppOnLine(this@SearchActivity, object : OnInternetCheckListener {
                    override fun onInternetAvailable() {
                        getProductListRequestData(text, userId, cateId)
                    }
                })) {
                getProductListRequestData(text, userId, cateId)
            }
        }, 500)
    }

    override fun onClickAddOrRemoveItem(
        action: String,
        productDetail: ProductDetail,
        addremoveText: TextView
    ) {
        when (action) {
            Constant.INSERT -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(this@SearchActivity,app,action,productDetail,addremoveText)
                } else {
                    LocalDBManager.insertItemInLocal(this@SearchActivity,productDetail)
                }
            }
            Constant.ADD -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(this@SearchActivity,app,action,productDetail,addremoveText)
                } else {
                    addremoveText.isEnabled = true
                    LocalDBManager.updateProduct(this@SearchActivity,productDetail)
                }
            }
            Constant.REMOVE -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(this@SearchActivity,app,action,productDetail,addremoveText)
                } else {
                    addremoveText.isEnabled = true
                    if (productDetail.itemQuantity > 0) {
                        LocalDBManager.updateProduct(this@SearchActivity,productDetail)
                    } else {
                        LocalDBManager.deleteProduct(this@SearchActivity,productDetail)
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
                    binding.cartLayout.tvTotalAmount.text =
                        Utility().formatTotalAmount(amount).toString()
                } else {
                    binding.llCartLayout.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        adapter?.notifyDataSetChanged()
        Handler(Looper.getMainLooper()).postDelayed(Runnable { showCartLayout() }, 300)
    }

    fun getProductListRequestData(text: String, userId: String?, cateId: String?) {
        val json = JsonObject()
        json.addProperty(Constant.CATEGORY_ID, cateId)
        json.addProperty(Constant.TEXT, text)
        json.addProperty(Constant.pageIndex, "0")

        viewModel.getProductList(this@SearchActivity, userId, Constant.DEVICE_TYPE, json)
            .observe(this) {
                if (it != null && it.productList.isNotEmpty()) {
                    for (product in it.productList) {
                        for ((i, variant) in product.variant.withIndex()) {
                            if (variant.vQuantityInCart > 0) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val isProductExist = async {
                                        AppDataBase.getInstance(this@SearchActivity)
                                            .productDao()
                                            .isProductExist(product.id, variant.vId)
                                    }.await()
                                    if (isProductExist) {
                                        val singleProduct = async {
                                            AppDataBase.getInstance(this@SearchActivity)
                                                .productDao()
                                                .getSingleProduct(product.id, variant.vId)
                                        }.await()
                                        if (variant.vQuantityInCart > singleProduct.itemQuantity) {
                                            AppDataBase.getInstance(this@SearchActivity)
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

                    binding.recentList.visibility = View.GONE
                    binding.tvRecent.visibility = View.GONE

                    binding.productShimmer.stopShimmer()
                    binding.productShimmer.visibility = View.GONE
                    binding.productShimmer.hideShimmer()
                    adapter = ProductListAdapter(
                        this@SearchActivity,
                        this@SearchActivity,
                        it.productList, this@SearchActivity, app
                    )
                    binding.productList.layoutManager =
                        LinearLayoutManager(this@SearchActivity)
                    binding.productList.isNestedScrollingEnabled = false
                    binding.productList.adapter = adapter

                    binding.productList.setHasFixedSize(true)
                    binding.tvNoProduct.visibility = View.GONE
                    binding.productList.visibility = View.VISIBLE
                    hideKeyBoard()
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

    override fun onclick(position: Int, data: String) {
        binding.editText.setText(data)
        binding.recentList.visibility = View.GONE
        binding.tvRecent.visibility = View.GONE
        if (app.user.getLoginSession()) {
            getProductList(
                data,
                app.user.getUserDetails()?.userId,
                ""
            )
        } else {
            getProductList(binding.editText.text.toString(), "", "")
        }
    }

    private fun hideKeyBoard() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}