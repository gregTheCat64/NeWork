package ru.javacat.nework.ui.screens

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TableLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.ActivityAppBinding
import ru.javacat.nework.databinding.FragmentPostsBinding
import ru.javacat.nework.ui.adapter.ViewPagerAdapter
import ru.javacat.nework.ui.screens.NewPostFragment.Companion.textArg
import ru.javacat.nework.ui.viewmodels.AuthViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {


    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    var duration = 0L

    val viewModel: AuthViewModel by viewModels()
    val userViewModel: UserViewModel by viewModels()

    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivityAppBinding

    //private val postViewModel: PostViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()

        val audioBar = findViewById<View>(R.id.audioBar)
        val barPlayBtn = findViewById<Button>(R.id.barPlayBtn)
        val barSeekBar = findViewById<SeekBar>(R.id.barSeekBar)

        barPlayBtn.setOnClickListener {
            if (player.isPlaying){
                player.pause()
            } else player.play()
        }

        barSeekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{

            private var mprogress = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mprogress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress = mprogress
                player.seekTo((mprogress.toLong()*duration)/100)
            }
        })


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

//        intent?.let {
//            if (it.action != Intent.ACTION_SEND) {
//                return@let
//            }
//
//            val text = it.getStringExtra(Intent.EXTRA_TEXT)
//            if (text?.isNotBlank() != true) {
//                return@let
//            }
//
//            intent.removeExtra(Intent.EXTRA_TEXT)
//            findNavController(R.id.nav_host_fragment)
//                .navigate(
//                    R.id.action_navigation_posts_to_newPostFragment,
//                    Bundle().apply {
//                        textArg = text
//                    }
//                )
//        }

//        binding.settingsBtn.setOnClickListener {
//            showMenu(it)
//        }

        //checkGoogleApiAvailability()


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
                            findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)
                            true
                        }
                        R.id.signUp -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.registrationFragment)
                            true
                        }
                        R.id.logout -> {
                            showSignOutDialog(appAuth, this@AppActivity)
                            true
                        }
                        R.id.profileBtn -> {
                            if (appAuth.getId()!=0L){
                                val id = appAuth.getId()
                                val bundle = Bundle()
                                bundle.putLong("userID", id)
                                //val action = PostsFragmentDirections.actionNavigationPostsToWallFragment(id)
                                findNavController(R.id.nav_host_fragment).navigate(R.id.wallFragment, bundle)
                            } else findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)
                            true
                        }
                        else -> false
                    }

            }.apply {
                currentMenuProvider = this
            })
        }
    }

    fun playAudio(url: String){

        val mediaItem = MediaItem.fromUri(url)


        player.clearMediaItems()
        player.setMediaItem(mediaItem)
        player.prepare()

        player.play()

        val scope = CoroutineScope(Dispatchers.Main)

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.i("MYSTATE", playbackState.toString())
                binding.audioBar.root.isVisible = true
                    //playbackState == Player.STATE_READY

                scope.launch {
                    while (playbackState == Player.STATE_READY) {
                        val position = player.currentPosition
                        val positionInSec = round(position.toDouble()/1000).toInt()
                        duration  = player.contentDuration
                        val durationInSec = round(duration.toDouble()/1000).toInt()
                        binding.audioBar.barSeekBar.progress = calculateProgress(position, duration)
                        binding.audioBar.barPlayBtn.text = "$positionInSec/$durationInSec"
                        Log.i("POS", position.toString())
                        delay(1000)
                    }
                }
                //scope.cancel()
                //duration = 0L
            }
        })

    }

    fun calculateProgress(position:Long, duration: Long): Int {
        return ((position.toDouble()/duration.toDouble())*100).toInt()
    }

//    private fun showMenu(view: View) {
//        val menu = PopupMenu(this, view)
//        menu.inflate(R.menu.menu_main)
//        menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {item->
//            when (item.itemId){
//                R.id.signIn -> {
//                    findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)
//                    true
//                }
//                R.id.signUp -> {
//                    findNavController(R.id.nav_host_fragment).navigate(R.id.registrationFragment)
//                    true
//                }
//                R.id.logout -> {
//                    showSignOutDialog()
//                    true
//                }
//                else -> {Toast.makeText(this, "lala", Toast.LENGTH_SHORT).show()}
//            }
//            true
//        })
//        menu.show()
//    }

//    private fun showSignOutDialog() {
//        val listener = DialogInterface.OnClickListener { _, which ->
//            when (which) {
//                DialogInterface.BUTTON_POSITIVE -> appAuth.removeAuth()
//                DialogInterface.BUTTON_NEGATIVE -> Toast.makeText(
//                    this,
//                    "ну и ладненько...",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//        val dialog = AlertDialog.Builder(this)
//            .setCancelable(false)
//            .setTitle("Внимание!")
//            .setMessage("Вы точно хотите выйти?")
//            .setPositiveButton("Уверен!", listener)
//            .setNegativeButton("Нет", listener)
//            .create()
//
//        dialog.show()
//    }


//    private fun checkGoogleApiAvailability() {
//        with(googleApiAvailability) {
//            val code = isGooglePlayServicesAvailable(this@AppActivity)
//            if (code == ConnectionResult.SUCCESS) {
//                return@with
//            }
//            if (isUserResolvableError(code)) {
//                getErrorDialog(this@AppActivity, code, 9000)?.show()
//                return
//            }
//            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
//                .show()
//        }
//
//        firebaseMessaging.token.addOnSuccessListener {
//            println(it)
//        }
//    }

}

