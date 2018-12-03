package sogong.korea.yazzikja.chat


import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.textclassifier.TextLinks
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule


import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.item_message.*


import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.TimeZone

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import sogong.korea.yazzikja.R
import sogong.korea.yazzikja.model.ChatModel
import sogong.korea.yazzikja.model.NotificationModel
import sogong.korea.yazzikja.model.UserModel
import kotlin.reflect.KClass

class MessageActivity : AppCompatActivity() {

    private var destinationUid: String? = null
    private var button: ImageButton? = null
    private var editText: EditText? = null

    private var uid: String? = null
    private var chatRoomUid: String? = null

    private var recyclerView: RecyclerView? = null

    private val simpleDateFormat = SimpleDateFormat("MM.dd HH:mm")

    private var destinationUserModel: UserModel? = null

    private var chatroomName: TextView? = null
    private var chatroomImage: ImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        uid = FirebaseAuth.getInstance().currentUser!!.uid

        destinationUid = intent.getStringExtra("destinationUid")
        button = findViewById<View>(R.id.messageactivity_imagebutton) as ImageButton
        editText = findViewById<View>(R.id.messageactivity_edittext) as EditText
        chatroomName = messageactivity_name
        chatroomImage = messageactivity_img_holder1
        recyclerView = findViewById<View>(R.id.messageactivity_recyclerview) as RecyclerView

        checkChatRoom()
        button!!.setOnClickListener {
            val chatModel = ChatModel()
            chatModel.users.plus(Pair(uid!!, true))
            chatModel.users.plus(Pair(destinationUid!!, true))

            if (chatRoomUid == null) {
                button!!.isEnabled = false
                FirebaseDatabase.getInstance().reference.child("chatrooms").push().setValue(chatModel)
                    .addOnSuccessListener { checkChatRoom() }
            } else {
                val comment = ChatModel.Comment()
                comment.uid = uid
                comment.message = editText!!.text.toString()
                comment.timestamp = ServerValue.TIMESTAMP
                FirebaseDatabase.getInstance().reference.child("chatrooms").child(chatRoomUid!!).child("comments")
                    .push().setValue(comment).addOnCompleteListener {
                    sendGcm()
                    editText!!.setText("")
                }
            }
        }

