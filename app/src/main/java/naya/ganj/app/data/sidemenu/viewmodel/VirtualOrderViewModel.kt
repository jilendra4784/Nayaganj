package naya.ganj.app.data.sidemenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import naya.ganj.app.data.sidemenu.model.MyOrderListModel
import naya.ganj.app.data.sidemenu.model.VirtualOrderModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry

class VirtualOrderViewModel(val repo : SideMenuDataRepositry): ViewModel() {
    private val mutableLiveData = MutableLiveData<VirtualOrderModel>()

    fun getMyVirtualOrderList(userid: String?): LiveData<VirtualOrderModel> {
        viewModelScope.launch {
            val result = repo.getMyVirtualOrderList(userid)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }


}