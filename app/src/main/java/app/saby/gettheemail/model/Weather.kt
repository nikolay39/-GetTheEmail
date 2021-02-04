package app.saby.gettheemail.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class Weather(
    val id      : String? = null,
    val title   : String? = null,
    val temp    : Float? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("secret_code")
    val secretCode: String? = null,
    val timeStamp: Long? = null
)




