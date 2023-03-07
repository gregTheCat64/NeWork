package ru.javacat.nework.ui

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import ru.javacat.nework.R
import ru.javacat.nework.auth.AppAuth
import ru.javacat.nework.databinding.ActivityAppBinding
import ru.javacat.nework.viewmodels.AuthViewModel

class AppActivity : AppCompatActivity() {

    //lateinit var appAuth: AppAuth
    //private lateinit var navView: BottomNavigationView

       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           val binding = ActivityAppBinding.inflate(layoutInflater)
           setContentView(binding.root)

           //***Так настраивается bottomNavigationBar
//           navView = binding.navView
//
           // val navController = findNavController(R.id.nav_host_fragment)
//
//        val appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.navigation_posts,
//            R.id.navigation_events
//        ))
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
//           navView.isVisible = true


           checkGoogleApiAvailability()

           val viewModel by viewModels<AuthViewModel>()

           var currentMenuProvider: MenuProvider? = null
           viewModel.data.observe(this) {
               currentMenuProvider?.also(::removeMenuProvider)

               addMenuProvider(object : MenuProvider {
                   override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                       menuInflater.inflate(R.menu.menu_main, menu)
                       val authorized = viewModel.authorized
                       menu.setGroupVisible(R.id.authorized, authorized)
                       menu.setGroupVisible(R.id.unAuthorized, !authorized)
                   }

                   override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                       when (menuItem.itemId) {
                           R.id.signIn -> {
                               findNavController(R.id.nav_host_fragment).navigate(R.id.action_navigation_posts_to_signInFragment)
                               //AppAuth.getInstance().setAuth(5, "x-token")
                               true
                           }
                           R.id.signUp -> {
                               findNavController(R.id.nav_host_fragment).navigate(R.id.action_navigation_posts_to_registrationFragment)
                               //AppAuth.getInstance().setAuth(5, "x-token")
                               true
                           }
                           R.id.logout ->{
                               showSignOutDialog()
                               //AppAuth.getInstance().removeAuth()
                               true
                           }
                           else -> false
                       }

               }.apply {
                   currentMenuProvider = this
               })
           }
       }

    private fun showSignOutDialog(){
        val listener = DialogInterface.OnClickListener{ _, which->
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> AppAuth.getInstance().removeAuth()
                DialogInterface.BUTTON_NEGATIVE -> Toast.makeText(this, "ну и ладненько...", Toast.LENGTH_SHORT).show()
            }
        }
        val dialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Внимание!")
            .setMessage("Вы точно хотите выйти?")
            .setPositiveButton("Уверен!", listener)
            .setNegativeButton("Нет", listener)
            .create()

        dialog.show()
    }


    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }
}