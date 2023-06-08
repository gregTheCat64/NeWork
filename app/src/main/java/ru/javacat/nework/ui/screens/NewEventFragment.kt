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
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentNewEventBinding
import ru.javacat.nework.databinding.FragmentNewPostBinding
import ru.javacat.nework.domain.model.AttachModel
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.EventType
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.model.UsersType
import ru.javacat.nework.domain.model.toAttachModel
import ru.javacat.nework.ui.adapter.OnUserListener
import ru.javacat.nework.ui.adapter.UsersAdapter
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.util.asOnlyDate
import ru.javacat.nework.util.asOnlyTime
import ru.javacat.nework.util.load
import ru.javacat.nework.util.snack
import ru.javacat.nework.util.toFile
import ru.javacat.nework.util.toast

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val eventViewModel: EventViewModel by activityViewModels()
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
//    }

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

        binding.buttonPanel.apply {
            linkBtn.isVisible = false
            addLocationBtn.isVisible = false
            addUsersBtn.isVisible = false
        }

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
                        eventViewModel.setNewAttach(uri, choosenType)
                        val attach = eventViewModel.attachFile
                        attach.value?.let { it1 -> refreshAttach(it1, binding) }
                    }
                }
            }

        //media:
        val pickFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        val file = it.data?.data?.toFile(requireContext())
                        eventViewModel.setNewAttach(file?.toUri(), choosenType)
                        val attach = eventViewModel.attachFile
                        attach.value?.let { it1 -> refreshAttach(it1, binding) }
                    }
                }
            }

        //listeners
        binding.buttonPanel.takePhoto.setOnClickListener {
            //TODO вынести функцию
            //eventViewModel.changeContent(binding.eventEditText.text?.trim().toString())
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
            // eventViewModel.changeContent(binding.eventEditText.text?.trim().toString())
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
            // eventViewModel.changeContent(binding.eventEditText.text?.trim().toString())
            val intent = Intent()
                .setType("audio/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            choosenType = AttachmentType.AUDIO
            pickFileLauncher.launch(intent)
        }

        binding.buttonPanel.videoBtn.setOnClickListener {
            // eventViewModel.changeContent(binding.eventEditText.text?.trim().toString())
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            choosenType = AttachmentType.VIDEO
            pickFileLauncher.launch(intent)
        }

        binding.buttonPanel.moreIconsBtn.setOnClickListener {
            it.visibility = View.GONE
            binding.buttonPanel.videoBtn.isVisible = true
            binding.buttonPanel.takePhoto.isVisible = true
        }

        binding.addSpeakerBtn.setOnClickListener {
            setFragmentResultListener("IDS") { _, bundle ->
                val result = bundle.getLongArray("IDS")
                println("result: ${result.contentToString()}")
                if (result != null) {
                    eventViewModel.setSpeakers(result.toList())
                    val attach = eventViewModel.attachFile
                    attach.value?.let { it1 -> refreshAttach(it1, binding) }
                }
            }
            findNavController().navigate(R.id.usersAddingFragment)
        }

        binding.dateEditText.setOnClickListener {
            showCalendar(parentFragmentManager, binding.dateEditText)
        }

        binding.timeEditText.setOnClickListener {
            showTimePicker(parentFragmentManager, binding.timeEditText)
        }


        binding.onLineBtn.setOnClickListener {
            binding.linkEditText.isVisible = true
            binding.locationGroup.isVisible = false
            eventViewModel.setType(EventType.ONLINE)
        }

        binding.offLineBtn.setOnClickListener {
            binding.linkEditText.isVisible = false
            binding.locationGroup.isVisible = true
            eventViewModel.setType(EventType.OFFLINE)
        }

        binding.clearPicBtn.setOnClickListener {
            choosenType = null
            eventViewModel.deleteAttachment()
        }

        binding.clearUsersAdded.setOnClickListener {
            eventViewModel.setSpeakers(emptyList())
            userViewModel.clearUsersChecked()
            binding.clearUsersAdded.visibility = View.GONE
        }

        binding.placeBtn.setOnClickListener {
            findNavController().navigate(R.id.mapsFragment)
        }

        //Навигация
        binding.topAppBar.setNavigationOnClickListener {
            eventViewModel.clearEdit()
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create -> {
                    val content = binding.eventEditText.text.toString()
                    val startDate = binding.dateEditText.text.toString()
                    val startTime = binding.timeEditText.text.toString()
                    if (content.isEmpty() || startDate.isEmpty() || startTime.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Заполните обязательные поля",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        eventViewModel.changeContent(content.trim())
                        val start = "$startDate $startTime:00"
                        eventViewModel.setStartDateTime(start)

                        val link = binding.linkEditText.text.toString()
                        if (link.isNotEmpty()) {
                            eventViewModel.setLink(link.trim())
                        }
                        eventViewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }

        //кнопка НАЗАД
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            val id = eventViewModel.edited.value?.id
            if (id != 0L){
                eventViewModel.clearEdit()
            }else {
                val content = binding.eventEditText.text.toString()
                val link = binding.linkEditText.text.toString()
                val startDate = binding.dateEditText.text.toString()
                val startTime = binding.timeEditText.text.toString()

                if (startDate.isNotEmpty() && startTime.isNotEmpty()){
                    val start = "$startDate $startTime:00"
                    eventViewModel.setStartDateTime(start)
                }

                if (link.isNotEmpty()){
                    eventViewModel.setLink(link.trim())
                }
                if (content.isNotEmpty()) {
                    eventViewModel.changeContent(content)
                }
                toast("Черновик сохранён")

            }
            findNavController().navigateUp()

        }

        //lists:
        val speakersList = binding.speakersRecView
        speakersList.adapter = speakerAdapter

        eventViewModel.edited.observe(viewLifecycleOwner) { event ->
            userViewModel.getUsersById(event.speakerIds)
            val attach = eventViewModel.attachFile

           initUI(event, attach.value, binding)

        }

        eventViewModel.state.observe(viewLifecycleOwner) {state->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        eventViewModel.refresh()
                    }
                    .show()
            }
        }
        eventViewModel.postCreated.observe(viewLifecycleOwner) {
            eventViewModel.refresh()
            findNavController().navigateUp()
        }

        userViewModel.addedUsers.observe(viewLifecycleOwner) {
            speakerAdapter.submitList(it)
            if (it.isNotEmpty()){
                binding.clearUsersAdded.isVisible = true
            }
        }

        eventViewModel.point.observe(viewLifecycleOwner) {
            if (it != null) {
                val locationText = "${it[0]}, ${it[1]}"
                binding.locationEditText.setText(locationText)
            }
        }

        return binding.root
    }

    private fun initUI(event: EventModel, attach: AttachModel?, binding: FragmentNewEventBinding) {
        if (event.content.isNotEmpty() && binding.eventEditText.text.toString().isEmpty()) {
            binding.eventEditText.setText(event.content.trim())
        }

        binding.onLineBtn.isChecked =
            event.type == EventType.ONLINE

        if (event.type == EventType.ONLINE){
            binding.linkEditText.isVisible = true
            binding.locationGroup.isVisible = false
        } else {
            binding.linkEditText.isVisible = false
            binding.locationGroup.isVisible = true
        }


        if (event.datetime != null
            && binding.dateEditText.text.toString().isEmpty()
            && binding.timeEditText.text.toString().isEmpty()){
            binding.dateEditText.setText(event.datetime.asOnlyDate())
            binding.timeEditText.setText(event.datetime.asOnlyTime())
        }

        if (event.link != null && binding.linkEditText.text.toString().isEmpty()) {
            binding.linkEditText.setText(event.link)
        }

        if (event.coords != null && binding.locationEditText.text.toString().isEmpty()){
            //toast(event.coords.toString())
            binding.locationEditText.setText(event.coords.toString())
        }

        if (event.attachment != null) {
            refreshAttach(event.attachment.toAttachModel(), binding)
        } else refreshAttach(attach, binding)
    }

    private fun refreshAttach(attach: AttachModel?, binding: FragmentNewEventBinding) {
        if (attach == null) {
            binding.attachmentContainer.visibility = View.GONE
            return
        } else {
            binding.attachmentContainer.visibility = View.VISIBLE
            when (attach.type) {
                AttachmentType.IMAGE -> {
                    binding.photo.visibility = View.VISIBLE
                    binding.audioContainer.root.visibility = View.GONE
                    binding.videoContainer.root.visibility = View.GONE
                    binding.photo.load(attach.uri.toString())
                }

                AttachmentType.AUDIO -> {
                    binding.audioContainer.root.visibility = View.VISIBLE
                    binding.photo.visibility = View.GONE
                    binding.videoContainer.root.visibility = View.GONE
                    binding.audioContainer.audioName.text = attach.uri?.toString()
                }

                AttachmentType.VIDEO -> {
                    binding.videoContainer.root.visibility = View.VISIBLE
                    binding.photo.visibility = View.GONE
                    binding.audioContainer.root.visibility = View.GONE
                    binding.videoContainer.videoName.text = attach.uri?.toString()
                }

                else -> {
                    binding.attachmentContainer.visibility = View.GONE
                }
            }
        }
    }


}