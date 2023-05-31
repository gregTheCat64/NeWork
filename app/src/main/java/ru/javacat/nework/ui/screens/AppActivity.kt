package ru.javacat.nework.ui.screens

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.findNavController
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.ActivityAppBinding
import ru.javacat.nework.ui.viewmodels.AuthViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadAvatar
import ru.javacat.nework.util.loadCircleCrop
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

        //player
        player = ExoPlayer.Builder(this).build()

        val audioBar = findViewById<View>(R.id.audioBar)
        val barPlayBtn = findViewById<Button>(R.id.barPlayBtn)
        val barSeekBar = findViewById<SeekBar>(R.id.barSeekBar)
        val barCloseBtn = findViewById<Button>(R.id.barCloseBtn)
        val profileBtn = findViewById<View>(R.id.profileBtn)
        val avatarImage = findViewById<ImageView>(R.id.appBarImage)
        //val mainMenu = findViewById<MenuItem>(R.id.authorized)

        avatarImage.setOnClickListener {
            var authorized = viewModel.authorized
            if (authorized){
                showAuthorizedMenu(it)
            } else showMenu(it)
        }

        barPlayBtn.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else player.play()
        }

        barCloseBtn.setOnClickListener {
            player.stop()
            audioBar.isVisible = false
        }

        //seekBar
        barSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var mprogress = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mprogress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress = mprogress
                player.seekTo((mprogress.toLong() * duration) / 100)
            }
        })



        //menu:
        viewModel.data.observe(this) {
            val id = appAuth.getId()
            userViewModel.getUserById(id)
        }

        userViewModel.user.observe(this){user->
                user.avatar.let {
                    val authorized = viewModel.authorized
                    if (authorized){
                        avatarImage.loadAvatar(it.toString())
                    } else
                        avatarImage.setImageDrawable(resources.getDrawable(R.drawable.baseline_account_circle_36))
                }
        }
    }


    fun playAudio(url: String) {

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
                        val positionInSec = round(position.toDouble() / 1000).toInt()
                        duration = player.contentDuration
                        val durationInSec = round(duration.toDouble() / 1000).toInt()
                        binding.audioBar.barSeekBar.progress = calculateProgress(position, duration)
                        //binding.audioBar.barPlayBtn.text = "$positionInSec/$durationInSec"
                        Log.i("POS", position.toString())
                        delay(1000)
                    }
                }
                //scope.cancel()
                //duration = 0L
            }
        })

    }

    fun calculateProgress(position: Long, duration: Long): Int {
        return ((position.toDouble() / duration.toDouble()) * 100).toInt()
    }

    private fun showMenu(view: View) {
        val menu = PopupMenu(this, view)
        menu.inflate(R.menu.menu_main)
        menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {item->
            when (item.itemId){
                R.id.signIn -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)

                }
                R.id.signUp -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.registrationFragment)

                }

                else -> {Toast.makeText(this, "lala", Toast.LENGTH_SHORT).show()}
            }
            true
        })
        menu.show()
    }

    private fun showAuthorizedMenu(view: View) {
        val menu = PopupMenu(this, view)
        menu.inflate(R.menu.menu_by_authorized)
        menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {item->
            when (item.itemId){
                R.id.logout -> {
                    showSignOutDialog(appAuth, this)

                }
                R.id.profileBtn -> {
                    if (appAuth.getId() != 0L) {
                        val id = appAuth.getId()
                        val bundle = Bundle()
                        bundle.putLong("userID", id)
                        findNavController(R.id.nav_host_fragment).navigate(
                            R.id.wallFragment,
                            bundle
                        )
                    } else findNavController(R.id.nav_host_fragment).navigate(R.id.signInFragment)

                }
                else -> {Toast.makeText(this, "lala", Toast.LENGTH_SHORT).show()}
            }
            true
        })
        menu.show()
    }


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

