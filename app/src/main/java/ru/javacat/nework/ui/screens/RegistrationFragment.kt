package ru.javacat.nework.ui.screens

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentRegistrationBinding
import ru.javacat.nework.ui.viewmodels.RegistrationViewModel
import ru.javacat.nework.util.loadCircleCrop
import ru.javacat.nework.util.snack


class RegistrationFragment : Fragment() {

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

    companion object {
        fun newInstance() = RegistrationFragment()
    }

    private  val viewModel: RegistrationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(inflater, container,false)

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
                        viewModel.changePhoto(uri, uri?.toFile())
                        //avatar = PhotoModel(uri, uri?.toFile())
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .compress(200)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg"
                    )
                )
                .createIntent {
                    pickPhotoLauncher.launch(it)
                }
        }

        binding.passwordEditText.addTextChangedListener(object : TextWatcher{
            val wrongSymbols = context?.getString(R.string.incorrect_symbol)
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank())
                    if (s.contains("[^A-Za-z0-9_]".toRegex()))
                        binding.passLayout.error = wrongSymbols
                else binding.passLayout.error = null
                else binding.passLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.registerUserBtn.setOnClickListener {
            val login = binding.loginEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val name = binding.nameEditText.text.toString().trim()
            val passCheck = binding.passwordCheckEditText.text.toString().trim()
            val errorString = context?.getString(R.string.Required_field)
            val passwordCheckErrorString = context?.getString(R.string.passwordCheckError)

            when {
                login.isEmpty() && password.isEmpty() && name.isEmpty() && passCheck.isEmpty() -> {
                    binding.loginLayout.error = errorString
                    binding.nameLayout.error = errorString
                    binding.passLayout.error = errorString
                    binding.passCheckLayout.error = errorString
                    binding.nameEditText.requestFocus()
                }
                name.isEmpty() ->{
                    binding.nameLayout.error = errorString
                }
                login.isEmpty() -> {
                    binding.loginLayout.error = errorString
                    binding.loginEditText.requestFocus()
                }
                password.isEmpty() ->{
                    binding.passLayout.error = errorString
                    binding.passwordEditText.requestFocus()
                }
                passCheck.isEmpty() -> {
                    binding.passLayout.error = errorString
                    binding.passwordCheckEditText.requestFocus()
                }
                password != passCheck -> {
                    snack(passwordCheckErrorString!!)
                    binding.passwordEditText.requestFocus()
                }
                else -> {
                    viewModel.registerUser(login,password,name)
                }
            }
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it?.uri != null){
                binding.avatarImage.isVisible = true
                binding.pickPhoto.isVisible = false
                binding.avatarImage.loadCircleCrop(it.uri.toString())
                //getAvatars(it.uri.toString(), binding)
            }
        }

        viewModel.tokenReceived.observe(viewLifecycleOwner) {
            if (it == 0){
                Snackbar.make(binding.root, "Добавлен пользователь", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, "Неверный пароль или логин", Snackbar.LENGTH_LONG).show()
            }
        }


        return binding.root
    }

}
