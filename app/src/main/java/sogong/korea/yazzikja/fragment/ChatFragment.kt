package sogong.korea.yazzikja.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import sogong.korea.yazzikja.R
import sogong.korea.yazzikja.chat.MessageActivity
import sogong.korea.yazzikja.model.ChatModel
import sogong.korea.yazzikja.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd hh:mm")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        val recyclerView = view.findViewById<View>(R.id.chatfragment_recyclerview) as RecyclerView
        recyclerView.adapter = ChatRecyclerViewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)

        return view
    }

    internal inner class ChatRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val uid: String
        private val chatModels = ArrayList<ChatModel>()
        private val destinationUsers = ArrayList<String>()

        init {
            uid = FirebaseAuth.getInstance().currentUser!!.uid

            FirebaseDatabase.getInstance().reference.child("chatrooms").orderByChild("users/$uid")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        chatModels.clear()
                        for (item in dataSnapshot.children) {
                            chatModels.add(item.getValue(ChatModel::class.java)!!)
                        }
                        notifyDataSetChanged()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
        }


        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_chat, viewGroup, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
            val customViewHolder = viewHolder as CustomViewHolder
            var destinationUid: String? = null

            for (user in chatModels[i].users!!.keys) {
                if (user != uid) {
                    destinationUid = user
                    destinationUsers.add(destinationUid)
                }
            }
            FirebaseDatabase.getInstance().reference.child("users").child(destinationUid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userModel = dataSnapshot.getValue(UserModel::class.java)
                        Glide.with(customViewHolder.itemView.context).load(userModel!!.profileImageUri)
                            .apply(RequestOptions().circleCrop()).into(customViewHolder.imageView)

                        customViewHolder.textview_title.text = userModel.userNickname
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

            val commentMap = TreeMap<String, ChatModel.Comment>(Collections.reverseOrder<Any>())
            commentMap.putAll(chatModels[i].comments!!)
            val lastMessageKey = commentMap.keys.toTypedArray()[0]
            customViewHolder.textview_last_message.text = chatModels[i].comments!![lastMessageKey]!!.message

            customViewHolder.itemView.setOnClickListener { v ->
                val messageIntent = Intent(v.context, MessageActivity::class.java)
                messageIntent.putExtra("destinationUid", destinationUsers[i])
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    val activityOptions =
                        ActivityOptions.makeCustomAnimation(v.context, R.anim.fromright, R.anim.toleft)
                    startActivity(messageIntent, activityOptions.toBundle())

                }
            }
            // Date Format
            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val unixTime = chatModels[i].comments!![lastMessageKey]!!.timestamp as Long
            val date = Date(unixTime)
            customViewHolder.textview_timestamp.text = simpleDateFormat.format(date)
        }

        override fun getItemCount(): Int {
            return chatModels.size
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView
            var textview_title: TextView
            var textview_last_message: TextView
            var textview_timestamp: TextView

            init {

                imageView = view.findViewById<View>(R.id.chatitem_imageview) as ImageView
                textview_title = view.findViewById<View>(R.id.chatitem_textview_title) as TextView
                textview_last_message = view.findViewById<View>(R.id.chatitem_textview_lastmessage) as TextView
                textview_timestamp = view.findViewById<View>(R.id.chatitem_textview_timestamp) as TextView
            }
        }
    }
}