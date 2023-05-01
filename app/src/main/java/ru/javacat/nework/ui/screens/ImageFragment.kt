package ru.javacat.nework.ui.screens

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentImageBinding
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadCircleCrop

class ImageFragment: DialogFragment(R.layout.fragment_image) {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentImageBinding.inflate(layoutInflater)
        val image = binding.image
        val url = arguments?.getString("PIC")

        if (url!= null )
        {
            Log.i("URL_PIC", url.toString())
            image.load(url.toString())
        }

        val listener = DialogInterface.OnClickListener{ _, which ->
            when (which){
                DialogInterface.BUTTON_NEGATIVE -> this.dismiss()
            }
        }

        val builder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Закрыть", listener)
            .create()
        return builder
    }
}