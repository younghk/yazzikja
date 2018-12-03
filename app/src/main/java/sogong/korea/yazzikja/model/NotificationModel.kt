package sogong.korea.yazzikja.model

import android.app.Notification

class NotificationModel {
    var to: String? = null
    var notification = Notification()
    var data = Data()

    class Notification {
        var title: String? = null
        var text: String? = null
    }

    class Data {
        var title: String? = null
        var text: String? = null
    }
}