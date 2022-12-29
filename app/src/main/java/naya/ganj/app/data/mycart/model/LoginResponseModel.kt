package naya.ganj.app.data.mycart.model


import com.google.gson.annotations.SerializedName

data class LoginResponseModel(
    @SerializedName("isNewUser")
    val isNewUser: Boolean,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("userDetails")
    val userDetails: UserDetails
) {

    data class UserDetails(
        @SerializedName("configObj")
        var configObj: ConfigObj,
        @SerializedName("deviceId")
        val deviceId: String,
        @SerializedName("deviceToken")
        val deviceToken: String,
        @SerializedName("emailId")
        var emailId: String,
        @SerializedName("mNumber")
        val mNumber: String,
        @SerializedName("name")
        var name: String,
        @SerializedName("role")
        val role: String,
        @SerializedName("userId")
        var userId: String,
        @SerializedName("wallet")
        val wallet: Int
    ) {
        data class ConfigObj(
            @SerializedName("AddressType")
            val addressType: List<String>,
            @SerializedName("cities")
            val cities: List<String>,
            @SerializedName("deliveryAttemptLim")
            val deliveryAttemptLim: Int,
            @SerializedName("deliveryCharges")
            val deliveryCharges: Int,
            @SerializedName("deliveryChargesThreshHold")
            val deliveryChargesThreshHold: Int,
            @SerializedName("deliveryOrderStatusSeries")
            val deliveryOrderStatusSeries: List<String>,
            @SerializedName("orderStatus")
            val orderStatus: List<OrderStatu>,
            @SerializedName("productStatus")
            val productStatus: List<ProductStatu>,
            @SerializedName("reScheduleButton")
            val reScheduleButton: String,
            @SerializedName("refundButton")
            val refundButton: String,
            @SerializedName("returnAttemptLim")
            val returnAttemptLim: Int,
            @SerializedName("virtualOrderStatus")
            val virtualOrderStatus: List<VirtualOrderStatu>,
            @SerializedName("productImgUrl")
            val productImgUrl: String,
        ) {
            data class OrderStatu(
                @SerializedName("CANCELLED")
                val cANCELLED: String,
                @SerializedName("COLLECTED")
                val cOLLECTED: String,
                @SerializedName("DELIVERED")
                val dELIVERED: String,
                @SerializedName("DISPATCHED")
                val dISPATCHED: String,
                @SerializedName("FAILURE")
                val fAILURE: String,
                @SerializedName("PENDING")
                val pENDING: String,
                @SerializedName("RETURNCANCELLED")
                val rETURNCANCELLED: String,
                @SerializedName("RETURNCOLLECTED")
                val rETURNCOLLECTED: String,
                @SerializedName("RETURNINITIATED")
                val rETURNINITIATED: String,
                @SerializedName("RETURNPARTIALCOLLECTED")
                val rETURNPARTIALCOLLECTED: String,
                @SerializedName("RETURNPARTIALSUCCESS")
                val rETURNPARTIALSUCCESS: String,
                @SerializedName("RETURNSUCCESS")
                val rETURNSUCCESS: String,
                @SerializedName("RETURNVERIFIED")
                val rETURNVERIFIED: String,
                @SerializedName("VERIFIED")
                val vERIFIED: String
            )

            data class ProductStatu(
                @SerializedName("CANCELLED")
                val cANCELLED: String,
                @SerializedName("COLLECTED")
                val cOLLECTED: String,
                @SerializedName("DELIVERED")
                val dELIVERED: String,
                @SerializedName("DISPATCHED")
                val dISPATCHED: String,
                @SerializedName("FAILURE")
                val fAILURE: String,
                @SerializedName("PENDING")
                val pENDING: String,
                @SerializedName("RETURNCANCELLED")
                val rETURNCANCELLED: String,
                @SerializedName("RETURNCOLLECTED")
                val rETURNCOLLECTED: String,
                @SerializedName("RETURNINITIATED")
                val rETURNINITIATED: String,
                @SerializedName("RETURNPARTIAL")
                val rETURNPARTIAL: String,
                @SerializedName("RETURNSUCCESS")
                val rETURNSUCCESS: String,
                @SerializedName("RETURNVERIFIED")
                val rETURNVERIFIED: String,
                @SerializedName("VERIFIED")
                val vERIFIED: String
            )

            data class VirtualOrderStatu(
                @SerializedName("PENDING")
                val pENDING: String,
                @SerializedName("SUCCESS")
                val sUCCESS: String
            )
        }
    }
}