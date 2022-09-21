package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.CouponModel
import naya.ganj.app.data.mycart.model.CouponResponseModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.utility.NetworkResult

class CouponViewModel(val addressListRespositry: AddressListRespositry) : ViewModel() {
    var mutableLiveData = MutableLiveData<NetworkResult<CouponModel>>()
    var applyOffer = MutableLiveData<NetworkResult<CouponResponseModel>>()

    fun getCouponList(userId:String?): LiveData<NetworkResult<CouponModel>> {
        viewModelScope.launch {
            val result= addressListRespositry.getCouponList(userId)
            if(result.isSuccessful){
                mutableLiveData.value=NetworkResult.Success(result.body()!!)
            }else{
                mutableLiveData.value=NetworkResult.Error(result.body()!!.msg)
            }
        }
        return mutableLiveData
    }

    fun applyCouponRequest(userId:String?,jsonObject: JsonObject): LiveData<NetworkResult<CouponResponseModel>> {
        viewModelScope.launch {
            val result= addressListRespositry.applyCouponRequest(userId,jsonObject)
            if(result.isSuccessful){
                applyOffer.value=NetworkResult.Success(result.body()!!)
            }else{
                applyOffer.value=NetworkResult.Error(result.body()!!.msg)
            }
        }
        return applyOffer
    }






}