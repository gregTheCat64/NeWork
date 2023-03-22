//package ru.javacat.nework.service
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//import com.google.gson.Gson
//import dagger.hilt.android.AndroidEntryPoint
//import ru.javacat.nework.R
//import ru.javacat.nework.data.auth.AppAuth
//import ru.javacat.nework.data.dto.Notify
//import java.lang.IllegalArgumentException
//import javax.inject.Inject
//import kotlin.random.Random
//
//@AndroidEntryPoint
//class FCMService : FirebaseMessagingService() {
//    private val action = "action"
//    private val content = "content"
//    private val channelId = "remote"
//    private val gson = Gson()
//
//    @Inject
//    lateinit var appAuth: AppAuth
//
//
//    override fun onCreate() {
//        super.onCreate()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = getString(R.string.channel_remote_name)
//            val descriptionText = getString(R.string.channel_remote_description)
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(channelId, name, importance).apply {
//                description = descriptionText
//            }
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    override fun onMessageReceived(message: RemoteMessage) {
//        println(message.data["content"])
//        val notify: Notify = gson.fromJson(message.data[content], Notify::class.java)
//        val recipientId = notify.recipientId
//        val myId = appAuth.getId()
//
//        when {
//            recipientId == null || recipientId == myId -> handleNotify(notify.content)
//            recipientId != myId -> appAuth.sendPushToken()
//        }
//
////        message.data[action]?.let {
////            when (Action.getValidAction(it)) {
////                Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
////                Action.NEW_POST -> handleNewPost(
////                    gson.fromJson(
////                        message.data[content],
////                        NewPost::class.java
////                    )
////
////                )
////                Action.ERROR -> println("ERROR_PUSH")
////            }
////
////        }
//    }
//
//    override fun onNewToken(token: String) {
//        appAuth.sendPushToken(token)
//        println("new pushToken sent: $token")
//    }
//
//    private fun handleLike(content: Like) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_baseline_cruelty_free_24)
//            .setContentTitle(
//                getString(
//                    R.string.notification_user_liked,
//                    content.userName,
//                    content.postAuthor,
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }
//
//    private fun handleNewPost(content: NewPost) {
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
//            .setContentTitle(
//                getString(
//                    R.string.notification_new_post,
//                    content.postAuthor
//                )
//            )
//            .setContentText(content.postTopic)
//            .setStyle(NotificationCompat.BigTextStyle()
//                .bigText(content.postText))
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }
//
//    private fun handleNotify(content: String){
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
//            .setContentText(content)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }
//}
//
//enum class Action {
//    LIKE,NEW_POST, ERROR;
//
//    companion object {
//        fun getValidAction(action: String): Action {
//            return try {
//                valueOf(action)
//            } catch (exception: IllegalArgumentException) {
//                ERROR
//            }
//        }
//    }
//}
//
//data class Like(
//    val userId: Long,
//    val userName: String,
//    val postId: Long,
//    val postAuthor: String,
//)
//
//data class NewPost(
//    val postAuthor: String,
//    val postText: String,
//    val postTopic: String
//)
//
