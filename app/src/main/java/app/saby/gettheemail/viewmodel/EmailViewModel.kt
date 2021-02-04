package app.saby.gettheemail.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.saby.gettheemail.model.Secret
import app.saby.gettheemail.model.Weather
import app.saby.gettheemail.repository.Repository
import io.reactivex.Flowable



class EmailViewModel(val repository: Repository):ViewModel() {

    lateinit var email: LiveData<String> ;

    fun getEmail(){
        val emailData: Flowable<String> = repository.init().toFlowable();
        email =  LiveDataReactiveStreams.fromPublisher(emailData)
    }
    fun init() {
        getEmail();
    }

}