package app.saby.gettheemail.domain

data class Weather(
    val id      : String,
    val title   : String,
    val temp    : Float,
    val image_url: String,
    val secret_code: String,
    val timeStamp: Int
)
