package naya.ganj.app.deliverymodule.view

import android.content.res.ColorStateList
import android.graphics.Color.red
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import naya.ganj.app.R
import naya.ganj.app.databinding.FragmentOrdersBinding
import org.webrtc.ContextUtils.getApplicationContext

class OrdersFragment : Fragment() {

    lateinit var binding: FragmentOrdersBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDeliveryOrder.setOnClickListener{
            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))

            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))

        }
        binding.btnReturnOrder.setOnClickListener{
            binding.btnReturnOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.purple_500))
            binding.btnReturnOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))

            binding.btnDeliveryOrder.background.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
            binding.btnDeliveryOrder.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_color))
        }
    }
}