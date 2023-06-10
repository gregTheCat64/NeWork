package ru.javacat.nework.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentUsersAddingBinding
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.model.UsersType
import ru.javacat.nework.ui.adapter.OnUserListener
import ru.javacat.nework.ui.adapter.UsersAdapter
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel

@AndroidEntryPoint
class UsersAddingFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentUsersAddingBinding.inflate(inflater, container, false)

        //val args : UsersAddingFragmentArgs by navArgs()
        val usersIds = mutableListOf<Long>()
        val sb = StringBuffer()


        //временное решение, сбрасываем чеки у всех юзеров
        //TODO: доработать чеки у юзеров которые были добавлены
        userViewModel.clearUsersChecked()

        val adapter = UsersAdapter(object : OnUserListener {
            override fun onTouch(user: User) {
                //sb.append("@${user.name} ")
                //binding.usersTextView.text = sb
                if (usersIds.contains(user.id)){
                    usersIds.remove(user.id)
                } else {usersIds.add(user.id)}

            }
        })
        binding.usersList.adapter = adapter

        var userList: List<User> = listOf()
        userViewModel.users.observe(viewLifecycleOwner){
            userList = it.sortedBy { !it.favoured }
            adapter.submitList(userList)
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


        //navigation:
        binding.topAppBar.setNavigationOnClickListener {
              usersIds.clear()
            findNavController().navigateUp()
        }

        binding.topAppBar.setOnMenuItemClickListener {menuItem->
            when (menuItem.itemId){
                R.id.create -> {
                    val users = usersIds.toLongArray()
                    setFragmentResult("IDS", bundleOf("IDS" to users))

                    findNavController().navigateUp()
                }

                else -> {false}
            }
        }


        return binding.root
    }
}