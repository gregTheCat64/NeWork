package ru.javacat.nework.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentNewEventBinding
import ru.javacat.nework.domain.model.UsersType
import ru.javacat.nework.ui.viewmodels.EventViewModel

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val eventViewModel: EventViewModel by activityViewModels()

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


        binding.addParticipatesBtn.setOnClickListener {
            val action = NewEventFragmentDirections.actionNewEventFragmentToUsersAddingFragment(UsersType.PARTICIPANT)
            findNavController().navigate(action)
        }

        binding.addSpeakerBtn.setOnClickListener {
            val action = NewEventFragmentDirections.actionNewEventFragmentToUsersAddingFragment(UsersType.SPEAKER)
            findNavController().navigate(action)
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.onLineBtn.setOnClickListener{
            binding.linkEditText.isVisible = true
            binding.locationGroup.isVisible = false
        }

        binding.offLineBtn.setOnClickListener {
            binding.linkEditText.isVisible = false
            binding.locationGroup.isVisible = true
        }


        eventViewModel.participateAdded.observe(viewLifecycleOwner){
            binding.apply {
                participantsEditText.text.clear()
                participantsEditText.text.append(it)
            }

        }

        eventViewModel.speakerAdded.observe(viewLifecycleOwner){
            binding.apply {
                speakersEditText.text.clear()
                speakersEditText.text.append(it)
            }
        }


        return binding.root
    }


}