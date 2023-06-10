package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentUsersSearchBinding
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.ui.adapter.OnUserListener
import ru.javacat.nework.ui.adapter.UsersAdapter
import ru.javacat.nework.ui.viewmodels.UserViewModel

@AndroidEntryPoint
class UsersSearchFragment: Fragment() {
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersSearchBinding.inflate(inflater)

        val adapter = UsersAdapter(object : OnUserListener{
            override fun onTouch(user: User) {
                val bundle = Bundle()
                bundle.putLong("userID", user.id)
                findNavController().navigate(R.id.wallFragment, bundle)
            }
        })

        binding.usersList.adapter = adapter

        var userList: List<User> = listOf()

        userViewModel.users.observe(viewLifecycleOwner){ users ->
            userList = users.sortedBy { !it.favoured }
            adapter.submitList(userList)
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        binding.filterButton.setOnClickListener {
            if (binding.userFilter.text.isNotEmpty()) {
                val findUserText = binding.userFilter.text.toString().trim()
                val filteredList = userList.filter {
                    it.name.contains(findUserText, true)
                }
                adapter.submitList(filteredList)
            } else adapter.submitList(userList)
        }

        binding.clearTextBtn.setOnClickListener {
            binding.userFilter.text.clear()
            adapter.submitList(userList)
        }

        return binding.root
    }
}