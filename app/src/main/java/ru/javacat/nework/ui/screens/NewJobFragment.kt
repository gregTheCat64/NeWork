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
import ru.javacat.nework.databinding.FragmentNewJobBinding
import ru.javacat.nework.domain.model.JobModel
import ru.javacat.nework.ui.viewmodels.JobsViewModel
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.util.snack
import ru.javacat.nework.util.toLocalDateTimeWhithoutZone

@AndroidEntryPoint
class NewJobFragment : Fragment() {

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

    private val viewModel: JobsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewJobBinding.inflate(inflater)

        //AppBar:
        binding.topAppBar.setNavigationOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create -> {
                    if (binding.jobEditText.text.isNotEmpty() &&
                        binding.positionEditText.text.isNotEmpty() &&
                        binding.startJobEditText.text.isNotEmpty() &&
                        binding.linkEditText.text.isNotEmpty()
                    ) {
                        val job = binding.jobEditText.text.toString().trim()
                        val position = binding.positionEditText.text.toString().trim()
                        val start = binding.startJobEditText.text.toString().trim() + " 08:00:00"
                        var end: String? = binding.endJobEditText.text.toString().trim()
                        end = if (binding.endJobEditText.text.isNotEmpty()) {
                            "$end 18:00:00"
                        } else null

                        val link = binding.linkEditText.text.toString().trim()

                        viewModel.save(
                            JobModel(
                                0L, 0L, true, job, position, start.toLocalDateTimeWhithoutZone(),
                                end?.toLocalDateTimeWhithoutZone(), link
                            )
                        )




                    } else Snackbar.make(
                        binding.root,
                        "Заполните обязательные поля",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            AndroidUtils.hideKeyboard(requireView())
            true
        }

        //dates:
        binding.startDateBtn.setOnClickListener {
            showCalendar(parentFragmentManager, binding.startJobEditText)
        }

        binding.endDateBtn.setOnClickListener {
            showCalendar(parentFragmentManager, binding.endJobEditText)
        }


        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.root.isVisible = state.loading
            if (state.error) {
                snack("Ошибка сети")
            }
        }

        viewModel.jobCreated.observe(viewLifecycleOwner){
            Snackbar.make(binding.root, "Успешно", Snackbar.LENGTH_LONG)
                .show()
            findNavController().navigateUp()
        }

        return binding.root
    }

}

