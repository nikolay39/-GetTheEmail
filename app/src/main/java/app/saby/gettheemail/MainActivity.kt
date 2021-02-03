package app.saby.gettheemail
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.saby.gettheemail.domain.Secret
import app.saby.gettheemail.domain.Weather
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

const val CHANNEL_ID:String = "getTheEmail";
const val NOTIFICATION_ID:Int = 123;

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var weatherObservable:Observable<Weather>;
    private lateinit var emailObservable:Observable<String>;
    companion object {
        var active: Boolean = false;
    }
    val mDisposable: CompositeDisposable = CompositeDisposable();
    private val tvMessage: TextView?
        get() = findViewById(R.id.tvMessage)

    private val tvError: TextView?
        get() = findViewById(R.id.tvError)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        Log.i(CHANNEL_ID, "run onCreate");
        getTestData();
        weatherObservable.let { it->
            mDisposable.add(
                it
                    .subscribeOn(Schedulers.computation())
                    .doOnError{error ->
                        tvError?.text = error.message; }
                    .filter{weather:Weather ->
                        with(NotificationManagerCompat.from(this)) {
                            cancel(NOTIFICATION_ID)
                        }
                        tvMessage?.text = getString(R.string.email_not_found);
                        weather.secret_code !== null;
                    }
                    .flatMap{weater: Weather -> selectEmail(weater)}
                    .doOnError{error ->
                        tvError?.text = error.message; }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {message -> showEmail(message)}
            )
        }
    }
    private fun selectEmail(weather: Weather?):Observable<String> {
        emailObservable = Observable.create<String> { emitter ->
            weather?.secret_code?.let { secret_code ->
                Firebase.database.getReference(secret_code)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(CHANNEL_ID, "addListenerForSingleValueEvent onDataChange select");
                            val secret = snapshot.getValue<Secret>(Secret::class.java);
                            Log.i(CHANNEL_ID, "addListenerForSingleValueEvent onDataChange secret $secret");
                            secret?.email?.let { email -> emitter.onNext(email);}
                        }
                        override fun onCancelled(error: DatabaseError) {
                            emitter.onError(Throwable("Error"));
                        }
                    })
            }
        }
        return emailObservable;
    }
    override fun onStart() {
        super.onStart()
        active = true;
    }
    override fun onStop() {
        super.onStop();
        active = false;
    }
    override fun onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
    }
    fun showEmail(message: String?) {
        message?.let {
            if (!active) {
                showNotification(message);
            }
            EmailDialog.newInstance("title", "message").show(this.supportFragmentManager, EmailDialog.TAG);
            tvMessage?.text = message;
        }
    }
    private fun createObservableWheater() {
        Log.i(CHANNEL_ID, "createObservableEmail run");
        weatherObservable = Observable.create<Weather> { emitter ->
            Firebase.auth.signInAnonymously().addOnSuccessListener { result ->
                if (result.user?.uid != null) {
                    Log.i(CHANNEL_ID, "user $result.user.uid");
                    Log.i(CHANNEL_ID, "getReferenceWeather ${Firebase.database.getReference("weather")}");
                    Firebase.database.getReference("weather").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(CHANNEL_ID, "addValueEventListener onDataChange wheater");
                            val weather = snapshot.getValue<Weather>(Weather::class.java);
                            Log.i(
                                CHANNEL_ID,
                                "addValueEventListener onDataChange weather $weather"
                            );
                            weather?.let {
                                emitter.onNext(weather);
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            emitter.onError(Throwable("Error"));
                        }
                    })
                }
            }.addOnFailureListener { it.message?.let {
                    message -> emitter.onError(Throwable(message))  }
            }
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
    private fun getTestData() {
        Log.i(CHANNEL_ID, "getTestData run");
        val userId = Firebase.auth.currentUser?.uid
        Log.i(CHANNEL_ID, "userId $userId");
        createObservableWheater();
    }
}