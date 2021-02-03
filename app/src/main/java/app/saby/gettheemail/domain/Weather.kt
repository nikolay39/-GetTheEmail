package app.saby.gettheemail.domain

data class Weather(
    val id      : String? = null,
    val title   : String? = null,
    val temp    : Float? = null,
    val image_url: String? = null,
    val secret_code: String? = null,
    val timeStamp: Long? = null
)