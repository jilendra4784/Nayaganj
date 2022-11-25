package naya.ganj.app.data.mycart.repositry

import com.google.gson.JsonObject
import naya.ganj.app.retrofit.ApiInterface
import naya.ganj.app.utility.Constant
import okhttp3.RequestBody

class AddressListRespositry(var apiInterface: ApiInterface) {

    suspend fun getAddressList(userId: String?) =
        apiInterface.getAddressList(userId, Constant.DEVICE_TYPE)


    fun getAddressListForMain(userId: String?) =
        apiInterface.getAddressListForMain(userId, Constant.DEVICE_TYPE)


    suspend fun addAddressRequest(userId: String?, jsonObject: JsonObject) =
        apiInterface.addAddressRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun deleteAddressRequest(userId: String?, jsonObject: JsonObject) =
        apiInterface.deleteAddressRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun updateAddressRequest(userId: String?, jsonObject: JsonObject) =
        apiInterface.updateAddressRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun setAddress(userId: String?, jsonObject: JsonObject) =
        apiInterface.setAddress(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun orderPlaceRequest(userId: String?, jsonObject: JsonObject) =
        apiInterface.orderPlaceRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun paytmPurchaseRequest(userId: String?, jsonObject: JsonObject) =
        apiInterface.paytmPurchaseRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun verifyTransactionStatus(userId: String?, jsonObject: JsonObject) =
        apiInterface.verifyTransactionStatus(userId, Constant.DEVICE_TYPE, jsonObject)




    suspend fun getOTPRequest( jsonObject: JsonObject) =
        apiInterface.getOTPRequest("", Constant.DEVICE_TYPE, jsonObject)

    suspend fun sendLoginRequest(userId: String, jsonObject: JsonObject) =
        apiInterface.sendLoginRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun sendAutoLoginRequest(userId :String,jsonObject: JsonObject) =
        apiInterface.sendAutoLoginRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun synchDataRequest(userId :String,jsonObject: RequestBody) =
        apiInterface.synchDataRequest(userId, Constant.DEVICE_TYPE, jsonObject)



    suspend fun updateProfileRequest(userId:String?,jsonObject: JsonObject) =
        apiInterface.updateProfileRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun placeVirtualOrderRequest(userId:String?,jsonObject: JsonObject) =
        apiInterface.placeVirtualOrderRequest(userId, Constant.DEVICE_TYPE, jsonObject)

    suspend fun getCouponList(userId:String?) = apiInterface.getCouponList(userId, Constant.DEVICE_TYPE)
    suspend fun applyCouponRequest(userId:String?,jsonObject: JsonObject) = apiInterface.applyCouponRequest(userId, Constant.DEVICE_TYPE,jsonObject)



}