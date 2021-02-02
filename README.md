**Тестовое задание**

Цель получить с сервера email для отправки тестового задания

Сервер: Firebase  
[авторизация](https://firebase.google.com/docs/auth/android/anonymous-auth?kotlin%20ktx_1)  
[база](https://firebase.google.com/docs/database/android/start)



Как получить:

1.  Анонимно авторизоваться в Firebase

2.  Читать погоду из базы (database ref: "weather/", json формат: Weather).

3.  В одном из обновлений погоды будет секретный код (поле "secret_code" в Weather). Он действует всего 5 мин.

4.  С помощью кода получить email из базы ( ref: "$your_secret_code/", json формат Secret)

5.  Вывести email в диалоге или если приложение не активно в Notification.

Weather:
```json
 {  
    "id" : "string",  
    "title": "string",  
    "temp": "number",  
    "image_url": "string",  
    "secret_code" : "string",  
    "timeStamp" : "number"  
}
```
Secret:
```json
 { "email": "string" }
```

Проект уже настроен для работы с Firebase. Функция getTestData() в MainActivity для проверки соединения.





