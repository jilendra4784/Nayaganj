package naya.ganj.app.data.home.repositry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.data.home.viewmodel.HomeViewModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.data.mycart.viewmodel.AddressListViewModel

class HomePageDataFactory constructor(val repo: HomeRepositry) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(this.repo) as T
        }else{
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}