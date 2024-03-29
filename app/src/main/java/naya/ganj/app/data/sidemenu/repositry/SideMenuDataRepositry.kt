package naya.ganj.app.data.sidemenu.repositry

import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant
import com.google.gson.JsonObject
import okhttp3.RequestBody

class SideMenuDataRepositry(val api : ApiInterface) {

   suspend fun getMyOrderList(userid:String)=api.getMyOrderList(userid,Constant.DEVICE_TYPE)

   suspend fun getOrderDetailRequest(userId:String,jsonObject: JsonObject) =api.getOrderDetailRequest(userId,Constant.DEVICE_TYPE,jsonObject)

   suspend fun updateUserDetailRequest(userId:String?,jsonObject: JsonObject) =api.updateUserDetailRequest(userId,Constant.DEVICE_TYPE,jsonObject)

   suspend fun updateMobileNumber(userId:String?,jsonObject: JsonObject) =api.updateMobileNumber(userId,Constant.DEVICE_TYPE,jsonObject)

   suspend fun getMyVirtualOrderList(userid:String?)=api.getMyVirtualOrderList(userid,Constant.DEVICE_TYPE)

   suspend fun sendRetailerRequest(userid:String?,jsonObject: JsonObject)=api.sendRetailerRequest(userid,Constant.DEVICE_TYPE,jsonObject)

   suspend fun changeOrderStatusRequest(userId:String,jsonObject: JsonObject) =api.changeOrderStatusRequest(userId,Constant.DEVICE_TYPE,jsonObject)

   suspend fun sendReturnRequest(userId:String?,jsonObject: RequestBody) =api.sendReturnRequest(userId,Constant.DEVICE_TYPE,jsonObject)

   suspend fun getWalletBalance(userId:String?) =api.getWalletBalance(userId,Constant.DEVICE_TYPE)

}