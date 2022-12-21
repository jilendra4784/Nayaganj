package naya.ganj.app.data.sidemenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import okhttp3.RequestBody

class ReturnOrderViewModel(val repo : SideMenuDataRepositry):ViewModel() {

    val mutableLiveData = MutableLiveData<ApiResponseModel>()

    fun sendReturnRequest(userId:String?,jsonObject: RequestBody): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result = repo.sendReturnRequest(userId,jsonObject)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }

}