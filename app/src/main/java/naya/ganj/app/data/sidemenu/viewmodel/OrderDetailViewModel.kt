package naya.ganj.app.data.sidemenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class OrderDetailViewModel(val repositry: SideMenuDataRepositry) : ViewModel() {
    private val mutableLiveData = MutableLiveData<OrderDetailModel>()

    fun getOrderDetailRequest(userId:String,jsonObject: JsonObject): LiveData<OrderDetailModel> {
        viewModelScope.launch {
            val result = repositry.getOrderDetailRequest(userId,jsonObject)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }
}