package com.example.guide

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.guide.databinding.ItemPostPlacesBinding

class PlacesAdapter(val listener: Listener): RecyclerView.Adapter<PlacesAdapter.PlaceHolder>() {
    var placeList: MutableList<Place> = emptyList<Place>().toMutableList()

    class PlaceHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding = ItemPostPlacesBinding.bind(itemView)

        fun bind(place: Place, listener: Listener) = with(binding){
            item_place_title.text = place.name
            item_place_desc.text= place.address
            itemView.setOnClickListener(){
                listener.onClick(place)
            }
        }

        val item_place_title: TextView = itemView.findViewById(R.id.item_place_title)
        val item_place_desc: TextView = itemView.findViewById(R.id.item_place_desc)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        return PlaceHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post_places, parent, false))
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.bind(placeList[position], listener)
        /*holder.item_place_title.text = currentItem.name
        holder.item_place_desc.text = currentItem.address
        holder.itemView.isSelected = position == selectedItemPosition

         */
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(place: MutableList<Place>){
        this.placeList = place
        notifyDataSetChanged()
    }


    interface Listener{
        fun onClick(place: Place)
    }
}