package ru.javacat.nework.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        
        val binding = FragmentEventsBinding.inflate(inflater, container, false)

        binding.postListBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_events_to_navigation_posts)
        }

        binding.eventsListBtn.setOnClickListener{
            // TODO: обновить список 
        }

        binding.newEventBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_events_to_newEventFragment)
        }
       
       
        return binding.root



        
    }

  
}