package app.saby.gettheemail.repository

import android.util.Log
import androidx.core.app.NotificationManagerCompat
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
import io.reactivex.Maybe
import io.reactivex.MaybeSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class Repository() {
    val userId = Firebase.auth.currentUser?.uid
    lateinit var weatherObservable: Single<Weather>;
    lateinit var emailObservable: Maybe<String>;
    fun init():Maybe<String> {
        createObservableWheater();
        return weatherObservable.let {singleWeather->
            singleWeather
                .subscribeOn(Schedulers.computation())
                .filter{weather:Weather -> weather.secretCode !== null;}
                .flatMap{weater: Weather -> selectEmail(weater)}
        }
    }
    fun createObservableWheater() {
        Log.i(CHANNEL_ID, "createObservableEmail run");
        weatherObservable = Single.create<Weather> { emitter ->
            Firebase.auth.signInAnonymously().addOnSuccessListener { result ->

                if (result.user?.uid != null) {
                    Log.i(CHANNEL_ID, "user $result.user.uid");
                    Log.i(CHANNEL_ID, "getReferenceWeather ${Firebase.database.getReference("weather")}");
                    Firebase.database.getReference("weather").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(CHANNEL_ID, "addValueEventListener onDataChange weather");
                            val weather = snapshot.getValue<Weather>(Weather::class.java);
                            Log.i( CHANNEL_ID, "addValueEventListener onDataChange weather $weather");
                            weather?.let { emitter.onSuccess(weather); }
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
    private fun selectEmail(weather: Weather?): Maybe<String> {
        emailObservable = Maybe.create<String>{ emitter ->
            weather?.secretCode?.let { secretCode ->
                Firebase.database.getReference(secretCode)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(CHANNEL_ID, "addListenerForSingleValueEvent onDataChange select");
                            val secret = snapshot.getValue<Secret>(Secret::class.java);
                            Log.i(CHANNEL_ID, "addListenerForSingleValueEvent onDataChange secret $secret");
                            secret?.email?.let { email -> emitter.onSuccess(email);}
                        }
                        override fun onCancelled(error: DatabaseError) {
                            emitter.onError(Throwable("Error"));
                        }
                    })
            }
        }
        return emailObservable;
    }
}