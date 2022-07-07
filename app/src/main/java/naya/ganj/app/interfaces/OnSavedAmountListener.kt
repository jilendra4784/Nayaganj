package naya.ganj.app.interfaces

interface OnSavedAmountListener {
    fun onSavedAmount(productId: String, variantId: Int, amount: Double)
}