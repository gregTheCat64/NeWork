package ru.javacat.nework.ui.screens

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentUsersBinding
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.ui.adapter.OnUserListener
import ru.javacat.nework.ui.adapter.UsersAdapter
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.ui.viewmodels.UsersListDialogViewModel

@AndroidEntryPoint
class UserListDialogFragment: DialogFragment() {

    private val viewModel: UsersListDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = FragmentUsersBinding.inflate(layoutInflater)
        val args = arguments
        val ids = args?.getLongArray("IDS") ?: throw NullPointerException("no args")

        viewModel.getUsersById(ids.toList())

        val list = binding.userList

        val adapter = UsersAdapter(object: OnUserListener{
            override fun onTouch(user: User) {
                //val action = PostsFragmentDirections.actionNavigationPostsToWallFragment(user.id)
                val bundle = Bundle()
                bundle.putLong("userID", user.id)
                findNavController().navigate(R.id.wallFragment, bundle)
            }
        } )
        list.adapter = adapter

        viewModel.users.observe(this){
            adapter.submitList(it)
        }

        val listener = DialogInterface.OnClickListener{ _, which ->
            when (which){
                DialogInterface.BUTTON_NEGATIVE -> this.dismiss()
            }
        }

        val builder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle("Отмечены:")
            .setPositiveButton("Закрыть", listener)
            .create()
        return builder
    }
}