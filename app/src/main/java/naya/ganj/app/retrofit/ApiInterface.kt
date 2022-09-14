package naya.ganj.app.retrofit


import com.google.gson.JsonObject
import naya.ganj.app.data.category.model.*
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.data.mycart.model.*
import naya.ganj.app.data.sidemenu.model.MyOrderListModel
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import naya.ganj.app.data.sidemenu.model.VirtualOrderModel
import naya.ganj.app.deliverymodule.model.DeliveredOrdersModel
import naya.ganj.app.deliverymodule.model.DeliveryOrderDetailModel
import naya.ganj.app.deliverymodule.model.DeliveryOrdersModel
import okhttp3.RequestBody
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
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<CheckProductInCartModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_REMOVE_ITEM_URL)
    fun removeProduct(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<ApiResponseModel>


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
    @GET(URLConstant.URL_ADDRESS_LIST_URL)
     fun getAddressListForMain(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String
    ): Call<AddressListModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_ADD_ADDRESS_URL)
    fun AddAddressForMain(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<AddressListModel>

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
        @Header("userid") userid: String?,
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
    @GET(URLConstant.URL_MY_VIRTUAL_ORDER_LIST_URL)
    suspend fun getMyVirtualOrderList(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String
    ): Response<VirtualOrderModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_RETAILER_URL)
    suspend fun sendRetailerRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_ORDER_DETAIL_URL)
    suspend fun getOrderDetailRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<OrderDetailModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_UPDATE_USER_DETAIL_URL)
    suspend fun updateUserDetailRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<LoginResponseModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_UPDATE_USER_DETAIL_URL)
    suspend fun updateMobileNumber(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>







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

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST(URLConstant.URL_SYNCH_CARTDATA_URL)
    suspend fun synchDataRequest(
        @Header("userid") userid: String,
        @Header("devicetype") d: String,
        @Body jsonObject: RequestBody
    ): Response<ApiResponseModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_UPDATE_DETAIL_URL)
    suspend fun updateProfileRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_PLACE_VIRTUAL_ORDER_URL)
    suspend fun placeVirtualOrderRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>

    @Headers("Content-Type: application/json")
    @GET(URLConstant.URL_COUPON_URL)
    suspend fun getCouponList(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String
    ): Response<CouponModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_SEARCH_PRODUCT_LIST_URL)
    suspend fun getSearchDataRequest(
        @Header("text") text: String,
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ProductListModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_HOME_PAGE_URL)
    suspend fun getBannerData(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<HomePageModel>

    // Delivery Module

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_DELIVERY_ORDERS_URL)
     fun getDeliveryOrders(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<DeliveryOrdersModel>

    /*@Headers("Content-Type: application/json")
    @POST(URLConstant.URL_DELIVERED_ORDERS_URL)
    suspend fun getDeliveredOrders(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<DeliveredOrdersModel>*/

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_DELIVERED_ORDERS_URL)
     fun getDeliveredOrdersData(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Call<DeliveredOrdersModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_DELIVERED_ORDERS_DETAIL_URL)
    suspend fun getOrderDetail(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<DeliveryOrderDetailModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_DELIVERED_ORDERS_PAYMENT_REQUEST_URL)
    suspend fun deliverOrderPaymentRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_CHANGE_ORDER_STATUS_URL)
    suspend fun changeOrderStatusAPIRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_REFUND_ORDER_URL)
    suspend fun refundRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: RequestBody
    ): Response<ApiResponseModel>

    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_RETURN_PRODUCT_URL)
    suspend fun returnProducApiRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>


    @Headers("Content-Type: application/json")
    @POST(URLConstant.URL_SET_DELIVERY_BOY_LOCATION_URL)
    suspend fun setDeliveryBoyLocationRequest(
        @Header("userid") userid: String?,
        @Header("devicetype") d: String,
        @Body jsonObject: JsonObject
    ): Response<ApiResponseModel>

}