package naya.ganj.app.data.sidemenu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry

class SideMenuViewModelFactory constructor(val repo: SideMenuDataRepositry) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return if (modelClass.isAssignableFrom(MyOrderViewModel::class.java)) {
            MyOrderViewModel(this.repo) as T
        } else if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
            OrderDetailViewModel(this.repo) as T
        }else if (modelClass.isAssignableFrom(UpdateProfileActivityViewModel::class.java)) {
            UpdateProfileActivityViewModel(this.repo) as T
        } else if (modelClass.isAssignableFrom(VirtualOrderViewModel::class.java)) {
            VirtualOrderViewModel(this.repo) as T
        }else if (modelClass.isAssignableFrom(RetailerViewModel::class.java)) {
            RetailerViewModel(this.repo) as T
        }else {
            throw IllegalArgumentException("ViewModel Not Found")
        }



    }
}