package app.saby.gettheemail

import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    companion object {
        var active: Boolean = false;
    }
    val mDisposable: CompositeDisposable = CompositeDisposable();
    private val tvTest: TextView?
        get() = findViewById(R.id.tvTest)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        val emailObservable = getTestData();
        mDisposable.add(
            emailObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showEmail(it) }
        )
    }
    override fun onStart() {
        super.onStart()
        active = true;
    }
    override fun onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
    }

    fun showEmail(message: String) {
        if (!active) {
            showNotification(message);
        }
        tvTest?.text = message;
    }
    private fun showNotification(text: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GetTheEmail")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
    private fun getTestData():Observable<String>  {
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            val emailObservable = Observable.create<String> { emitter ->
                Firebase.auth.signInAnonymously().addOnSuccessListener { result ->
                    if (result.user?.uid != null) {
                        Firebase.database.getReference("test").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val weather: Weather? = snapshot.child("Weather").getValue<Weather>(Weather::class.java);
                                    weather?.let {
                                        if (it.secret_code.isNotBlank()) {
                                            val secret: Secret? = snapshot.child(it.secret_code).getValue<Secret>(Secret::class.java);
                                            secret?.let {
                                                if (it.email.isNotBlank()) {
                                                    emitter.onNext(it.email)
                                                }
                                            }
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    emitter.onNext("Error");
                                }
                        })
                    }
                }.addOnFailureListener { it.message?.let { message -> emitter.onNext(message) } }
            }
            return emailObservable;
        }

    }
}