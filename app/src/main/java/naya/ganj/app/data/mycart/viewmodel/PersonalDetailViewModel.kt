package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry

class PersonalDetailViewModel(val repo: AddressListRespositry) : ViewModel() {
    var mutableLiveData = MutableLiveData<ApiResponseModel>()

    fun updateProfileRequet(jsonObject: JsonObject): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result = repo.updateProfileRequest(jsonObject)
            mutableLiveData.value = result.body()
        }

        return mutableLiveData
    }
}