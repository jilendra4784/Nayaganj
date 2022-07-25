package naya.ganj.app.data.sidemenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import naya.ganj.app.data.sidemenu.model.MyOrderListModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import kotlinx.coroutines.launch

class MyOrderViewModel(val repositry: SideMenuDataRepositry) : ViewModel() {
    private val mutableLiveData = MutableLiveData<MyOrderListModel>()

    fun getMyOrderList(userid: String): LiveData<MyOrderListModel> {
        viewModelScope.launch {
            val result = repositry.getMyOrderList(userid)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }
}