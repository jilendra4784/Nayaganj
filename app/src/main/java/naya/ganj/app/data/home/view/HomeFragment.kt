package naya.ganj.app.data.home.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.smarteist.autoimageslider.SliderView
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.category.view.ProductListActivity
import naya.ganj.app.data.home.adapter.HomeAdapter
import naya.ganj.app.data.home.adapter.SliderAdapter
import naya.ganj.app.data.home.repositry.HomePageDataFactory
import naya.ganj.app.data.home.repositry.HomeRepositry
import naya.ganj.app.data.home.viewmodel.HomeViewModel
import naya.ganj.app.databinding.FragmentHomeBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.NetworkResult
import naya.ganj.app.utility.Utility


class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var homeViewModel: HomeViewModel
    lateinit var app: Nayaganj


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        homeViewModel = ViewModelProvider(requireActivity(),HomePageDataFactory(HomeRepositry(RetrofitClient.instance)))[HomeViewModel::class.java]
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        app = requireActivity().applicationContext as Nayaganj

        if(Utility.isAppOnLine(requireActivity(),object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getBannerList()
                }
            }))
        getBannerList()
        setStaticData()

        if(app.user.getAppLanguage()==1){
            binding.searchEdittext.hint=requireActivity().resources.getString(R.string.search_here_h)
        }

        return binding.root
    }

    private fun getBannerList() {
            homeViewModel.getBannerList(app.user.getUserDetails()?.userId)
                .observe(requireActivity()) { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            if (isAdded) {
                                val adapter =
                                    SliderAdapter(
                                        requireActivity(),
                                        response.data!!.promoBannerList
                                    )
                                binding.slider.autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
                                binding.slider.setSliderAdapter(adapter)
                                binding.slider.scrollTimeInSec = 3
                                binding.slider.isAutoCycle = true
                                binding.slider.startAutoCycle()

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

    private fun setStaticData() {

        binding.rvHomeRecyclerview.layoutManager = GridLayoutManager(requireActivity(), 3)
        // binding.rvHomeRecyclerview.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvHomeRecyclerview.isNestedScrollingEnabled = false
        binding.rvHomeRecyclerview.adapter = HomeAdapter()

        binding.llSearchLayout.setOnClickListener {
            startActivity(
                Intent(
                    requireActivity(),
                    ProductListActivity::class.java
                )
            )
        }
    }
}