package naya.ganj.app.data.sidemenu.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.R
import naya.ganj.app.data.sidemenu.model.MyOrderListModel
import naya.ganj.app.data.sidemenu.view.MyOrdersDetailsActivity
import naya.ganj.app.databinding.AdapterMyorderlistBinding
import naya.ganj.app.utility.Constant

class MyOrderListAdapter(val context: Context, val orederList: List<MyOrderListModel.Orders>) :
    RecyclerView.Adapter<MyOrderListAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: AdapterMyorderlistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int

    ): MyViewHolder {
        return MyViewHolder(
            AdapterMyorderlistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return orederList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val order: MyOrderListModel.Orders = orederList.get(position)

        holder.binding.tvTime.text = order.date
        holder.binding.tvItems.text = order.totalItems.toString() + " Items"
        holder.binding.tvOrderId.text = order.orderId
        holder.binding.tvAmount.text = order.totalAmount.toString()

        val orderStatus = order.orderStatus.split("|")

        try {
            if (orderStatus[0] == "Pending") {
                holder.binding.tvOrderStatus.text = orderStatus[0]
                holder.binding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
                holder.binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))

            } else if (orderStatus[0].equals("Failed") || orderStatus[0].equals("Cancelled")) {
                holder.binding.tvOrderStatus.text = orderStatus[0]
                holder.binding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
                holder.binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))
            } else if (orderStatus[0].equals("Delivered")) {
                holder.binding.tvOrderStatus.text = orderStatus[0]
                holder.binding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
                holder.binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))

            } else {
                holder.binding.tvOrderStatus.text = orderStatus[0]
                holder.binding.tvOrderStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.binding.tvOrderStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MyOrdersDetailsActivity::class.java)
            intent.putExtra(Constant.orderId, order.orderId)
            intent.putExtra(Constant.orderStatus, order.orderStatus)
            intent.putExtra(Constant.totalItems, order.totalItems.toString())
            context.startActivity(intent)

        }


    }


}