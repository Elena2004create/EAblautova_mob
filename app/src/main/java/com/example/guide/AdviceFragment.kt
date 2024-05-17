package com.example.guide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.guide.databinding.FragmentAdviceBinding


class AdviceFragment : Fragment() {

    private lateinit var binding: FragmentAdviceBinding

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

        binding.apply {
            advice1.setOnClickListener(){
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mircamping.ru/stati/kak-pravilno-sobrat-aptechku-turista"))
                startActivity(browserIntent)
            }
        }

    }

    }