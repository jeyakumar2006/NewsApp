package com.example.mynewsapp.Utils

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.example.mynewsapp.R

object AlertDialogHelper {

    private var dialog: AlertDialog? = null

    fun ShowNetworkerrorDialog(context: Context) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.custom_networkerror_dialog, null)
        val builder = AlertDialog.Builder(context, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
        dialog = builder.create()

        val settingsbtn: ImageView = dialogView.findViewById(R.id.settingsBtn)

        settingsbtn.setOnClickListener {
            DismisserrorDialog()
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        ShowerrorDialog()
    }

    fun ShowerrorDialog() {
        Log.e(TAG, "ShowerrorDialog::::1111:::++++::::: ")
        dialog?.show()
    }

    fun DismisserrorDialog() {
        dialog?.dismiss()
        dialog = null
    }



}