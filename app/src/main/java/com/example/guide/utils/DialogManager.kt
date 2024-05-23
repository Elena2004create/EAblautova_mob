package com.example.guide.utils

import android.app.AlertDialog
import android.content.Context

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Запрос на использование геолокации")
        dialog.setMessage("Разрешить определять местоположение?")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Да"){ _,_ ->
            listener.onClick()
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Нет"){ _,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }
    interface Listener{
        fun onClick()
    }
}