package naya.ganj.app.data.category.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.adapter.ProductListAdapter
import naya.ganj.app.data.category.viewmodel.ProductListViewModel
import naya.ganj.app.databinding.ProductListFragmentBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility

class ProductListFragment : Fragment(), OnclickAddOremoveItemListener {

    lateinit var viewModel: ProductListViewModel
    lateinit var binding: ProductListFragmentBinding
    lateinit var app: Nayaganj

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ProductListViewModel::class.java)
        binding = ProductListFragmentBinding.inflate(inflater, container, false)
        app = requireActivity().applicationContext as Nayaganj

        val userDetail = app.user.getUserDetails()
        if (userDetail == null) {
            getProductList("", arguments?.getString(Constant.CATEGORY_ID), this)
        } else {
            getProductList(
                app.user.getUserDetails()?.userId,
                arguments?.getString(Constant.CATEGORY_ID),
                this
            )
        }

        binding.include5.ivBackArrow.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        binding.include5.toolbarTitle.setText("Product List")
        return binding.root
    }

    private fun getProductList(
        userId: String?,
        productId: String?,
        productListFragment: ProductListFragment
    ) {
        val json = JsonObject()
        json.addProperty(Constant.CATEGORY_ID, productId)
        json.addProperty(Constant.TEXT, "")
        json.addProperty(Constant.pageIndex, "0")

        viewModel.getProductList(userId, Constant.DEVICE_TYPE, json)
            .observe(requireActivity()) {
                if (isAdded) {
                    binding.productList.layoutManager = LinearLayoutManager(requireActivity())
                    binding.productList.isNestedScrollingEnabled = false
                    binding.productList.adapter = ProductListAdapter(
                        requireActivity(),
                        requireActivity(),
                        it.productList, productListFragment, app
                    )
                }
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
                            requireActivity(),
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
                            requireActivity(),
                            productDetail.productId,
                            productDetail.variantId,
                            productDetail.itemQuantity
                        )
                    }
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            lifecycleScope.launch(Dispatchers.IO) {
                val list: List<ProductDetail> =
                    AppDataBase.getInstance(requireActivity()).productDao().getProductList()

                Log.e("TAG", "onClickAddOrRemoveItem: " + list)
            }

        }, 1000)

        //Utility().addRemoveItem(action, productDetail)

    }
}