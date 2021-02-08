package app.saby.gettheemail.model.repository

import app.saby.gettheemail.model.Secret
import app.saby.gettheemail.model.Weather
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class Repository() {
    val userId = Firebase.auth.currentUser?.uid
    lateinit var weatherObservable: Observable<Weather>;
    lateinit var emailObservable: Observable<String>;
    fun init():Observable<String> {
        createObservableWheater();
        return weatherObservable.let {singleWeather->
            singleWeather
                .subscribeOn(Schedulers.computation())
                //.filter{weather:Weather ->  weather.secretCode !== null;}
                .flatMap({weater: Weather ->
                    println("transform")
                    selectEmail(weater)})
        }
    }
    fun createObservableWheater() {
        Timber.i("createObservableEmail run");
        weatherObservable = Observable.create<Weather> { emitter ->
            Firebase.auth.signInAnonymously().addOnSuccessListener { result ->
                if (result.user?.uid != null) {
                    Timber.i("user $result.user.uid");
                    Timber.i( "getReferenceWeather ${Firebase.database.getReference("weather")}");
                    Firebase.database.getReference("weather").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Timber.i("addValueEventListener onDataChange weather");
                            val weather = snapshot.getValue<Weather>(Weather::class.java);
                            Timber.i( "addValueEventListener onDataChange weather $weather");
                            weather?.let {
                                Timber.i("run onSucces rx")
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
    private fun selectEmail(weather: Weather?): Observable<String> {
        emailObservable = Observable.create<String>{ emitter ->
            weather?.secretCode?.let { secretCode ->
                Firebase.database.getReference(secretCode)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Timber.i( "addListenerForSingleValueEvent onDataChange select");
                            val secret = snapshot.getValue<Secret>(Secret::class.java);
                            Timber.i( "addListenerForSingleValueEvent onDataChange secret $secret");
                            secret?.email?.let { email -> emitter.onNext(email)}
                        }
                        override fun onCancelled(error: DatabaseError) {
                            emitter.onError(
                                Throwable("Error"));
                        }
                    })
            }?: emitter.onNext("test@mail.ru")
        }
        Timber.i("run selectEmail");
        return emailObservable;
    }
}