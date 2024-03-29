package naya.ganj.app.data.home.view

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.google.gson.JsonObject
import com.smarteist.autoimageslider.SliderView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.home.adapter.*
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.data.home.repositry.HomePageDataFactory
import naya.ganj.app.data.home.repositry.HomeRepositry
import naya.ganj.app.data.home.viewmodel.HomeViewModel
import naya.ganj.app.data.mycart.view.OfferBottomSheetDetail.Companion.TAG
import naya.ganj.app.databinding.FragmentHomeBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.retrofit.URLConstant
import naya.ganj.app.roomdb.entity.ApiManager
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.LocalDBManager
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.NetworkResult
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment(), OnclickAddOremoveItemListener {

    lateinit var binding: FragmentHomeBinding
    lateinit var homeViewModel: HomeViewModel
    lateinit var app: Nayaganj


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(
            requireActivity(),
            HomePageDataFactory(HomeRepositry(RetrofitClient.instance))
        )[HomeViewModel::class.java]
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        app = requireActivity().applicationContext as Nayaganj
        if (app.user.getLoginSession()) {
            URLConstant.BaseImageUrl = ""
        }
        binding.llMainLeaniearLayout.visibility = View.GONE

        setHomePageData()
        checkViewVisibility()
        binding.swipeRefreshLayout.setOnRefreshListener {
            setHomePageData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return binding.root
    }


    private fun getJsonObject(index: Int): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("index", index)
        return jsonObject
    }

    private fun setHomePageData() {
        binding.llMainLeaniearLayout.visibility = View.VISIBLE
        val jsonObject = getJsonObject(1)
        homeViewModel.getHomeData(app.user.getUserDetails()?.userId ?: "", jsonObject)
            .observe(requireActivity()) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        if (response.data!!.status) {
                            if (isAdded) {
                                requireActivity().runOnUiThread { binding.slider.requestFocus() }
                                binding.promoBannerSlider.autoCycleDirection =
                                    SliderView.LAYOUT_DIRECTION_LTR
                                binding.promoBannerSlider.setSliderAdapter(
                                    OfferPromoBanner(
                                        requireActivity(),
                                        response.data.data.offerPromoBanner
                                    )
                                )
                                binding.promoBannerSlider.scrollTimeInSec = 3
                                binding.promoBannerSlider.isAutoCycle = true
                                binding.promoBannerSlider.startAutoCycle()

                                val mainActivity = requireActivity() as MainActivity
                                mainActivity.updateCartValue(response.data.data.itemsInCart.toString())

                                setPromoBannerList(response.data.data, binding.slider)
                                setProductList(
                                    response.data.data,
                                    binding.tvProductTitle,
                                    binding.productList,
                                    0
                                )
                                setCategoryGrid3List(response.data.data, binding.rvHomeRecyclerview)

                            }
                        } else {
                            Utility.showToast(
                                requireActivity(),
                                response.message.toString()
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        Utility.serverNotResponding(
                            requireActivity(),
                            response.message.toString()
                        )
                    }
                }
            }
    }

    private fun setPromoBannerList(data: HomePageModel.Data, promoBannerSlider: SliderView) {
        promoBannerSlider.autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
        promoBannerSlider.setSliderAdapter(SliderAdapter(requireActivity(), data.promoBanner))
        promoBannerSlider.scrollTimeInSec = 3
        promoBannerSlider.isAutoCycle = true
        promoBannerSlider.startAutoCycle()
    }


    private fun setProductList(
        data: HomePageModel.Data,
        tvTitleTextView: TextView,
        recyclerView: RecyclerView,
        mApiIndex: Int
    ) {
        tvTitleTextView.text = Utility.convertLanguage(data.productName, app)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)
        if (mApiIndex == 4) {
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = ProductListHomeAdapterNew(
                requireActivity(),
                data.productList,
                app,
                requireActivity(),
                this
            )
        } else {
            recyclerView.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            recyclerView.adapter = ProductListHomeAdapter(
                requireActivity(),
                data.productList,
                app,
                requireActivity(),
                this
            )
        }


        recyclerView.scheduleLayoutAnimation()
    }

    private fun setCategoryGrid2List(
        data: HomePageModel.Data,
        tvTitleTextView: TextView,
        recyclerView: RecyclerView
    ) {
        tvTitleTextView.text = data.subCategoryName
        recyclerView.layoutManager = GridLayoutManager(requireActivity(), 2)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = HomeAdapter(requireActivity(), data.subCategoryList, app)
        recyclerView.scheduleLayoutAnimation()
    }

    private fun setCategoryGrid3List(
        data: HomePageModel.Data,
        recyclerView: RecyclerView
    ) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = HomeAdapter(requireActivity(), data.category, app)
    }

    override fun onClickAddOrRemoveItem(
        action: String,
        productDetail: ProductDetail,
        addremoveText: TextView
    ) {
        when (action) {
            Constant.INSERT -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(
                        requireContext(),
                        app,
                        action,
                        productDetail,
                        addremoveText
                    )
                } else {
                    LocalDBManager.insertItemInLocal(requireContext(), productDetail)
                }
            }
            Constant.ADD -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(
                        requireContext(),
                        app,
                        action,
                        productDetail,
                        addremoveText
                    )
                } else {
                    addremoveText.isEnabled = true
                    LocalDBManager.updateProduct(requireContext(), productDetail)
                }
            }
            Constant.REMOVE -> {
                if (app.user.getLoginSession()) {
                    ApiManager.updateProductCart(
                        requireContext(),
                        app,
                        action,
                        productDetail,
                        addremoveText
                    )
                } else {
                    addremoveText.isEnabled = true
                    if (productDetail.itemQuantity > 0) {
                        LocalDBManager.updateProduct(requireContext(), productDetail)
                    } else {
                        LocalDBManager.deleteProduct(requireContext(), productDetail)
                    }
                }
            }
        }
        // Update
        Handler(Looper.getMainLooper()).postDelayed(Runnable { setBadgeCount() }, 300)

    }

    private fun setBadgeCount() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val list: List<ProductDetail> = Utility().getAllProductList(requireActivity())
                if (list.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        val mainActivity = requireActivity() as MainActivity
                        mainActivity.updateCartValue(list.size.toString())
                    }
                }

            } catch (_: Exception) {
            }
        }
    }

    private fun checkViewVisibility() {

        var count = 0
        var cat2Count = 0
        var cate3Count = 0
        var cate4Count = 0
        val scrollBounds = Rect()
        binding.nestedscrollview.getHitRect(scrollBounds)
        binding.nestedscrollview.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            if (binding.llCate2.getLocalVisibleRect(scrollBounds)) {
                if (!binding.llCate2.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < binding.llCate2.getHeight()) {
                    if (count == 0) {
                        val jsonObject = getJsonObject(2)
                        homeViewModel.getHomeDataForIndex2(
                            app.user.getUserDetails()?.userId ?: "",
                            jsonObject
                        ).observe(requireActivity()) { response ->
                            when (response) {
                                is NetworkResult.Success -> {
                                    if (response.data!!.status) {
                                        if (isAdded) {
                                            setCategoryGrid2List(
                                                response.data.data,
                                                binding.tvcat2,
                                                binding.rvcatrecyclerview2
                                            )
                                            setProductList(
                                                response.data.data,
                                                binding.tvProductTitle2,
                                                binding.productList2, 0
                                            )
                                            setPromoBannerList(response.data.data, binding.slider2)
                                            binding.llCate2.visibility = View.VISIBLE
                                            Utility.listAnimation(binding.rvcatrecyclerview2)

                                        }
                                    } else {
                                        Utility.showToast(
                                            requireActivity(),
                                            response.message.toString()
                                        )
                                    }
                                }

                                is NetworkResult.Error -> {
                                    try {
                                        Utility.serverNotResponding(
                                            requireActivity(),
                                            response.message.toString()
                                        )
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }
                    }
                    count++
                }
            }

            if (binding.llCate3.getLocalVisibleRect(scrollBounds)) {
                if (!binding.llCate3.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < binding.productList.getHeight()) {
                    if (cat2Count == 0) {
                        val jsonObject = getJsonObject(3)
                        homeViewModel.getHomeDataForIndex3(
                            app.user.getUserDetails()?.userId ?: "",
                            jsonObject
                        ).observe(requireActivity()) { response ->
                            when (response) {
                                is NetworkResult.Success -> {
                                    if (response.data!!.status) {
                                        if (isAdded) {
                                            setCategoryGrid2List(
                                                response.data.data,
                                                binding.tvcat3,
                                                binding.rvcatrecyclerview3
                                            )
                                            Utility.listAnimation(binding.rvcatrecyclerview3)
                                            setProductList(
                                                response.data.data,
                                                binding.tvProductTitle3,
                                                binding.productList3, 0
                                            )
                                            setPromoBannerList(response.data.data, binding.slider3)
                                            binding.llCate3.visibility = View.VISIBLE

                                        }
                                    } else {
                                        Utility.showToast(
                                            requireActivity(),
                                            response.message.toString()
                                        )
                                    }
                                }

                                is NetworkResult.Error -> {
                                    Utility.serverNotResponding(
                                        requireActivity(),
                                        response.message.toString()
                                    )
                                }
                            }
                        }
                    }
                    cat2Count++
                }
            }

            if (binding.llCate4.getLocalVisibleRect(scrollBounds)) {
                if (!binding.llCate4.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < binding.llCate4.getHeight()) {
                    if (cate3Count == 0) {
                        val jsonObject = getJsonObject(4)
                        homeViewModel.getHomeDataForIndex4(
                            app.user.getUserDetails()?.userId ?: "",
                            jsonObject
                        ).observe(requireActivity()) { response ->
                            when (response) {
                                is NetworkResult.Success -> {
                                    if (response.data!!.status) {
                                        if (isAdded) {
                                            setCategoryGrid2List(
                                                response.data.data,
                                                binding.tvcat4,
                                                binding.rvcatrecyclerview4
                                            )
                                            setProductList(
                                                response.data.data,
                                                binding.tvProductTitle4,
                                                binding.productList4, 4
                                            )
                                            Utility.listAnimation(binding.rvcatrecyclerview4)
                                            setPromoBannerList(response.data.data, binding.slider4)
                                            binding.llCate4.visibility = View.VISIBLE
                                        }
                                    } else {
                                        Utility.showToast(
                                            requireActivity(),
                                            response.message.toString()
                                        )
                                    }
                                }

                                is NetworkResult.Error -> {
                                    Utility.serverNotResponding(
                                        requireActivity(),
                                        response.message.toString()
                                    )
                                }
                            }
                        }
                    }
                    cate3Count++
                }
            }

            if (binding.llCate5.getLocalVisibleRect(scrollBounds)) {
                if (!binding.llCate5.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < binding.llCate5.getHeight()) {
                    if (cate4Count == 0) {
                        val jsonObject = getJsonObject(5)
                        homeViewModel.getHomeDataForIndex5(
                            app.user.getUserDetails()?.userId ?: "",
                            jsonObject
                        ).observe(requireActivity()) { response ->
                            when (response) {
                                is NetworkResult.Success -> {
                                    if (response.data!!.status) {
                                        if (isAdded) {
                                            binding.llCate5.visibility = View.VISIBLE
                                            binding.rvbrandrecyclerview.layoutManager =
                                                GridLayoutManager(requireActivity(), 3)
                                            binding.rvbrandrecyclerview.isNestedScrollingEnabled =
                                                false
                                            binding.rvbrandrecyclerview.adapter = BrandAdapter(
                                                requireActivity(),
                                                response.data.data.brandList,
                                                app
                                            )
                                            Utility.listAnimation(binding.rvbrandrecyclerview)

                                        }
                                    } else {
                                        Utility.showToast(
                                            requireActivity(),
                                            response.message.toString()
                                        )
                                    }
                                }

                                is NetworkResult.Error -> {
                                    Utility.serverNotResponding(
                                        requireActivity(),
                                        response.message.toString()
                                    )
                                }
                            }
                        }
                    }
                    cate4Count++
                }
            }


        })
    }

    override fun onResume() {
        super.onResume()
        binding.linearLayout.postDelayed({ binding.slider.requestFocus() }, 100)
    }
}