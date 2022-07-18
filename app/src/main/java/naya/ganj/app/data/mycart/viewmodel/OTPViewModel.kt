package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class OTPViewModel(val repo: AddressListRespositry) : ViewModel() {

    val mutableLiveData = MutableLiveData<ApiResponseModel>()

    fun getOTPRequest(jsonObject: JsonObject): LiveData<ApiResponseModel> {

        viewModelScope.launch {
            val result = repo.getOTPRequest(jsonObject)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }
}