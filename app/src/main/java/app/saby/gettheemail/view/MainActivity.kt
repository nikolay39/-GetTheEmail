package app.saby.gettheemail.view
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.saby.gettheemail.EmailDialog
import app.saby.gettheemail.R
import app.saby.gettheemail.databinding.ActivityMainBinding
import app.saby.gettheemail.model.Weather
import app.saby.gettheemail.viewmodel.EmailViewModel

const val CHANNEL_ID:String = "getTheEmail";
const val NOTIFICATION_ID:Int = 123;

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding;
    val model: EmailViewModel by viewModels();
    companion object {
        var active: Boolean = false;
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(layoutInflater);
        val view = binding.root
        setContentView(view)
    }
    override fun onStart() {
        super.onStart()
        active = true;
    }
    override fun onStop() {
        super.onStop();
        active = false;
    }
    /*
    override fun onDestroy() {
        super.onDestroy();
        model.mDisposable.dispose();
    }
    */
    fun showEmail(message: String?) {
        message?.let {
            if (!active) {
                showNotification(message);
            }
            EmailDialog.newInstance(getString(R.string.dialog_title), message).show(this.supportFragmentManager, EmailDialog.TAG);
            binding.tvMessage.text = message;
        }
    }
    private fun showNotification(text: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GetTheEmail")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentText(text)
            .setSound(alarmSound)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}