package com.example.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.map.databinding.ItemBinding


class SAdapter (private val onClickListener: OnClickListener) : ListAdapter<Place, SAdapter.SVHolder>(SDUtil){
//Muestra los datos een el recyclerview
    companion object SDUtil : DiffUtil.ItemCallback<Place>(){
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.name == newItem.name
        }

    }

    inner class SVHolder(private val binding: ItemBinding):
        RecyclerView.ViewHolder(binding.root){

            fun bind(p: Place){
                binding.name.text = p.name
                binding.lat.text = p.lat.toString()
                binding.lng.text = p.lng.toString()
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SVHolder {
        return SVHolder(
            ItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SVHolder, position: Int) {
        val p = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(p)
        }
        holder.bind(p)
    }

    class OnClickListener(val clickListener: (p: Place) -> Unit) {
        fun onClick(place: Place) = clickListener(place)
    }

}