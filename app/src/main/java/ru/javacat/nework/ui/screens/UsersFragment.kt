package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentUsersBinding

@AndroidEntryPoint
class UsersFragment: Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentUsersBinding.inflate(inflater, container, false)




        return binding.root
    }
}