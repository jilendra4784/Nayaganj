package naya.ganj.app.interfaces

interface ProductSelectListener {
    fun onSelectProduct(productId:String,variantId:String,itemQuantity:Int)
}