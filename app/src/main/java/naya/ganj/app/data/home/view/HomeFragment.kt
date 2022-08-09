package naya.ganj.app.data.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import naya.ganj.app.data.home.adapter.HomeAdapter
import naya.ganj.app.data.home.viewmodel.HomeViewModel
import naya.ganj.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding

    // This property is only valid between onCreateView and
    // onDestroyView.


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setStaticData()
        return binding.root
    }

    private fun setStaticData() {

        binding.rvHomeRecyclerview.layoutManager = GridLayoutManager(requireActivity(), 3)
       // binding.rvHomeRecyclerview.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvHomeRecyclerview.isNestedScrollingEnabled = false
        binding.rvHomeRecyclerview.adapter = HomeAdapter()
    }
}