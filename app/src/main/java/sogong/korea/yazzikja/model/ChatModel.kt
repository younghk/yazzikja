package sogong.korea.yazzikja.model

import org.w3c.dom.Comment

import java.util.HashMap

class ChatModel {

    var users: HashMap<String, Boolean>  = HashMap()
    var comments: HashMap<String, Comment> = HashMap()

    class Comment {
        var uid: String? = null
        var message: String? = null
        var timestamp: Any? = null
    }
}
