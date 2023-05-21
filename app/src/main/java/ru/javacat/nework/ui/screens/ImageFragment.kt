package ru.javacat.nework.ui.screens

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.GnssAntennaInfo.Listener
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentImageBinding
import ru.javacat.nework.util.DownloadAndSaveImageTask
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadCircleCrop
import ru.javacat.nework.util.loadFull
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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
                DialogInterface.BUTTON_NEUTRAL ->
                    DownloadAndSaveImageTask(requireContext()).execute(url.toString())
            }
        }

        val builder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Закрыть", listener)
            .setNeutralButton("Скачать", listener)
            .setIcon(R.drawable.ic_baseline_account_circle_24)
            .create()
        return builder
    }
    private fun saveImage(path: String) {
        var `in`: InputStream? = null
        var bmp: Bitmap? = null
        //val iv = findViewById<View>(R.id.imagFullImage) as ImageView
        var responseCode = -1
        try {
            val url = URL(path) //"http://192.xx.xx.xx/mypath/img1.jpg
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.setDoInput(true)
            con.connect()
            responseCode = con.getResponseCode()
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //download
                `in` = con.getInputStream()
                bmp = BitmapFactory.decodeStream(`in`)
                `in`.close()
                //iv.setImageBitmap(bmp)
            }
        } catch (ex: Exception) {
            Log.e("Exception", ex.toString())
        }
}
}
