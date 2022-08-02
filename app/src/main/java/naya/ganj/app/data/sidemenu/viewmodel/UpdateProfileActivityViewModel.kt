package naya.ganj.app.data.sidemenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.data.mycart.model.LoginResponseModel
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry

class UpdateProfileActivityViewModel(val repo: SideMenuDataRepositry) : ViewModel() {
    private val mutableLiveData = MutableLiveData<LoginResponseModel>()
    private val updateMobileNumber = MutableLiveData<ApiResponseModel>()

    fun updateUserDetailRequest(userId:String?,jsonObject: JsonObject): LiveData<LoginResponseModel> {
        viewModelScope.launch {
            val result = repo.updateUserDetailRequest(userId,jsonObject)
            mutableLiveData.value = result.body()
        }
        return mutableLiveData
    }

    fun updateMobileNumber(userId:String?,jsonObject: JsonObject): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result = repo.updateMobileNumber(userId,jsonObject)
            updateMobileNumber.value = result.body()
        }
        return updateMobileNumber
    }




}