package naya.ganj.app.data.category.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.data.category.adapter.ExpandableListAdapter
import naya.ganj.app.data.category.model.CategoryDataModel
import naya.ganj.app.data.category.viewmodel.CategoryViewModel
import naya.ganj.app.databinding.FragmentDashboardBinding
import naya.ganj.app.utility.Constant

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
            /*val bundle = Bundle()
            bundle.putString(
                Constant.CATEGORY_ID,
                cateModel.categoryList.get(groupPosition).subCategoryList.get(childPosition).id
            )
            findNavController().navigate(R.id.productListFragment, bundle)
*/
            val intent = Intent(requireActivity(), ProductListActivity::class.java)
            intent.putExtra(
                Constant.CATEGORY_ID,
                cateModel.categoryList.get(groupPosition).subCategoryList.get(childPosition).id
            )
            startActivity(intent)
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

}