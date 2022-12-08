package naya.ganj.app.data.home.view

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.google.gson.JsonObject
import com.smarteist.autoimageslider.SliderView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.MainActivity
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.model.AddRemoveModel
import naya.ganj.app.data.category.view.ProductListActivity
import naya.ganj.app.data.home.adapter.*
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.data.home.repositry.HomePageDataFactory
import naya.ganj.app.data.home.repositry.HomeRepositry
import naya.ganj.app.data.home.viewmodel.HomeViewModel
import naya.ganj.app.databinding.FragmentHomeBinding
import naya.ganj.app.interfaces.OnclickAddOremoveItemListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ProductDetail
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.NetworkResult
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() , OnclickAddOremoveItemListener {

    lateinit var binding: FragmentHomeBinding
    lateinit var homeViewModel: HomeViewModel
    lateinit var app: Nayaganj
    var cateImages = arrayOf(
        R.drawable.foodgrains_oil_masala,
        R.drawable.bakery_cakes_dairy,
        R.drawable.bevrages,
        R.drawable.snacks_branded_foods,
        R.drawable.beauty_hygiene,
        R.drawable.all_cleaning_household,
        R.drawable.gourmet_world_food,
        R.drawable.all_caby_care
    )


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

        binding.llMainLeaniearLayout.visibility = View.GONE

        if (app.user.getAppLanguage() == 1) {
            binding.searchEdittext.hint =
                requireActivity().resources.getString(R.string.search_here_h)
        }
        binding.llSearchLayout.setOnClickListener {
            startActivity(Intent(requireActivity(), ProductListActivity::class.java))
            //startActivity(Intent(requireActivity(), GlobalSearchActivity::class.java))
        }

        binding.slider.requestFocus()
        setHomePageData(1)

        checkViewVisibility()

        return binding.root
    }


    private fun getJsonObject(index: Int): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("index", index)
        return jsonObject
    }


    private fun setHomePageData(mIndex: Int) {
        binding.llMainLeaniearLayout.visibility = View.VISIBLE
        when (mIndex) {
            1 -> {
                val jsonObject = getJsonObject(mIndex)
                homeViewModel.getHomeData(app.user.getUserDetails()?.userId ?: "", jsonObject)
                    .observe(requireActivity()) { response ->
                        when (response) {
                            is NetworkResult.Success -> {
                                if (response.data!!.status) {
                                    if (isAdded) {
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

                                        setPromoBannerList(response.data.data, binding.slider)
                                        setProductList(response.data.data, binding.tvProductTitle, binding.productList)
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
            2 -> {
                val jsonObject = getJsonObject(mIndex)
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
                                        binding.productList2
                                    )
                                    setPromoBannerList(response.data.data, binding.slider2)
                                    binding.llCate2.visibility = View.VISIBLE
                                    Utility.listAnimation(binding.rvcatrecyclerview2)

                                }
                            } else {
                                Utility.showToast(requireActivity(), response.message.toString())
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
            3 -> {
                val jsonObject = getJsonObject(mIndex)
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
                                        binding.productList3
                                    )
                                    setPromoBannerList(response.data.data, binding.slider3)
                                    binding.llCate3.visibility = View.VISIBLE

                                }
                            } else {
                                Utility.showToast(requireActivity(), response.message.toString())
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
            4 -> {
                val jsonObject = getJsonObject(mIndex)
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
                                        binding.productList4
                                    )
                                    Utility.listAnimation(binding.rvcatrecyclerview4)
                                    setPromoBannerList(response.data.data, binding.slider4)
                                    binding.llCate4.visibility = View.VISIBLE
                                }
                            } else {
                                Utility.showToast(requireActivity(), response.message.toString())
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
            5 -> {
                val jsonObject = getJsonObject(mIndex)
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
                                    binding.rvbrandrecyclerview.isNestedScrollingEnabled = false
                                    binding.rvbrandrecyclerview.adapter = BrandAdapter(
                                        requireActivity(),
                                        response.data.data.brandList,
                                        app
                                    )
                                    Utility.listAnimation(binding.rvbrandrecyclerview)

                                }
                            } else {
                                Utility.showToast(requireActivity(), response.message.toString())
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
        recyclerView: RecyclerView
    ) {
        tvTitleTextView.text = Utility.convertLanguage(data.productName, app)
        recyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ProductListHomeAdapter(
            requireActivity(),
            data.productList,
            app,
            requireActivity(),
            this
        )
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
        recyclerView.adapter = HomeAdapter(requireActivity(), data.subCategoryList, app, cateImages)
        //Utility.listAnimation(recyclerView)
    }

    private fun setCategoryGrid3List(
        data: HomePageModel.Data,
        recyclerView: RecyclerView
    ) {
        //tvTitleTextView.text = data.subCategoryName
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = HomeAdapter(requireActivity(), data.category, app, cateImages)
        //recyclerView.layoutAnimation=AnimationUtils.loadLayoutAnimation(requireActivity(),R.anim.item_animation_from_bottom)

    }

    override fun onClickAddOrRemoveItem(
        action: String,
        productDetail: ProductDetail,
        addremoveText: TextView
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
                                        Utility().insertProduct(requireActivity(), productDetail)
                                    }
                                }else{
                                    Utility.serverNotResponding(requireActivity(),response.message())
                                }
                            }

                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(requireActivity(),t.message.toString())
                            }
                        })


                }else{
                    lifecycleScope.launch(Dispatchers.IO) {
                        Utility().insertProduct(requireActivity(), productDetail)
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
                                        AppDataBase.getInstance(requireActivity()).productDao().updateProduct(productDetail.itemQuantity, productDetail.productId, productDetail.variantId)
                                    }
                                }else{
                                    Utility.serverNotResponding(requireActivity(),response.message())
                                }
                            }

                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(requireActivity(),t.message.toString())
                            }
                        })
                }else{
                    addremoveText.isEnabled=true
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDataBase.getInstance(requireActivity()).productDao().updateProduct(productDetail.itemQuantity, productDetail.productId, productDetail.variantId)
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
                                            AppDataBase.getInstance(requireActivity()).productDao().updateProduct(productDetail.itemQuantity, productDetail.productId, productDetail.variantId)
                                        }
                                    }else{
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            AppDataBase.getInstance(requireActivity()).productDao().deleteProduct(productDetail.productId, productDetail.variantId)
                                        }
                                    }

                                }else{
                                    Utility.serverNotResponding(requireActivity(),response.message())
                                }
                            }

                            override fun onFailure(call: Call<AddRemoveModel>, t: Throwable) {
                                Utility.serverNotResponding(requireActivity(),t.message.toString())
                            }
                        })
                }else{
                    addremoveText.isEnabled=true
                    if (productDetail.itemQuantity > 0) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            Utility().updateProduct(
                                requireActivity(),
                                productDetail.productId,
                                productDetail.variantId,
                                productDetail.itemQuantity
                            )
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            Utility().deleteProduct(
                                requireActivity(),
                                productDetail.productId,
                                productDetail.variantId
                            )
                        }
                    }
                }
            }
        }
        // Update
        Handler(Looper.getMainLooper()).postDelayed(Runnable{setBadgeCount()},300)

    }

    private fun setBadgeCount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<ProductDetail> = Utility().getAllProductList(requireActivity())
            withContext(Dispatchers.Main) {
                val mainActivity = requireActivity() as MainActivity
                mainActivity.updateCartValue(list.size.toString())
            }
        }
    }

    private fun checkViewVisibility() {
        binding.nestedscrollview.isNestedScrollingEnabled=false
        var count = 0
        var cat2Count = 0
        var cate3Count = 0
        var cate4Count = 0

        val scrollBounds = Rect()
        binding.nestedscrollview.getHitRect(scrollBounds)
        binding.nestedscrollview.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (binding.llCate2.getLocalVisibleRect(scrollBounds)) {
                if (!binding.llCate2.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < binding.productList.getHeight()) {
                    if (count == 0)
                        setHomePageData(2)
                    count++
                }
            }

            if (binding.llCate3.getLocalVisibleRect(scrollBounds)) {
                if (!binding.llCate3.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < binding.productList.getHeight()) {
                    if (cat2Count == 0)
                        setHomePageData(3)
                    cat2Count++
                }
            }

            if (binding.llCate4.getLocalVisibleRect(scrollBounds)) {
                if (!binding.llCate4.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < binding.productList.getHeight()) {
                    if (cate3Count == 0)
                        setHomePageData(4)
                    cate3Count++
                }
            }

            if (binding.llCate5.getLocalVisibleRect(scrollBounds)) {
                if (!binding.llCate5.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < binding.productList.getHeight()) {
                    if (cate4Count == 0)
                        setHomePageData(5)
                    cate4Count++
                }
            }
        })

    }


}