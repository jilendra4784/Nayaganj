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

class DeliveryModuleViewModel(val repositry: DeliveryModuleRepositry): ViewModel() {
    val mutableLiveData = MutableLiveData<ApiResponseModel>()
    val changeOrderStatusAPI = MutableLiveData<ApiResponseModel>()
    val deliveredOrdersData = MutableLiveData<DeliveredOrdersModel>()
    val orderDetailLiveData = MutableLiveData<DeliveryOrderDetailModel>()

    fun getDeliveredOrdersData(userId:String?,jsonObject: JsonObject): LiveData<DeliveredOrdersModel> {
        viewModelScope.launch {
            val result=repositry.getDeliveredOrdersRequest(userId,jsonObject)
            deliveredOrdersData.value=result.body()
        }
        return deliveredOrdersData
    }


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




}