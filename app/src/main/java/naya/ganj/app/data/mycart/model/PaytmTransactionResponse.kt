package naya.ganj.app.data.mycart.model

data class PaytmTransactionResponse(
    val BANKTXNID: String,
    val RESPCODE: String,
    val RESPMSG: String,
    val STATUS: String,
    val ORDERID:String
)
