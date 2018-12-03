package sogong.korea.yazzikja.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.UserManager
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

import java.util.ArrayList

import sogong.korea.yazzikja.R
import sogong.korea.yazzikja.chat.MessageActivity
import sogong.korea.yazzikja.model.UserModel

class PeopleFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_people, container, false)
        val recyclerView = view.findViewById<View>(R.id.peoplefragment_recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        recyclerView.adapter = PeopleFragmentRecyclerViewAdapter()

        return view
    }

    internal inner class PeopleFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var userModels: MutableList<UserModel>

        init {
            userModels = ArrayList()
            val myUid = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userModels.clear()
                    for (snapshot in dataSnapshot.children) {
                        val userModel = snapshot.getValue(UserModel::class.java)

                        if (userModel!!.uid == myUid) {
                            continue
                        }
                        userModels.add(userModel)
                    }
                    notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, i: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)

            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

            Glide.with(viewHolder.itemView.context).load(userModels[position].profileImageUri)
                .apply(RequestOptions().circleCrop()).into((viewHolder as CustomViewHolder).imageView)
            viewHolder.textView.text = userModels[position].userNickname

            viewHolder.itemView.setOnClickListener { view ->
                val messageIntent = Intent(view.context, MessageActivity::class.java)
                messageIntent.putExtra("destinationUid", userModels[position].uid)
                var activityOptions: ActivityOptions? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    activityOptions = ActivityOptions.makeCustomAnimation(view.context, R.anim.fromright, R.anim.toleft)
                    startActivity(messageIntent, activityOptions!!.toBundle())
                }
            }
            if (userModels[position].statusMessage != null) {
                viewHolder.textViewStatusMessage.text = userModels[position].statusMessage
            } else {
                viewHolder.textViewStatusMessage.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int {
            return userModels.size
        }

        private inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView
            var textView: TextView
            var textViewStatusMessage: TextView

            init {
                imageView = itemView.findViewById<View>(R.id.frienditem_imageview) as ImageView
                textView = itemView.findViewById<View>(R.id.frienditem_textview) as TextView
                textViewStatusMessage = itemView.findViewById<View>(R.id.itemfriend_textview_statusmessage) as TextView
            }
        }

    }
}
