package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentSignInBinding
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.ui.viewmodels.SignInViewModel

class SignInFragment: Fragment() {

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
        fun newInstance() = SignInFragment()
    }

    private val viewModel: SignInViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.loginBtn.setOnClickListener {
            if (binding.loginEditText.text.toString().isNotEmpty()
                && binding.passwordEditText.text.toString().isNotEmpty() ){
                val login = binding.loginEditText.text.toString().trim()
                val pass = binding.passwordEditText.text.toString().trim()
                AndroidUtils.hideKeyboard(requireView())
                try {
                    viewModel.updateUser(login,pass)
                } catch (e: NetworkError) {
                    //TODO доработать момент
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .show()
                }

            } else Snackbar.make(binding.root, "Заполните все поля", Snackbar.LENGTH_LONG).show()
        }

        binding.toRegistrationBtn.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_registrationFragment)
        }
        viewModel.tokenReceived.observe(viewLifecycleOwner) {
            if (it == 0){
                Snackbar.make(binding.root, "успешный вход!", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, "Неверный пароль или логин", Snackbar.LENGTH_LONG).show()
            }
        }
        return binding.root
    }
}