package naya.ganj.app.data.home.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialSharedAxis
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
import naya.ganj.app.data.home.adapter.HomeAdapter
import naya.ganj.app.data.home.adapter.OfferPromoBanner
import naya.ganj.app.data.home.adapter.ProductListHomeAdapter
import naya.ganj.app.data.home.adapter.SliderAdapter
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.data.home.repositry.HomePageDataFactory
import naya.ganj.app.data.home.repositry.HomeRepositry
import naya.ganj.app.data.home.viewmodel.HomeViewModel
import naya.ganj.app.databinding.FragmentHomeBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
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
        binding.llProgressbar.visibility = View.VISIBLE

        if (Utility.isAppOnLine(requireActivity(), object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getHomeData()
                }
            }))
            getHomeData()
        if (app.user.getAppLanguage() == 1) {
            binding.searchEdittext.hint =
                requireActivity().resources.getString(R.string.search_here_h)
        }
        binding.llSearchLayout.setOnClickListener {
            startActivity(Intent(requireActivity(), ProductListActivity::class.java))
        }

        return binding.root
    }


    private fun getHomeData() {
        val userID: String
        val jsonObject = JsonObject()
        jsonObject.addProperty("index", 1);
        Log.e("TAG", "getHomeData: " + app.user.getUserDetails()?.userId)
        userID = if (app.user.getUserDetails()?.userId == null) {
            ""
        } else {
            app.user.getUserDetails()?.userId!!
        }
        homeViewModel.getHomeData(userID, jsonObject)
            .observe(requireActivity()) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        if (response.data!!.status) {
                            if (isAdded) {
                                setHomePageData(response)
                            }
                        } else {
                            try {
                                binding.llProgressbar.visibility = View.GONE
                                Utility.serverNotResponding(
                                    requireActivity(),
                                    response.data.msg
                                )
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        binding.llProgressbar.visibility = View.GONE
                        Utility.serverNotResponding(
                            requireActivity(),
                            response.message.toString()
                        )
                    }
                }
            }
    }

    private fun setHomePageData(response: NetworkResult.Success<HomePageModel>) {

        //HomePage Banner
        val bannerSlider = SliderAdapter(requireActivity(), response.data?.data!!.promoBanner)
        binding.slider.autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
        binding.slider.setSliderAdapter(bannerSlider)
        binding.slider.scrollTimeInSec = 3
        binding.slider.isAutoCycle = true
        binding.slider.startAutoCycle()

        // Set Category Data
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

        binding.rvHomeRecyclerview.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.rvHomeRecyclerview.isNestedScrollingEnabled = false
        binding.rvHomeRecyclerview.adapter =
            HomeAdapter(requireActivity(), response.data.data.category, app, cateImages)
        Utility.listAnimation(binding.rvHomeRecyclerview)

        // Set PromoBanner Slider
        val promoBannerSlider =
            OfferPromoBanner(requireActivity(), response.data.data.offerPromoBanner)
        binding.promoBannerSlider.autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
        binding.promoBannerSlider.setSliderAdapter(promoBannerSlider)
        binding.promoBannerSlider.scrollTimeInSec = 3
        binding.promoBannerSlider.isAutoCycle = true
        binding.promoBannerSlider.startAutoCycle()

        // set Sub Category List
        binding.tvSubCatTitle.text = Utility.convertLanguage(response.data.data.subCategory, app)
        binding.subCategoryList.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        binding.subCategoryList.isNestedScrollingEnabled = false
        binding.subCategoryList.adapter = ProductListHomeAdapter(requireActivity(),response.data.data.productList,app,requireActivity(),this)
        Utility.listAnimation(binding.subCategoryList)

        binding.llMainLeaniearLayout.visibility = View.VISIBLE
        binding.llProgressbar.visibility = View.GONE
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
            withContext(Dispatchers.Main){
               val mainActivity=requireActivity() as MainActivity
                mainActivity.updateCartValue(list.size.toString())
            }
        }
    }



}