package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.databinding.FragmentJobsBinding
import ru.javacat.nework.ui.adapter.JobsAdapter
import ru.javacat.nework.ui.viewmodels.JobsViewModel

@AndroidEntryPoint
class JobsFragment: Fragment() {
    private val viewModel: JobsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentJobsBinding.inflate(inflater)

        val args: JobsFragmentArgs by navArgs()
        val authorId = args.authorArg

        viewModel.getJobsByUserId(authorId)

        val adapter = JobsAdapter()

        binding.jobsList.adapter = adapter

        viewModel.userJobs.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }


        return binding.root
    }
}