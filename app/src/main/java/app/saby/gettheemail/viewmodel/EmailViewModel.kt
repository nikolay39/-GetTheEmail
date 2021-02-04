package app.saby.gettheemail.viewmodel

import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import app.saby.gettheemail.R
import app.saby.gettheemail.model.Secret
import app.saby.gettheemail.model.Weather
import app.saby.gettheemail.view.CHANNEL_ID
import app.saby.gettheemail.view.NOTIFICATION_ID
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

class EmailViewModel:ViewModel() {

    lateinit var weatherObservable: Observable<Weather>;
    lateinit var emailObservable: Observable<String>;
    val mDisposable: CompositeDisposable = CompositeDisposable();
    fun selectEmail(weather: Weather?):Observable<String> {
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
    private fun createObservableWheater() {
        Log.i(CHANNEL_ID, "createObservableEmail run");
        weatherObservable = Observable.create<Weather> { emitter ->
            Firebase.auth.signInAnonymously().addOnSuccessListener { result ->
                if (result.user?.uid != null) {
                    Log.i(CHANNEL_ID, "user $result.user.uid");
                    Log.i(CHANNEL_ID, "getReferenceWeather ${Firebase.database.getReference("weather")}");
                    Firebase.database.getReference("weather").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(CHANNEL_ID, "addValueEventListener onDataChange weather");
                            val weather = snapshot.getValue<Weather>(Weather::class.java);
                            Log.i( CHANNEL_ID, "addValueEventListener onDataChange weather $weather");
                            weather?.let { emitter.onNext(weather); }
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
    fun getTestData() {
        val userId = Firebase.auth.currentUser?.uid
        Log.i(CHANNEL_ID, "userId $userId");
        createObservableWheater();
    }
}