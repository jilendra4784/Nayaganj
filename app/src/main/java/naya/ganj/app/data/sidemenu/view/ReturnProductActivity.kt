package naya.ganj.app.data.sidemenu.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naya.ganj.app.Nayaganj
import naya.ganj.app.data.sidemenu.adapter.ReturnProductAdapter
import naya.ganj.app.data.sidemenu.model.OrderDetailModel
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.OrderDetailViewModel
import naya.ganj.app.data.sidemenu.viewmodel.ReturnOrderViewModel
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.databinding.ActivityReturnProductBinding
import naya.ganj.app.interfaces.ProductSelectListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.ReturnProduct
import naya.ganj.app.utility.Constant
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ReturnProductActivity : AppCompatActivity() , ProductSelectListener {

    lateinit var binding : ActivityReturnProductBinding
    lateinit var app:Nayaganj
    var orderID=""
    lateinit var viewModel: ReturnOrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityReturnProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            SideMenuViewModelFactory(SideMenuDataRepositry(RetrofitClient.instance))
        )[ReturnOrderViewModel::class.java]
        orderID=intent.getStringExtra(Constant.orderId).toString()
        binding.includeLayout.ivBackArrow.setOnClickListener{finish()}
        binding.includeLayout.toolbarTitle.text="Return Product List"
        app=applicationContext as Nayaganj
        val gson= Gson()
        val orderDetailValue=intent.getStringExtra("productlist")
        val orderDetailModel: OrderDetailModel=gson.fromJson(orderDetailValue,OrderDetailModel::class.java)

        init(orderDetailModel)

        binding.btnSumit.setOnClickListener{
            sendReturnRequest()

        }

        lifecycleScope.launch(Dispatchers.IO){
            AppDataBase.getInstance(this@ReturnProductActivity).productDao().deleteAllReturnItem()
        }

    }

    private fun sendReturnRequest() {
        lifecycleScope.launch(Dispatchers.IO){
           val list: List<ReturnProduct> = AppDataBase.getInstance(this@ReturnProductActivity).productDao().getReturnProductList()

            val jsonArray = JSONArray()
            for (item in list) {
                val itemsData = JSONObject()
                itemsData.put(Constant.PRODUCT_ID, item.productId)
                itemsData.put(Constant.VARIANT_ID, item.variantId)
                itemsData.put(Constant.QUANTITY, item.itemQuantity)
                jsonArray.put(itemsData)
            }

            val obj=JSONObject()
            obj.put(Constant.productsArr, jsonArray)
            obj.put(Constant.orderId, orderID)

            val myreqbody = JSONObject(obj.toString()).toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            withContext(Dispatchers.Main){
                viewModel.sendReturnRequest(app.user.getUserDetails()?.userId,myreqbody).observe(this@ReturnProductActivity){
                    Log.e("TAG", "sendReturnRequest: "+it )
                    if(it!=null){
                        if(it.status){
                            Toast.makeText(this@ReturnProductActivity,"Return Order Request send Successfully.",Toast.LENGTH_SHORT).show()
                            finish()
                        }else{
                            Toast.makeText(this@ReturnProductActivity,it.msg,Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun init(orderDetailModel: OrderDetailModel) {
        binding.returnList.layoutManager=LinearLayoutManager(this@ReturnProductActivity)
        binding.returnList.adapter=ReturnProductAdapter(this@ReturnProductActivity,app,orderDetailModel.orderDetails.products,this)

    }

    override fun onSelectProduct(productId: String, variantId: String, itemQuantity: Int) {
        lifecycleScope.launch(Dispatchers.IO){
            val isProductExist=AppDataBase.getInstance(this@ReturnProductActivity).productDao().isReturnExist(productId,variantId)
            if(!isProductExist){
                val returnProduct =ReturnProduct(productId,variantId,itemQuantity)
                AppDataBase.getInstance(this@ReturnProductActivity).productDao().insertReturnProduct(returnProduct)
            }else
            {
                AppDataBase.getInstance(this@ReturnProductActivity).productDao().updateReturnProduct(productId,variantId,itemQuantity)
            }
        }
    }


}