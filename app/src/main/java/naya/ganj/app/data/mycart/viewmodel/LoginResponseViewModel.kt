package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.model.LoginResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import okhttp3.RequestBody
import org.json.JSONObject

class LoginResponseViewModel(val repo: AddressListRespositry) : ViewModel() {

    val mutableLiveData = MutableLiveData<LoginResponseModel>()
    private val apiResponseData = MutableLiveData<ApiResponseModel>()

    fun getLoginResponse(userId : String,jsonObject: JsonObject): LiveData<LoginResponseModel> {
        viewModelScope.launch {
            val result=repo.sendLoginRequest(userId,jsonObject)
            mutableLiveData.value=result.body()

        }
        return  mutableLiveData
    }

    fun getAutoLoginResponse(userId : String,jsonObject: JsonObject): LiveData<LoginResponseModel> {
        viewModelScope.launch {
            val result=repo.sendAutoLoginRequest(userId,jsonObject)
            mutableLiveData.value=result.body()

        }
        return  mutableLiveData
    }

    fun synchDataRequest(userId: String, jsonObject: RequestBody) : LiveData<ApiResponseModel>{
        viewModelScope.launch {
            val result=repo.synchDataRequest(userId,jsonObject)
            apiResponseData.value=result.body()
        }
        return apiResponseData
    }


}