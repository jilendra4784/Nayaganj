package naya.ganj.app.data.category.adapter

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naya.ganj.app.databinding.RecentAdapterLayoutBinding
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.roomdb.entity.AppDataBase
import naya.ganj.app.roomdb.entity.RecentSuggestion

class RecentListAdapter(val activity:Activity,
    val context: Context,
    val list: MutableList<RecentSuggestion>,
    val onItemClick: OnitemClickListener
) :
    RecyclerView.Adapter<RecentListAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: RecentAdapterLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            RecentAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.tvRecentTitle.text = list.get(position).query
        holder.binding.cardview.setOnClickListener {
            onItemClick.onclick(position, list.get(position).query)
        }
        holder.binding.icCloseIcon.setOnClickListener {
            Thread(Runnable {
                AppDataBase.getInstance(context).productDao().deleteSuggesiton(list[holder.absoluteAdapterPosition].query)
                activity.runOnUiThread(Runnable{
                    Log.e("TAG", "onBindViewHolder: list Size: "+list.size+", position"+holder.absoluteAdapterPosition )
                    Handler(Looper.getMainLooper()).postDelayed(Runnable{
                        list.removeAt(holder.absoluteAdapterPosition)
                        notifyItemRemoved(holder.absoluteAdapterPosition)
                    },200)

                })
            }).start()
        }
    }


}