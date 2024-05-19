package com.example.guide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guide.databinding.FragmentAdviceBinding


class AdviceFragment : Fragment() {

    private lateinit var binding: FragmentAdviceBinding
    private val articles = listOf(
        Article("Как правильно собрать аптечку туриста", "Аптечка – один из обязательных предметов в походном рюкзаке", "https://mircamping.ru/stati/kak-pravilno-sobrat-aptechku-turista"),
        Article("Что взять с собой в поездку", "Список необходимых вещей для путешествия", "https://bolshayastrana.com/blog/chto-vzyat-s-soboj-v-poezdku-spisok-neobhodimyh-veshchej-321"),
        Article("Города Золотого кольца России", "Интересные места и факты о нескольких городах Золотого кольца", "https://www.kp.ru/russia/goroda-zolotogo-koltsa-rossii/"),
        Article("Гостиницы России", "Отели России, рейтинги отелей. Описания, фото и отзывы туристов ", "https://tonkosti.ru/%D0%93%D0%BE%D1%81%D1%82%D0%B8%D0%BD%D0%B8%D1%86%D1%8B_%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D0%B8?utm_referrer=https%3A%2F%2Fwww.google.com%2F"),
        Article("Как выбрать отель и не испортить отпуск?", "Правила выбора хорошего отеля и советы экспертов", "https://lenta.ru/articles/2023/08/07/hotel/"),
        Article("Здоровое питание в поездках", " Как организовать питание в дороге", "https://xn----8sbehgcimb3cfabqj3b.xn--p1ai/healthy-nutrition/articles/zdorovoe-pitanie-v-poezdkakh-7-osnovnykh-pravil/"),
        Article("Чек-лист хорошего ресторана", "Рассматриваются заведения, которые стоит посетить", "https://restoplace.cc/blog/chek-list-restorana")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAdviceBinding.inflate(layoutInflater)
        retainInstance = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArticleAdapter(requireContext(), articles)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        /*binding.apply {
            advice1.setOnClickListener(){
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mircamping.ru/stati/kak-pravilno-sobrat-aptechku-turista"))
                startActivity(browserIntent)
            }
        }*/

    }

    }