package naya.ganj.app.deliverymodule.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import naya.ganj.app.data.mycart.model.ApiResponseModel
import naya.ganj.app.deliverymodule.model.DeliveredOrdersModel
import naya.ganj.app.deliverymodule.model.DeliveryOrdersModel
import naya.ganj.app.deliverymodule.model.DeliveryOrderDetailModel
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import okhttp3.RequestBody

class DeliveryModuleViewModel(val repositry: DeliveryModuleRepositry): ViewModel() {
    val mutableLiveData = MutableLiveData<ApiResponseModel>()
    val changeOrderStatusAPI = MutableLiveData<ApiResponseModel>()
    val refundMutableData = MutableLiveData<ApiResponseModel>()
    val reschudule = MutableLiveData<ApiResponseModel>()

    val orderDetailLiveData = MutableLiveData<DeliveryOrderDetailModel>()


    fun getOrderDetail(userId:String?,jsonObject: JsonObject): LiveData<DeliveryOrderDetailModel> {
        viewModelScope.launch {
            val result=repositry.getOrderDetail(userId,jsonObject)
            orderDetailLiveData.value=result.body()
        }
        return orderDetailLiveData
    }


    fun deliverOrderPaymentRequest(userId:String?,jsonObject: JsonObject): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result=repositry.getDeliveryOrdersRequest(userId,jsonObject)
            mutableLiveData.value=result.body()
        }

        return mutableLiveData
    }

    fun changeOrderStatusAPIRequest(userId:String?,jsonObject: JsonObject): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result=repositry.changeOrderStatusAPIRequest(userId,jsonObject)
            changeOrderStatusAPI.value=result.body()
        }

        return changeOrderStatusAPI
    }

    fun refundRequest(userId:String?,jsonObject: RequestBody): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result=repositry.refundRequest(userId,jsonObject)
            refundMutableData.value=result.body()
        }

        return refundMutableData
    }

    fun returnProducApiRequest(userId:String?,jsonObject: JsonObject): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result=repositry.returnProducApiRequest(userId,jsonObject)
            refundMutableData.value=result.body()
        }

        return refundMutableData
    }

    fun setDeliveryBoyLocationRequest(userId:String?,jsonObject: JsonObject): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result=repositry.setDeliveryBoyLocationRequest(userId,jsonObject)
            refundMutableData.value=result.body()
        }

        return refundMutableData
    }

    fun sendReschduleRequest(userId:String?,jsonObject: JsonObject): LiveData<ApiResponseModel> {
        viewModelScope.launch {
            val result=repositry.sendReschduleRequest(userId,jsonObject)
            reschudule.value=result.body()
        }

        return reschudule
    }




}