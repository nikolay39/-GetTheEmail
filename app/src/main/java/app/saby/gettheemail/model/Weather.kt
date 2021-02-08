package app.saby.gettheemail.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    var id      : String? = null,
    var title   : String? = null,
    var temp    : Float? = null,
    var image_url: String? = null,
    var secret_code: String? = null,
    var timeStamp: Long? = null
) {
    val secretCode: String?
        get() = this.secret_code
}




