package naya.ganj.app.data.category.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import naya.ganj.app.R
import naya.ganj.app.data.category.adapter.ExpandableListAdapter
import naya.ganj.app.data.category.model.CategoryDataModel
import naya.ganj.app.data.category.viewmodel.CategoryViewModel
import naya.ganj.app.databinding.FragmentDashboardBinding

class CategoryFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    var categoryViewModel: CategoryViewModel? = null
    lateinit var adapter: ExpandableListAdapter
    lateinit var cateModel: CategoryDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCategoryData()

        var previousOpenGroup = -1
        binding.expandablelist.setOnGroupExpandListener(ExpandableListView.OnGroupExpandListener {
            if (it != previousOpenGroup) {
                binding.expandablelist.collapseGroup(previousOpenGroup)
            }
            previousOpenGroup = it
        })

        binding.expandablelist.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->

            /* val intent = Intent(requireActivity(), ProductListActivity::class.java)
             intent.putExtra(
                 "PRODUCT_ID",
                 cateModel.categoryList.get(groupPosition).subCategoryList.get(childPosition).id
             )
             startActivity(intent)*/
            val bundle = Bundle()
            bundle.putString(
                "PRODUCT_ID",
                cateModel.categoryList.get(groupPosition).subCategoryList.get(childPosition).id
            )
            /*Navigation.findNavController(binding.root)
                .navigate(R.id.action_navigation_dashboard_to_productListFragment, bundle)
            */
            findNavController().navigate(
                R.id.action_navigation_dashboard_to_productListFragment,
                bundle
            )
            true
        }
    }

    private fun getCategoryData() {
        categoryViewModel?.getCategoryData()?.observe(requireActivity(), Observer {

            it.let {
                cateModel = it
                adapter = ExpandableListAdapter(it)
                binding.expandablelist.setAdapter(adapter)

            }
        })
    }

    override fun onResume() {
        super.onResume()
        val appbar = requireActivity().findViewById<AppBarLayout>(R.id.appBarLayout)
        appbar.visibility = View.VISIBLE
    }
}