package naya.ganj.app.retrofit

import com.google.gson.JsonObject
import naya.ganj.app.data.category.model.*
import naya.ganj.app.data.mycart.model.*
import naya.ganj.app.data.sidemenu.model.MyOrderListModel
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @GET(URLConstant.cate_url)
    fun getAllCateData(): Call<CategoryDataModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.PRODUCT_LIST_URL)
    fun getProductList(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<ProductListModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.ADD_REMOVE_URL)
    fun addremoveItemRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<AddRemoveModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_MYCART_URL)
    fun getMyCartData(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<MyCartModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_CHECK_PRODUCT_IN_CART_URL)
    fun checkProductInCartRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<CheckProductInCartModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_PRODUCT_DETAIL_URL)
    fun getProductDetail(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<ProductDetailModel>


    @Headers("Content-Type: application/json")
    @GET(URLConstant.URL_ADDRESS_LIST_URL)
    suspend fun getAddressList(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String
    ): Response<AddressListModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_ADD_ADDRESS_URL)
    suspend fun addAddressRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<AddressResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_DELETE_ADDRESS_URL)
    suspend fun deleteAddressRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<AddressResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_UPDATE_ADDRESS_URL)
    suspend fun updateAddressRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<AddressResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_SET_ADDRESS_URL)
    suspend fun setAddress(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<AddressResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_CHECKOUT_URL)
    suspend fun orderPlaceRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<OrderPlacedModel>

    @Headers("Content-Type: application/json")
    @GET(URLConstant.URL_MY_ORDER_LIST_URL)
    suspend fun getMyOrderList(
        @Header("userid") userid: String,
        @Header("devicetype") d: String
    ): Response<MyOrderListModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_ORDER_DETAIL_URL)
    suspend fun getOrderDetailRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<OrderDetailModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_GET_OTP_URL)
    suspend fun getOTPRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_LOGIN_URL)
    suspend fun sendLoginRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<LoginResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_AUTO_LOGIN_URL)
    suspend fun sendAutoLoginRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<LoginResponseModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_UPDATE_DETAIL_URL)
    suspend fun updateProfileRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>


}