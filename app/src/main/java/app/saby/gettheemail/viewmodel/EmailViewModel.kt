package app.saby.gettheemail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.saby.gettheemail.model.repository.Repository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class EmailViewModel:ViewModel() {

    lateinit var email: LiveData<String> ;
    var oldEmail:  MutableLiveData<String> = MutableLiveData("xxxxx@xxxxxx.xxxxxx");
    private fun getEmail(){
        val repository =  Repository()
    val emailData: Flowable<String> = repository.init().toFlowable(BackpressureStrategy.LATEST);
        email =  LiveDataReactiveStreams.fromPublisher(emailData)
    }
    fun init() {
        getEmail();
    }
}
