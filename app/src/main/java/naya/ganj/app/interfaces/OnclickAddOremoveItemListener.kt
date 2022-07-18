package naya.ganj.app.interfaces

import naya.ganj.app.roomdb.entity.ProductDetail

interface OnclickAddOremoveItemListener {
    fun onClickAddOrRemoveItem(action: String, productDetail: ProductDetail)
}