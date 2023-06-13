package ru.javacat.nework.ui.screens

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentImageBinding
import ru.javacat.nework.util.loadFull

class ImageFragment: DialogFragment(R.layout.fragment_image) {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentImageBinding.inflate(layoutInflater)
        val image = binding.image
        val url = arguments?.getString("PIC")

        if (url!= null )
        {
            Log.i("URL_PIC", url.toString())
            image.loadFull(url.toString())
        }

        val listener = DialogInterface.OnClickListener{ _, which ->
            when (which){
                DialogInterface.BUTTON_NEGATIVE -> this.dismiss()

            }
        }

        val builder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Закрыть", listener)
            //.setNeutralButton("Скачать", listener)
            .setIcon(R.drawable.ic_baseline_account_circle_24)
            .create()
        return builder
    }

}
