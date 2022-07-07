package naya.ganj.app.data.category.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import naya.ganj.app.R
import naya.ganj.app.data.category.adapter.ProductListAdapter
import naya.ganj.app.data.category.viewmodel.ProductListViewModel
import naya.ganj.app.databinding.ProductListFragmentBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ProductListFragment : Fragment(), OnclickAddOremoveItemListener {

    lateinit var viewModel: ProductListViewModel
    lateinit var binding: ProductListFragmentBinding
    var isProductDetailClicked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ProductListViewModel::class.java)
        binding = ProductListFragmentBinding.inflate(inflater, container, false)
        getProductList(arguments?.getString("PRODUCT_ID"), this)

        val appbar = requireActivity().findViewById<AppBarLayout>(R.id.appBarLayout)
        appbar.visibility = View.GONE

        binding.include5.ivBackArrow.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        binding.include5.toolbarTitle.setText("Product List")

        return binding.root
    }

    private fun getProductList(productId: String?, productListFragment: ProductListFragment) {
        val json = JsonObject()
        json.addProperty("categoryId", productId)
        json.addProperty("text", "")
        json.addProperty(Constant.pageIndex, "0")


        viewModel.getProductList("61cc52c880b1d508d650b5b4", "A", json)
            .observe(requireActivity(), Observer {
                val productListAdapter = ProductListAdapter(
                    requireActivity(),
                    it.productList, productListFragment
                )
                binding.productList.layoutManager = LinearLayoutManager(requireActivity())
                binding.productList.adapter = productListAdapter
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
                        requireActivity(),
                        productId,
                        variantId
                    )
                }.await()

                if (isProductExist) {
                    Utility().updateProduct(
                        requireActivity(),
                        productId,
                        variantId,
                        totalAmount
                    )
                } else {
                    Utility().insertProduct(
                        requireActivity(),
                        productId,
                        variantId,
                        totalAmount
                    )
                }
            }
        } else {
            lifecycle.coroutineScope.launch(Dispatchers.IO) {
                Utility().deleteProduct(requireActivity(), productId, variantId)
            }
        }
    }
}