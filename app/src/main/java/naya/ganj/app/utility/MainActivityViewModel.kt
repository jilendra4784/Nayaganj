package naya.ganj.app.utility

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.AddressListModel
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry

class MainActivityViewModel(val repo: AddressListRespositry) : ViewModel() {
    val addressData = MutableLiveData<AddressListModel>()
    val placeOrderResponse = MutableLiveData<ApiResponseModel>()

    fun getAddressListRequest(userId: String?): LiveData<AddressListModel> {
        viewModelScope.launch {
            val result = repo.getAddressList(userId)
            addressData.value = result.body()
        }

        return addressData
    }


    fun placeVirtualOrderRequest(
        userId: String?,
        jsonObject: JsonObject
    ): LiveData<ApiResponseModel> {

        viewModelScope.launch {
            val result = repo.placeVirtualOrderRequest(userId, jsonObject)
            placeOrderResponse.value = result.body()
        }

        return placeOrderResponse
    }
}