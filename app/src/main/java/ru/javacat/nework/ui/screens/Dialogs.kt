package ru.javacat.nework.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
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