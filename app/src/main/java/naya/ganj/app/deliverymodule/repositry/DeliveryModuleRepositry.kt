package naya.ganj.app.deliverymodule.repositry

import com.google.gson.JsonObject
import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant
import okhttp3.RequestBody

class DeliveryModuleRepositry(val apiInterface: ApiInterface) {

    suspend fun getOrderDetail(userId:String?,jsonObject: JsonObject)=apiInterface.getOrderDetail(userId,Constant.DEVICE_TYPE,jsonObject)

    suspend fun getDeliveryOrdersRequest(userId: String?, jsonObject: JsonObject) = apiInterface.deliverOrderPaymentRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun changeOrderStatusAPIRequest(userId: String?, jsonObject: JsonObject) = apiInterface.changeOrderStatusAPIRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun refundRequest(userId: String?, jsonObject: RequestBody) = apiInterface.refundRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun returnProducApiRequest(userId: String?, jsonObject: JsonObject) = apiInterface.returnProducApiRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun setDeliveryBoyLocationRequest(userId: String?, jsonObject: JsonObject) = apiInterface.setDeliveryBoyLocationRequest(userId, Constant.DEVICE_TYPE, jsonObject)

}

