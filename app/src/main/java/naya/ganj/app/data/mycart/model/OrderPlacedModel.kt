package naya.ganj.app.data.mycart.model


data class OrderPlacedModel(
    val msg: String,
    val pToken: String,
    val status: Boolean,
    val paymentOrderId: String,
    val orderId: String
)