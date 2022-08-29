package naya.ganj.app.data.mycart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.CouponModel
import naya.ganj.app.data.mycart.repositry.AddressListRespositry
import naya.ganj.app.utility.NetworkResult

class CouponViewModel(val addressListRespositry: AddressListRespositry) : ViewModel() {
    var mutableLiveData = MutableLiveData<NetworkResult<CouponModel>>()

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
}