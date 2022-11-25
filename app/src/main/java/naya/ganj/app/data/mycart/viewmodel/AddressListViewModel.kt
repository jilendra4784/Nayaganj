package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import naya.ganj.app.data.mycart.model.AddressListModel
import naya.ganj.app.data.mycart.model.AddressResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class AddressListViewModel(val repo: AddressListRespositry) : ViewModel() {

    var mutableLiveData = MutableLiveData<AddressListModel>()
    var mutableDeleteData = MutableLiveData<AddressResponseModel>()

    fun getAddressList(userid:String?): LiveData<AddressListModel> {
        viewModelScope.launch {
            val result = repo.getAddressList(userid)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }

    fun deleteAddressRequest(userId: String?,jsonObject: JsonObject): LiveData<AddressResponseModel> {
        viewModelScope.launch {
            val result = repo.deleteAddressRequest(userId,jsonObject)
            mutableDeleteData.value = result.body()
        }
        return mutableDeleteData
    }

    fun setAddress(userId: String?, jsonObject: JsonObject): LiveData<AddressResponseModel> {
        viewModelScope.launch {
            val result = repo.setAddress(userId,jsonObject)
            mutableDeleteData.value = result.body()
        }
        return mutableDeleteData
    }




}