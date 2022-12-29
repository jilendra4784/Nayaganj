package naya.ganj.app.utility


import com.google.gson.annotations.SerializedName

data class ConfigModel(
    @SerializedName("configData")
    val configData: ConfigData,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Boolean
) {
    data class ConfigData(
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
        @SerializedName("productImgUrl")
        val productImgUrl: String,
        @SerializedName("productStatus")
        val productStatus: List<ProductStatu>,
        @SerializedName("reScheduleButton")
        val reScheduleButton: String,
        @SerializedName("refundButton")
        val refundButton: String,
        @SerializedName("returnAttemptLim")
        val returnAttemptLim: Int,
        @SerializedName("virtualOrderStatus")
        val virtualOrderStatus: List<VirtualOrderStatu>
    ) {
        data class OrderStatu(
            @SerializedName("CANCELLED")
            val cANCELLED: String?,
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
            val rETURNPARTIALCOLLECTED: String?,
            @SerializedName("RETURNPARTIALSUCCESS")
            val rETURNPARTIALSUCCESS: String?,
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