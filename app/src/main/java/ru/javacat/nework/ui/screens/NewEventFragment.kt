package ru.javacat.nework.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentNewEventBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.model.UsersType
import ru.javacat.nework.ui.adapter.OnUserListener
import ru.javacat.nework.ui.adapter.UsersAdapter
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.util.toFile

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
        var choosenType: AttachmentType? = null

        val speakerAdapter = UsersAdapter(object : OnUserListener {
            override fun onTouch(user: User) {
                super.onTouch(user)
            }
        })

        //pickers:
        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        eventViewModel.changeAttach(uri, choosenType)
                    }
                }
            }

        //media:
        val pickFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        val file = it.data?.data?.toFile(requireContext())
                        eventViewModel.changeAttach(file?.toUri(), choosenType)
                    }
                }
            }



        //listeners
        binding.buttonPanel.takePhoto.setOnClickListener {
            //TODO вынести функцию
            eventViewModel.changeContent(binding.eventEditText.text?.trim().toString())
            choosenType = AttachmentType.IMAGE
            ImagePicker.Builder(this)
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent {
                    pickPhotoLauncher.launch(it)
                }
        }

        binding.buttonPanel.pickPhoto.setOnClickListener {
            eventViewModel.setState(FeedModelState(loading = true))
            eventViewModel.changeContent(binding.eventEditText.text?.trim().toString())
            choosenType = AttachmentType.IMAGE
            ImagePicker.Builder(this)
                .galleryOnly()
                .crop()
                .compress(2000)
                .createIntent {
                    pickPhotoLauncher.launch(it)
                }
        }

        binding.buttonPanel.audio.setOnClickListener {
            eventViewModel.changeContent(binding.eventEditText.text?.trim().toString())
            val intent = Intent()
                .setType("audio/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            choosenType = AttachmentType.AUDIO
            pickFileLauncher.launch(intent)
        }

        binding.buttonPanel.videoBtn.setOnClickListener {
            eventViewModel.changeContent(binding.eventEditText.text?.trim().toString())
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            choosenType = AttachmentType.VIDEO
            pickFileLauncher.launch(intent)
        }

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
            eventViewModel.deleteAttachment()
            AndroidUtils.hideKeyboard(requireView())
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

        eventViewModel.edited.observe(viewLifecycleOwner){event->
            userViewModel.getUsersById(event.speakerIds)
        }

        userViewModel.addedUsers.observe(viewLifecycleOwner){
            speakerAdapter.submitList(it)
        }

        return binding.root
    }


}