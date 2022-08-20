package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.utility.NetworkResult

class OTPViewModel(val repo: AddressListRespositry) : ViewModel() {

    val mutableLiveData = MutableLiveData<NetworkResult<ApiResponseModel>>()

    fun getOTPRequest(jsonObject: JsonObject): LiveData<NetworkResult<ApiResponseModel>> {

        viewModelScope.launch {
            try{
                val result = repo.getOTPRequest(jsonObject)
                if(result.isSuccessful){
                    mutableLiveData.value =NetworkResult.Success(result.body()!!)
                }else{
                    mutableLiveData.value =NetworkResult.Error(result.message())
                }
            }catch (e: Exception){
                mutableLiveData.value =NetworkResult.Error(e.message.toString())
            }

        }
        return mutableLiveData
    }
}