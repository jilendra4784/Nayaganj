package naya.ganj.app.deliverymodule.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.databinding.FragmentOrderHistoryBinding
import naya.ganj.app.deliverymodule.adapter.DeliveredOrdersAdapter
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleFactory
import naya.ganj.app.deliverymodule.repositry.DeliveryModuleRepositry
import naya.ganj.app.deliverymodule.viewmodel.DeliveryModuleViewModel
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.Constant
import naya.ganj.app.utility.Utility

class OrdersHistoryFragment : Fragment() {

    lateinit var binding: FragmentOrderHistoryBinding
    lateinit var viewModel: DeliveryModuleViewModel
    private var orderType = "delivery"
    lateinit var app: Nayaganj

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

        setUpUI()

        binding.btnDeliveryOrder.setOnClickListener {
            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))
            orderType = "delivery"
            getOrderHistory(orderType)

        }
        binding.btnReturnOrder.setOnClickListener { binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))

            orderType="return"
            getOrderHistory(orderType)
        }
        getOrderHistory(orderType)
    }

    private fun setUpUI() {
        binding.rvDeliveredOrderList.visibility = View.GONE
        binding.rvDeliveredOrderList.layoutManager = LinearLayoutManager(requireActivity())

        binding.rvReturnedOrderList.visibility = View.GONE
        binding.rvReturnedOrderList.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun getOrderHistory(orderType:String) {

        if(Utility().isAppOnLine(requireActivity())){
            binding.progressBar.visibility = View.VISIBLE
            val jsonObject = JsonObject()
            jsonObject.addProperty(Constant.Type, orderType)
            viewModel.getDeliveredOrdersData(app.user.getUserDetails()?.userId, jsonObject)
                .observe(requireActivity()) {
                    if (it != null) {
                        if (isAdded) {
                            if (orderType.equals("delivery")) {
                                binding.rvDeliveredOrderList.adapter =
                                    DeliveredOrdersAdapter(requireActivity(), orderType,it.ordersList)
                                binding.rvDeliveredOrderList.visibility = View.VISIBLE
                                binding.rvReturnedOrderList.visibility = View.GONE
                                binding.progressBar.visibility = View.GONE

                            } else {
                                binding.rvReturnedOrderList.adapter =
                                    DeliveredOrdersAdapter(requireActivity(),orderType ,it.ordersList)
                                binding.rvReturnedOrderList.visibility = View.VISIBLE
                                binding.rvDeliveredOrderList.visibility = View.GONE
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
        }
    }
}