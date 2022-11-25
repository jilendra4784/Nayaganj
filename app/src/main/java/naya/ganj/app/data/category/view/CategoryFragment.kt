package naya.ganj.app.data.category.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.category.adapter.NewExpandableListAdapter
import naya.ganj.app.data.category.model.CategoryDataModel
import naya.ganj.app.data.category.viewmodel.CategoryViewModel
import naya.ganj.app.databinding.FragmentDashboardBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility

class CategoryFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    var categoryViewModel: CategoryViewModel? = null
    lateinit var app :Nayaganj

    lateinit var adapter: NewExpandableListAdapter
    lateinit var cateModel: CategoryDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        app=requireActivity().applicationContext as Nayaganj
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(Utility.isAppOnLine(requireActivity(),object : OnInternetCheckListener{
                override fun onInternetAvailable() {
                    if (isAdded)
                        getCategoryData()
                }
            })){
            if (isAdded)
                getCategoryData()
        }

       /* var previousOpenGroup = -1
        binding.expandablelist.setOnGroupExpandListener {
            if (it != previousOpenGroup) {
                binding.expandablelist.collapseGroup(previousOpenGroup)
            }
            previousOpenGroup = it
        }*/

        binding.expandablelist.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val intent = Intent(requireActivity(), ProductListActivity::class.java)
            intent.putExtra(
                Constant.CATEGORY_ID,
                cateModel.categoryList[groupPosition].subCategoryList[childPosition].id
            )
            startActivity(intent)
            true
        }
    }

    private fun getCategoryData() {
        binding.progressBar.visibility=View.VISIBLE
        binding.expandablelist.visibility = View.GONE
        categoryViewModel?.getCategoryData(requireActivity())?.observe(requireActivity()) {
            if (it != null) {
                cateModel = it
                val listOfTitle=ArrayList<String>()
                val listOfDataItems=HashMap<String,List<String>>()

                for((i,item) in it.categoryList.withIndex()){
                    listOfTitle.add(item.category)

                    val listOfChildItem=ArrayList<String>()
                    for(childItem in item.subCategoryList)
                    {
                        listOfChildItem.add(childItem.category)
                    }
                    listOfDataItems.put(item.category,listOfChildItem)
                }

                if(isAdded){
                    adapter=NewExpandableListAdapter(requireActivity(),listOfTitle,listOfDataItems,app)
                    binding.expandablelist.setAdapter(adapter)
                }

                binding.progressBar.visibility = View.GONE
                binding.expandablelist.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }
}