package naya.ganj.app.data.sidemenu.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import naya.ganj.app.R
import naya.ganj.app.data.sidemenu.adapter.VirtualRecyclerviewAdapter.MyViewHolder
import naya.ganj.app.data.sidemenu.model.VirtualOrderModel
import naya.ganj.app.databinding.AdapterVirtualAdapterLayoutBinding
import naya.ganj.app.interfaces.OnitemClickListener

class VirtualRecyclerviewAdapter(
    val context: Context,
    val virtualOrdersList: List<VirtualOrderModel.VirtualOrders>,
    val listener: OnitemClickListener
) : RecyclerView.Adapter<MyViewHolder>() {

    class MyViewHolder(val binding: AdapterVirtualAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            AdapterVirtualAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val vOrderModel = virtualOrdersList.get(position)

        if (vOrderModel.fileName.contains(".mp3")) {
            Picasso.get().load(R.drawable.audio_icon).into(holder.binding.imagview8)
        } else {
            Picasso.get().load(vOrderModel.fileName).into(holder.binding.imagview8)
        }

        holder.binding.imagview8.setOnClickListener {
            listener.onclick(position, vOrderModel.fileName)
        }

        holder.binding.tvOrderId.text = vOrderModel.virtualOrderId
        holder.binding.tvPlacedOn.text = vOrderModel.date
        val orderStatus = vOrderModel.status.split("|")

        try {
            if (orderStatus[0] == "Pending") {
                holder.binding.tvStatus.text = orderStatus[0]
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.binding.tvStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))

            } else if (orderStatus[0].equals("Failed") || orderStatus[0].equals("Cancelled")) {
                holder.binding.tvStatus.text = orderStatus[0]
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.binding.tvStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))
            } else if (orderStatus[0].equals("Delivered")) {
                holder.binding.tvStatus.text = orderStatus[0]
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.binding.tvStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))

            } else {
                holder.binding.tvStatus.text = orderStatus[0]
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.binding.tvStatus.setBackgroundColor(Color.parseColor(orderStatus[1]))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return virtualOrdersList.size
    }
}