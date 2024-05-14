package com.example.guide

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.ListFragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.guide.Note
import com.example.guide.R
import com.example.guide.NotesFragmentDirections

class NotesAdapter: RecyclerView.Adapter<NotesAdapter.NoteHolder>() {
    private var noteList = emptyList<Note>()
    class NoteHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val item_notes_title: TextView = itemView.findViewById(R.id.item_notes_title)
        val item_notes_desc: TextView = itemView.findViewById(R.id.item_notes_desc)
        val rowLayout: CardView = itemView.findViewById(R.id.rowLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        return NoteHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post_notes, parent, false))
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        val currentItem = noteList[position]
        holder.item_notes_title.text = currentItem.title
        holder.item_notes_desc.text = currentItem.text
        holder.rowLayout.setOnClickListener(){
            val action = NotesFragmentDirections.actionNotesFragmentToUpdateNoteFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(note: List<Note>){
        this.noteList = note
        notifyDataSetChanged()
    }
}