        checkChatRoom()
    }

    internal fun sendGcm() {
        val gson = Gson()

        val userNickname = FirebaseAuth.getInstance().currentUser!!.displayName
        val notificationModel = NotificationModel()
        notificationModel.to = destinationUserModel!!.pushToken
        notificationModel.notification.title = "Sender"
        notificationModel.notification.text = editText!!.text.toString()
        notificationModel.data.title = userNickname
        notificationModel.data.text = editText!!.text.toString()

        val requestBody =
            RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel))

        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .addHeader("Authorization", "key=AIzaSyAiUafX93GCzGNtpXDsJxwR4XlK0Pb56FQ")
            .url("https://gcm-http.googleapis.com/gcm/send")
            .post(requestBody)
            .build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

            }
        })
    }

    internal fun checkChatRoom() {
        FirebaseDatabase.getInstance().reference.child("chatrooms").orderByChild("users/" + uid!!).equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null) {
                        button!!.isEnabled = false
                        val newRoom = ChatModel()
                        newRoom.users.plus(Pair(uid!!, true))
                        newRoom.users.plus(Pair(destinationUid!!, true))
                        FirebaseDatabase.getInstance().reference.child("chatrooms").push().setValue(newRoom)
                            .addOnSuccessListener { checkChatRoom() }
                        return
                    }

                    for (item in dataSnapshot.children) {
                        val chatModel = item.getValue(ChatModel::class.java)
                        if (chatModel!!.users.containsKey(destinationUid) && chatModel.users.size == 2) {
                            chatRoomUid = item.key
                            button!!.isEnabled = true
                            recyclerView!!.layoutManager = LinearLayoutManager(this@MessageActivity)
                            recyclerView!!.adapter = RecyclerViewAdapter()
                        }
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    internal inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var comments: MutableList<ChatModel.Comment>

        init {
            comments = ArrayList<ChatModel.Comment>()
            FirebaseDatabase.getInstance().reference.child("users").child(destinationUid!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        destinationUserModel = dataSnapshot.getValue(UserModel::class.java)
                        chatroomName?.setText(destinationUserModel!!.userNickname)

                        getMessageList()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

        }

        fun getMessageList() {
            FirebaseDatabase.getInstance().reference.child("chatrooms").child(chatRoomUid!!).child("comments")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        comments.clear()

                        for (item in dataSnapshot.children) {
                            comments.add(item.getValue(ChatModel.Comment::class.java)!!)
                        }

                        notifyDataSetChanged()

                        recyclerView!!.scrollToPosition(comments.size - 1)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        private inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var textview_left_message: TextView
            var textview_right_message: TextView
            var textview_nickname: TextView
//            var imageview_profile: ImageView
            var linearlayout_destination: LinearLayout
            var linearylayout_main: LinearLayout
            var textview_left_timestamp: TextView
            var textview_right_timestamp: TextView
            var linearlayout_right: LinearLayout
            var linearlayout_left: LinearLayout

            init {
                textview_left_message = view.findViewById<View>(R.id.messageitem_textview_left_message) as TextView
                textview_right_message = view.findViewById<View>(R.id.messageitem_textview_right_message) as TextView
                textview_nickname = view.findViewById<View>(R.id.messageitem_textview_nickname) as TextView
//                imageview_profile = view.findViewById<View>(R.id.messageitem_imageview_profile) as ImageView
                linearlayout_destination =
                        view.findViewById<View>(R.id.messageitem_linearlayout_destination) as LinearLayout
                linearylayout_main = view.findViewById<View>(R.id.messageitem_linearlayout_main) as LinearLayout
                textview_left_timestamp = view.findViewById<View>(R.id.messageitem_textview_left_timestamp) as TextView
                textview_right_timestamp = view.findViewById<View>(R.id.messageitem_textview_right_timestamp) as TextView
                linearlayout_right = view.findViewById(R.id.messageitem_linearlayout_right) as LinearLayout
                linearlayout_left = view.findViewById(R.id.messageitem_linearlayout_left) as LinearLayout

            }
        }


        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            val messageViewHolder = viewHolder as MessageViewHolder

            if (comments[position].uid == uid) {
                messageViewHolder.textview_right_message.text = comments[position].message
                messageViewHolder.textview_right_message.setBackgroundResource(R.drawable.rightbubble)

//                messageViewHolder.textview_right_message.scaleY(-1.0)
                messageViewHolder.linearlayout_destination.visibility = View.INVISIBLE
                messageViewHolder.linearlayout_left.visibility = View.INVISIBLE
                messageViewHolder.textview_right_message.textSize = 25f
                messageViewHolder.linearylayout_main.gravity = Gravity.RIGHT
            } else {
                Glide.with(viewHolder.itemView.context)
                    .load(destinationUserModel!!.profileImageUri)
                    .apply(RequestOptions().circleCrop())
                    .into(messageactivity_img_holder1)
//                messageViewHolder.textview_nickname.text = destinationUserModel!!.userNickname
                messageViewHolder.linearlayout_destination.visibility = View.VISIBLE
                messageViewHolder.linearlayout_right.visibility = View.INVISIBLE
                messageViewHolder.textview_left_message.setBackgroundResource(R.drawable.leftbubble)
                messageViewHolder.textview_left_message.text = comments[position].message
                messageViewHolder.textview_left_message.textSize = 25f
                messageViewHolder.linearylayout_main.gravity = Gravity.LEFT
            }
            val unixTime = comments[position].timestamp as Long
            val date = Date(unixTime)
            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val time = simpleDateFormat.format(date)
            messageViewHolder.textview_left_timestamp.text = time
            messageViewHolder.textview_right_timestamp.text = time
        }

        override fun getItemCount(): Int {
            return comments.size
        }


    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fromleft, R.anim.toright)
    }

}


