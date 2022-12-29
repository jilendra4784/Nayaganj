package naya.ganj.app.data.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.home.model.HomePageModel
import naya.ganj.app.databinding.BrandAdapterBinding
import naya.ganj.app.utility.ImageManager


class BrandAdapter(
    val context: Context,
    val brandList: List<HomePageModel.Data.Brand>,
    val app: Nayaganj
) :
    RecyclerView.Adapter<BrandAdapter.MyViewHolder>() {


    class MyViewHolder(val binding: BrandAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = BrandAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try{
            if(brandList[position].imgUrl.isNotEmpty()){
                ImageManager.onLoadingImage(app,context, brandList[position].imgUrl,holder.binding.ivCatImage)
            }else{
                holder.binding.ivCatImage.setBackgroundResource(R.drawable.default_image)
            }

        }catch (e:Exception){e.printStackTrace()}
    }

    override fun getItemCount(): Int {
        return brandList.size
    }

}