package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.AddressResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry

class AddAddressViewModel(var repositry: AddressListRespositry) : ViewModel() {

    val mutableLiveData = MutableLiveData<AddressResponseModel>()

    fun addAddressRequest(userID: String?, jsonObject: JsonObject): LiveData<AddressResponseModel> {
        viewModelScope.launch {
            val result = repositry.addAddressRequest(userID,jsonObject)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }

    fun updateAddressRequest(userID: String?, jsonObject: JsonObject): LiveData<AddressResponseModel> {
        viewModelScope.launch {
            val result = repositry.updateAddressRequest(userID,jsonObject)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }


}