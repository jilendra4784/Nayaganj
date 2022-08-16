package naya.ganj.app.deliverymodule.repositry

import com.google.gson.JsonObject
import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant

class DeliveryModuleRepositry(val apiInterface: ApiInterface) {
   suspend fun getDeliveryOrdersRequest(userId:String?,jsonObject: JsonObject)=apiInterface.getDeliveryOrders(userId,Constant.DEVICE_TYPE,jsonObject)

   suspend fun getDeliveredOrdersRequest(userId:String?,jsonObject: JsonObject)=apiInterface.getDeliveredOrders(userId,Constant.DEVICE_TYPE,jsonObject)

   suspend fun getOrderDetail(userId:String?,jsonObject: JsonObject)=apiInterface.getOrderDetail(userId,Constant.DEVICE_TYPE,jsonObject)


}

