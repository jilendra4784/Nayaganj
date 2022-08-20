package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.NetworkError
import naya.ganj.app.data.mycart.model.OrderPlacedModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.utility.NetworkResult

class PaymentOptionsViewModel(var repo: AddressListRespositry) : ViewModel() {

     private var mutableLiveData= MutableLiveData<NetworkResult<OrderPlacedModel>>()

    fun orderPlaceRequest(userId:String?,jsonObject: JsonObject): LiveData<NetworkResult<OrderPlacedModel>> {
        viewModelScope.launch {

            try{
                val result = repo.orderPlaceRequest(userId,jsonObject)
                if(result.isSuccessful){
                    mutableLiveData.value = NetworkResult.Success(result.body()!!)
                }else {
                    mutableLiveData.value = NetworkResult.Error(result.body()!!.msg)
                }
            }catch (e: Exception){
                mutableLiveData.value = NetworkResult.Error(e.message.toString())
            }

        }
        return mutableLiveData
    }
}