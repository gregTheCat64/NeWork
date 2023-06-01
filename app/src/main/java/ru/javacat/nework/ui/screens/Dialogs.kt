package ru.javacat.nework.ui.screens

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.datepicker.MaterialCalendar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun showImageDialog(url:String,fm: FragmentManager) {
    val dialog = ImageFragment()
    val dialogBundle = Bundle()
    dialogBundle.putString("PIC", url)
    dialog.arguments = dialogBundle
    dialog.show(fm,"")
}

//fun showVideoDialog(url: String, fm: FragmentManager){
//    val dialog = VideoPlayerFragment()
//    val dialogBundle = Bundle()
//    dialogBundle.putString("URL", url)
//    dialog.arguments = dialogBundle
//    //dialog.show(fm, "")
//}

fun showUserListDialog(ids: List<Long>, fm: FragmentManager){
    val userListDialog = UserListDialogFragment()
    val bundle = Bundle()
    bundle.putLongArray("IDS", ids.toLongArray())
    userListDialog.arguments = bundle
    userListDialog.show(fm, "")
}

fun showCalendar(fm: FragmentManager, editText: EditText){
    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select Date")
        .build()
    picker.addOnPositiveButtonClickListener {
        val date = Date(it)
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
        val timeInMillis = dateFormatter.format(
            // it is the milliseconds received inside lambda of addOnPositiveButtonClickListener
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault()).toLocalDate()
        )
        editText.setText(timeInMillis)
    }
    picker.show(fm,"materialDatePicker")
}

fun showTimePicker(fm: FragmentManager,editText: EditText){
    val selectedHour: Int? = null
    val selectedMinute: Int? = null
    val hour = selectedHour ?: LocalDateTime.now().hour
    val minute = selectedMinute ?: LocalDateTime.now().minute

    MaterialTimePicker.Builder()
        .setTimeFormat(TimeFormat.CLOCK_24H)
        .setHour(hour)
        .setMinute(minute)
        .build()
        .apply {
            addOnPositiveButtonClickListener {
                val hourAsText = if (this.hour < 10) "0${this.hour}" else this.hour
                val minuteAsText = if (this.minute < 10) "0${this.minute}" else this.minute
                val time = "${hourAsText}:${minuteAsText}"
                editText.setText(time)
                 }
        }.show(fm, MaterialTimePicker::class.java.canonicalName)
}


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

fun showSignOutDialog(appAuth: AppAuth, context: Context) {
    val listener = DialogInterface.OnClickListener { _, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                appAuth.removeAuth()
            }
            DialogInterface.BUTTON_NEGATIVE -> Toast.makeText(
                context,
                "ну и ладненько...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val dialog = AlertDialog.Builder(context)
        .setCancelable(false)
        .setTitle("Внимание!")
        .setMessage("Вы точно хотите выйти?")
        .setPositiveButton("Уверен!", listener)
        .setNegativeButton("Нет", listener)
        .create()

    dialog.show()
}