package ru.javacat.nework.util

import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import ru.javacat.nework.R

fun showSignInDialog(fragment: Fragment) {
    val listener = DialogInterface.OnClickListener { _, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> NavHostFragment.findNavController(fragment)
                .navigate(R.id.signInFragment)
            DialogInterface.BUTTON_NEGATIVE -> Toast.makeText(
                fragment.requireContext(),
                "Не забудьте авторизоваться",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val dialog = AlertDialog.Builder(fragment.requireContext())
        .setCancelable(false)
        .setTitle("Вы не авторизованы!")
        .setMessage("Пожалуйста, авторизуйтесь")
        .setPositiveButton("Хорошо", listener)
        .setNegativeButton("Позже", listener)
        .create()

    dialog.show()
}