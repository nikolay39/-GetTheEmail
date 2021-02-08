package app.saby.gettheemail.view
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import app.saby.gettheemail.EmailDialog
import app.saby.gettheemail.R
import app.saby.gettheemail.databinding.ActivityMainBinding
import app.saby.gettheemail.service.ForegroundService
import app.saby.gettheemail.viewmodel.EmailViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding;

    private var foregroundServiceBound = false
    private var foregroundOnlyService: ForegroundService? = null
    private lateinit var emailObserver:Observer<String> ;

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundService.LocalBinder
            foregroundOnlyService = binder.service
            foregroundServiceBound = true
        }
        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyService = null
            foregroundServiceBound = false
        }
    }
    private val model: EmailViewModel by viewModels();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(layoutInflater);
        val view = binding.root
        setContentView(view);
        model.init();
        // Create the observer which updates the UI.
        emailObserver = Observer<String> { email ->
            email?.let {
                if(model.oldEmail.value !== email) {
                    EmailDialog.newInstance(getString(R.string.dialog_title), email)
                        .show(this.supportFragmentManager, EmailDialog.TAG);
                    model.oldEmail.postValue(email);
                }
                binding.tvMessage.text = email;
            }
        }
        startService(Intent(applicationContext, ForegroundService::class.java));
    }
    override fun onStart() {
        super.onStart()
        model.email.observe(this, emailObserver);
        val serviceIntent = Intent(this, ForegroundService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }
    override fun onStop() {
        model.email.removeObserver(emailObserver);
        if (foregroundServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundServiceBound = false
        }
        super.onStop()
    }
}