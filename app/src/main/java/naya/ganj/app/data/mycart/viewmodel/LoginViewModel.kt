package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import naya.ganj.app.data.mycart.model.LoginResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class LoginViewModel(val repo: AddressListRespositry) : ViewModel() {

    val mutableLiveData = MutableLiveData<LoginResponseModel>()

    fun getOTPRequest(jsonObject: JsonObject): LiveData<LoginResponseModel> {

        viewModelScope.launch {
            val result = repo.getOTPRequest(jsonObject)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }
}