package com.example.guide.articles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guide.R
import com.example.guide.data.Article
import com.example.guide.databinding.ItemArticlesBinding

class ArticleAdapter(private val context: Context, private val articles: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    private val backgrounds = listOf(
        R.color.rv_1_color,
        R.color.rv_2_color,
        R.color.rv_3_color,
        R.color.rv_4_color,
        R.color.rv_5_color,
        R.color.rv_6_color,
        R.color.rv_7_color
    )

    inner class ArticleViewHolder(val binding: ItemArticlesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.articleTitle.text = article.title
            binding.articleDescription.text = article.description

            val backgroundIndex = position % backgrounds.size
            binding.container.setBackgroundResource(backgrounds[backgroundIndex])

            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemArticlesBinding.inflate(inflater, parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size
}