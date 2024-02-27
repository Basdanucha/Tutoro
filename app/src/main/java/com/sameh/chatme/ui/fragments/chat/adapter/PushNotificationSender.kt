import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PushNotificationSender {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun sendNotificationToUser(userId: String, notificationMessage: String, title: String) {
        // ส่งแจ้งเตือนไปยังเครื่องของผู้ใช้ที่มี userId ที่กำหนด
        getUserFCMToken(userId) { fcmToken ->
            if (fcmToken != null) {
                sendPushNotification(fcmToken, notificationMessage , userId)
                sendNotification(fcmToken , notificationMessage , title)
            }
        }
    }

    private fun getUserFCMToken(userId: String, onComplete: (String?) -> Unit) {
        // อ่าน FCM token จาก Firebase Realtime Database สำหรับผู้ใช้ที่มี userId ที่กำหนด
        database.child(userId).child("fcmToken").get().addOnSuccessListener { dataSnapshot ->
            val fcmToken = dataSnapshot.value as? String
            onComplete(fcmToken)
        }.addOnFailureListener {
            onComplete(null)
        }
    }

    private fun sendPushNotification(fcmToken: String, notificationMessage: String , id: String) {
        println(fcmToken)
            val notificationMessage = RemoteMessage.Builder(fcmToken)
                .setMessageId(id)
                .addData("title","Tutoro")
                .addData("body",notificationMessage)
                .build()

            FirebaseMessaging.getInstance().send(notificationMessage)


    }


    private fun sendNotification(fcmToken: String, message: String, title: String ) {
        val serverKey = "AAAAiXC8wnk:APA91bGG1hfA-fMltsXX-LwlNTZvHPczVTf3XffcEa-6HYvbYZjABBp67pRvEepTxGEmk06xciPg7JrWkmekv6_UkLC8tUbHJCRSvLBhjvW8qE2AbESlYqy2fX888r97013NziJ4DPhk"
        val fcmToken = fcmToken

        val url = "https://fcm.googleapis.com/fcm/send"
        val client = OkHttpClient()

        // Create JSON payload for the notification
        val notificationData = JSONObject()
        notificationData.put("title", "$title")
        notificationData.put("body", "$message")

        val requestBody = JSONObject()
        requestBody.put("to", fcmToken)
        requestBody.put("data", notificationData)

        // Create the request
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "key=$serverKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        // Send the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle success
                if (response.isSuccessful) {
                    println("Notification sent successfully!")
                } else {
                    println("Failed to send notification. Response: ${response.body?.string()}")
                }
            }
        })
    }



}
