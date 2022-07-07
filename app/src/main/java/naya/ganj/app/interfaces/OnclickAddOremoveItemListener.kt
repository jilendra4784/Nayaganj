package naya.ganj.app.interfaces

interface OnclickAddOremoveItemListener {
    fun onClickAddOrRemoveItem(action: String, productId: String, variantId: String,promoCode:String,totalAmount :Double)
}