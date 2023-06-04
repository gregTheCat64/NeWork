package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentJobsBinding
import ru.javacat.nework.ui.adapter.JobsAdapter
import ru.javacat.nework.ui.viewmodels.JobsViewModel

@AndroidEntryPoint
class JobsFragment: Fragment() {
    private val viewModel: JobsViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility =
            View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.GONE
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentJobsBinding.inflate(inflater)

        val args = arguments
        val authorId = args?.getLong("userID", 0L) ?:0L

        viewModel.getJobsByUserId(authorId)

        val adapter = JobsAdapter()

        binding.jobsList.adapter = adapter

        viewModel.userJobs.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

        binding.jobsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.jobsToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.create -> {
                    findNavController().navigate(R.id.newJobFragment)
                true
                }

                else -> {false}
            }
        }


        return binding.root
    }
}