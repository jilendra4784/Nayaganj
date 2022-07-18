package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.LoginResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry

class LoginResponseViewModel(val repo: AddressListRespositry) : ViewModel() {

    val mutableLiveData = MutableLiveData<LoginResponseModel>()

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


}