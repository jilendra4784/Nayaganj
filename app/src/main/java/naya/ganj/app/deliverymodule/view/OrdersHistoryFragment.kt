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
import naya.ganj.app.databinding.FragmentOrderHistoryBinding
import naya.ganj.app.deliverymodule.adapter.OrdersHistoryAdapter
import naya.ganj.app.deliverymodule.model.DeliveredOrdersModel
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleFactory
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersHistoryFragment : Fragment() {

    lateinit var binding: FragmentOrderHistoryBinding
    lateinit var viewModel: DeliveryModuleViewModel
    private var orderType = "delivery"
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
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        app = requireActivity().applicationContext as Nayaganj

        viewModel = ViewModelProvider(
            requireActivity(),
            DeliveryModuleFactory(DeliveryModuleRepositry(RetrofitClient.instance))
        )[DeliveryModuleViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDeliveryOrder.setOnClickListener {
            binding.rvDeliveredOrderList.visibility = View.GONE
            binding.rvReturnedOrderList.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))
            orderType = "delivery"

            Handler(Looper.getMainLooper()).postDelayed({getOrderHistory(orderType)},300)


        }
        binding.btnReturnOrder.setOnClickListener {

            binding.rvDeliveredOrderList.visibility = View.GONE
            binding.rvReturnedOrderList.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))

            orderType="return"
            Handler(Looper.getMainLooper()).postDelayed({getOrderHistory(orderType)},300)
        }

        setUpUI()
        getOrderHistory(orderType)
    }

    private fun setUpUI() {
        binding.rvDeliveredOrderList.visibility = View.GONE
        binding.rvDeliveredOrderList.layoutManager = LinearLayoutManager(requireActivity())

        binding.rvReturnedOrderList.visibility = View.GONE
        binding.rvReturnedOrderList.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun getOrderHistory(orderType:String) {

        if(Utility.isAppOnLine(requireActivity())){

            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.Type, orderType)

            RetrofitClient.instance.getDeliveredOrdersData(app.user.getUserDetails()?.userId,Constant.DEVICE_TYPE ,jsonObject)
                .enqueue(object : Callback<DeliveredOrdersModel>{
                    override fun onResponse(
                        call: Call<DeliveredOrdersModel>,
                        response: Response<DeliveredOrdersModel>
                    ) {
                        if (isAdded) {
                            if (orderType == "delivery") {
                                binding.rvDeliveredOrderList.adapter =
                                    OrdersHistoryAdapter(requireActivity(), orderType,response.body()!!.ordersList)
                                binding.rvDeliveredOrderList.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE

                            } else {
                                binding.rvReturnedOrderList.adapter =
                                    OrdersHistoryAdapter(requireActivity(),orderType ,response.body()!!.ordersList)
                                binding.rvReturnedOrderList.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    }

                    override fun onFailure(call: Call<DeliveredOrdersModel>, t: Throwable) {
                        Utility.serverNotResponding(requireActivity())
                    }
                })

        }
    }
}