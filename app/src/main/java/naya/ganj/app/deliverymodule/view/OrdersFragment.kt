package naya.ganj.app.deliverymodule.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.FragmentOrdersBinding
import naya.ganj.app.deliverymodule.adapter.DeliverOrdersAdapter
import naya.ganj.app.deliverymodule.model.DeliveryOrdersModel
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleFactory
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersFragment : Fragment() {

    lateinit var binding: FragmentOrdersBinding
    lateinit var viewModel: DeliveryModuleViewModel
    lateinit var app: Nayaganj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            requireActivity(),
            DeliveryModuleFactory(DeliveryModuleRepositry(RetrofitClient.instance))
        )[DeliveryModuleViewModel::class.java]
        app = requireActivity().applicationContext as Nayaganj
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        binding.btnDeliveryOrder.setOnClickListener {

            binding.rvDeliveryOrdersList.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))

            Handler(Looper.getMainLooper()).postDelayed( { getOrderList(app.user.getUserDetails()?.userId, "") },100)

        }
        binding.btnReturnOrder.setOnClickListener {
            binding.rvReturnOrdersList.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))
            Handler(Looper.getMainLooper()).postDelayed( { getOrderList(app.user.getUserDetails()?.userId, Constant.RETURNVERIFIED) },100)

        }

        // Delivery Orders List
        binding.rvDeliveryOrdersList.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvDeliveryOrdersList.isNestedScrollingEnabled = false

        binding.rvReturnOrdersList.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvReturnOrdersList.isNestedScrollingEnabled = false

        getOrderList(app.user.getUserDetails()?.userId, "")

    }


    private fun getOrderList(userId: String?, orderStatus: String) {
        if (Utility().isAppOnLine(requireActivity())) {
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.DeliveryOrderStatus, orderStatus);

            RetrofitClient.instance.getDeliveryOrders(userId,Constant.DEVICE_TYPE,jsonObject).enqueue(object : Callback<DeliveryOrdersModel> {
                override fun onResponse(
                    call: Call<DeliveryOrdersModel>,
                    response: Response<DeliveryOrdersModel>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if(response.isSuccessful){
                        val it=response.body()
                        if(orderStatus == ""){
                            if (it != null && it.ordersList.isNotEmpty()) {
                                binding.rvDeliveryOrdersList.adapter = DeliverOrdersAdapter(requireActivity(), it.ordersList, orderStatus)
                                binding.rvDeliveryOrdersList.visibility = View.VISIBLE
                                binding.rvReturnOrdersList.visibility=View.GONE
                            }
                        }else{
                            if (it != null && it.ordersList.isNotEmpty()) {
                                binding.rvReturnOrdersList.adapter = DeliverOrdersAdapter(requireActivity(), it.ordersList, orderStatus)
                                binding.rvReturnOrdersList.visibility = View.VISIBLE
                                binding.rvDeliveryOrdersList.visibility = View.GONE
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<DeliveryOrdersModel>, t: Throwable) {

                }
                })
        }
    }
}