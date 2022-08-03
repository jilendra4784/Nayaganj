package naya.ganj.app.data.search.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class SearchFragment : Fragment(){

    // Note : Product List Activity Used As Search Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findNavController().navigate(SearchFragmentDirections.actionNavigationSearchToNavigationHome2())
    }
}