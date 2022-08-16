package naya.ganj.app.deliverymodule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.R
import naya.ganj.app.databinding.AdapterDeliveryOrdersLayoutBinding
import naya.ganj.app.deliverymodule.model.DeliveryOrdersModel

class DeliverOrdersAdapter(val context: Context,val orderList: List<DeliveryOrdersModel.Orders>) :
    RecyclerView.Adapter<DeliverOrdersAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: AdapterDeliveryOrdersLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            AdapterDeliveryOrdersLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val order: DeliveryOrdersModel.Orders = orderList[position]
        holder.binding.tvOrderId.text = order.orderId
        holder.binding.tvItems.text = order.totalItems.toString()+" "+"Items"
        holder.binding.tvAmount.text=context.resources.getString(R.string.Rs)+   order.totalAmount.toString()
        holder.binding.tvOrderOn.text = order.date
        holder.binding.tvPaymentStatus.text = order.paymentStatus
        holder.binding.tvAddressType.text = order.address.nickName
        holder.binding.tvDeliveryAddress.text =
            order.address.houseNo + order.address.landmark + order.address.city + order.address.pincode

    }
}