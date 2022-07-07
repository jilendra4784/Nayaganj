package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import naya.ganj.app.data.mycart.model.OrderPlacedModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class PaymentOptionsViewModel(var repo: AddressListRespositry) : ViewModel() {

     private var mutableLiveData= MutableLiveData<OrderPlacedModel>()

    fun orderPlaceRequest(jsonObject: JsonObject): LiveData<OrderPlacedModel> {
        viewModelScope.launch {
            val result = repo.orderPlaceRequest(jsonObject)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }
}