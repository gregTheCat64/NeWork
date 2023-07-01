package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentJobsBinding
import ru.javacat.nework.ui.adapter.JobsAdapter
import ru.javacat.nework.ui.viewmodels.JobsViewModel
import javax.inject.Inject

@AndroidEntryPoint
class JobsFragment: Fragment() {
    private val viewModel: JobsViewModel by viewModels()


    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentJobsBinding.inflate(inflater)

        val args = arguments
        val authorId = args?.getLong("userID", 0L) ?:0L
        val myId = appAuth.authStateFlow.value.id

        viewModel.getJobsByUserId(authorId)

        val adapter = JobsAdapter()

        binding.jobsList.adapter = adapter

        viewModel.userJobs.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

        binding.jobsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        binding.jobsToolbar.menu.findItem(R.id.create).isVisible = myId == authorId

        viewModel.state.observe(viewLifecycleOwner){state->
            println(state)
            with(binding) {
                progress.root.isVisible = state.loading
            }
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.getJobsByUserId(authorId)
                    }
                    .show()
            }
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