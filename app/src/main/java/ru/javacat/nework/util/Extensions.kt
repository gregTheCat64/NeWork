package ru.javacat.nework.util

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.snack(msg: String){
    Snackbar.make(this.requireView(), msg, Snackbar.LENGTH_SHORT).show()
}

fun Fragment.toast(msg: String){
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_SHORT).show()
}