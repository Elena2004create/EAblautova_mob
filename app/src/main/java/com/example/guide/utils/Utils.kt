package com.example.guide.utils

import android.content.Context
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.example.guide.R
import com.yandex.mapkit.SpannableString
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.map.VisibleRegion

fun VisibleRegion.toBoundingBox() = BoundingBox(bottomLeft, topRight)

fun SpannableString.toSpannable(@ColorInt color: Int): Spannable {
    val spannableString = android.text.SpannableString(text)
    spans.forEach {
        spannableString.setSpan(
            ForegroundColorSpan(color),
            it.begin,
            it.end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannableString
}

fun <T> List<T>.takeIfNotEmpty(): List<T>? = takeIf { it.isNotEmpty() }

typealias CommonColors = R.color
typealias CommonDrawables = R.drawable
typealias CommonId = R.id

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun <T : View, V> T.goneOrRun(value: V?, block: T.(V) -> Unit) {
    this.isVisible = value != null
    if (value != null) {
        this.block(value)
    }
}