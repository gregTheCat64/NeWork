package ru.javacat.nework.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentNewEventBinding
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.model.UsersType
import ru.javacat.nework.ui.adapter.OnUserListener
import ru.javacat.nework.ui.adapter.UsersAdapter
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.util.AndroidUtils

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val eventViewModel: EventViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.show()
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewEventBinding.inflate(inflater, container, false)


        val speakerAdapter = UsersAdapter(object : OnUserListener {
            override fun onTouch(user: User) {
                super.onTouch(user)
            }
        })

        val participantAdapter = UsersAdapter(object : OnUserListener {
            override fun onTouch(user: User) {
                super.onTouch(user)
            }
        })


        //listeners
        binding.addSpeakerBtn.setOnClickListener {
            setFragmentResultListener("IDS") { _, bundle ->
                val result = bundle.getLongArray("IDS")
                println("result: ${result.contentToString()}")
                if (result != null) {
                    eventViewModel.setSpeakers(result.toList())
                }
            }
            findNavController().navigate(R.id.usersAddingFragment)
        }


        binding.clearPicBtn.setOnClickListener {
            eventViewModel.clearEdit()
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }



        binding.cancelBtn.setOnClickListener {
            eventViewModel.clearEdit()
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }

        binding.onLineBtn.setOnClickListener {
            binding.linkEditText.isVisible = true
            binding.locationGroup.isVisible = false
        }

        binding.offLineBtn.setOnClickListener {
            binding.linkEditText.isVisible = false
            binding.locationGroup.isVisible = true
        }

        binding.clearPicBtn.setOnClickListener {
            //TODO: добавить тип аттача и удалить
            eventViewModel.deleteAttachment()
        }

        binding.saveEventBtn.setOnClickListener {
            //TODO: доделать сейв
        }

        //lists:
        val speakersList = binding.speakersRecView
        speakersList.adapter = speakerAdapter

        userViewModel.speakers.observe(viewLifecycleOwner){
            speakerAdapter.submitList(it)
        }

        return binding.root
    }


}