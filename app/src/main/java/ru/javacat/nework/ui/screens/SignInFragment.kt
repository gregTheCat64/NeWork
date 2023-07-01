package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentSignInBinding
import ru.javacat.nework.ui.viewmodels.SignInViewModel
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.util.snack
import javax.inject.Inject

class SignInFragment : Fragment() {




    private val viewModel: SignInViewModel by activityViewModels()



    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)



        binding.loginBtn.setOnClickListener {
            val login = binding.loginEditText.text.toString().trim()
            val pass = binding.passwordEditText.text.toString().trim()
            val errorString = context?.getString(R.string.Required_field)

            when {
                login.isEmpty() && pass.isEmpty() -> {
                    binding.loginLayout.error = errorString
                    binding.passLayout.error = errorString
                    binding.loginEditText.requestFocus()
                }

                login.isEmpty() -> {
                    binding.loginLayout.error = errorString
                    binding.loginEditText.requestFocus()
                }

                pass.isEmpty() -> {
                    binding.passLayout.error = errorString
                    binding.passwordEditText.requestFocus()
                }

                else -> {
                    AndroidUtils.hideKeyboard(requireView())
                    viewModel.updateUser(login, pass)
                }

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

        binding.toRegistrationBtn.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_registrationFragment)
        }
        viewModel.tokenReceived.observe(viewLifecycleOwner) {
            Log.i("TOKEN", it.toString())
            if (it == 0) {
                Snackbar.make(binding.root, "С возвращением!", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) {
            snack(it)
        }
        return binding.root
    }
}