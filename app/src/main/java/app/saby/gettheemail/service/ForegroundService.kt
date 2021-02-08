package app.saby.gettheemail.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import androidx.core.app.NotificationCompat
import app.saby.gettheemail.R
import app.saby.gettheemail.model.repository.Repository
import app.saby.gettheemail.view.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ForegroundService: Service() {
    private val NOTIFICATION_CHANNEL_ID = "Foreground getTheEmail";
    private val CHANNEL_NAME = "Foreground getTheEmail";
    private var serviceRunningInForeground = false
    private  var disposable: Disposable? = null;
    private val localBinder = LocalBinder()

    private val emailObservable = Repository().init();
    private lateinit var notificationManager: NotificationManager

    companion object{
        const val NOTIFICATION_ID:Int = 123;
        const val EXTRA_CANCEL_FROM_NOTIFICATION = "$PACKAGE_NAME.extra.CANCEL_FROM_NOTIFICATION"
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand()")
        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_FROM_NOTIFICATION, false)
        if (cancelLocationTrackingFromNotification) {
            unsubscribeTonUpdates();
            stopSelf();
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }
    override fun onCreate() {
        Timber.i("onCreate");
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(serviceRunningInForeground) {
            Timber.i("serviceRunningInForeground true")
        } else {
            Timber.i("serviceRunningInForeground false")
        }
        super.onCreate()
    }
    fun subscribeTonUpdates() {
        Timber.i("subscribeTonUpdates()")
        disposable = emailObservable
            .subscribe({email ->
                notificationManager.notify(NOTIFICATION_ID, generateNotification(email))
            })
    }
    fun unsubscribeTonUpdates() {
        disposable?.dispose()
    }
    override fun onBind(intent: Intent): IBinder? {
        Timber.i("onBind()")
        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        unsubscribeTonUpdates();
        return localBinder
    }
    override fun onRebind(intent: Intent) {
        Timber.i( "onRebind()")
        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        unsubscribeTonUpdates();
        super.onRebind(intent)
    }
    override fun onUnbind(intent: Intent): Boolean {
        Timber.i( "onUnbind()")
        val message: String = getString(R.string.default_notification);
        Timber.i(message);
        val notification = generateNotification(message)
        startForeground(NOTIFICATION_ID, notification)
        serviceRunningInForeground = true;
         subscribeTonUpdates();
        return true
    }
    private fun generateNotification(message: String): Notification {
        Timber.i("generateNotification()");

        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("GetTheEmail")
        Timber.i("generate nofitication step 2 end");
        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(this, MainActivity::class.java);

        val cancelIntent = Intent(this, ForegroundService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_FROM_NOTIFICATION, true);

        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)
        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        Timber.i("notification end()");
        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_launcher_foreground, getString(R.string.launch_activity),
                activityPendingIntent
            )
            .addAction(
                R.drawable.ic_stat_name,
                getString(R.string.stop_location),
                servicePendingIntent
            )
            .build()
    }
    inner class LocalBinder : Binder() {
        internal val service: ForegroundService
            get() = this@ForegroundService
    }
}