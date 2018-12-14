package sogong.korea.yazzikja.fragment

import android.content.Intent
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
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.item_profile_feed.*
import sogong.korea.yazzikja.ManageProfileActivity
import sogong.korea.yazzikja.R
import sogong.korea.yazzikja.UploadPostActivity
import sogong.korea.yazzikja.model.ImageModel
import sogong.korea.yazzikja.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd hh:mm")

    private var uid : String ? = null
    private var userInfo : UserModel ? = null

    private var textViewNickname : TextView ? = null
    private var textViewLocation : TextView ? = null
    private var imageViewProfileImageBackground : ImageView? = null
    private var textViewNumlike : TextView ? = null
    private var textViewProfileDescription : TextView ? = null
    private var imageViewUploadPost : ImageView ? = null
    private var imageViewEditProfile : ImageView ? = null

    private var textViewLocation1 : TextView ? = null
    private var textViewLocation2 : TextView ? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        textViewNickname = profilefragment_textview_nickname
        textViewLocation = profilefragment_textview_location
        imageViewProfileImageBackground = profilefragment_imageview_profile_image_background
        textViewNumlike = profilefragment_textview_numlike
        textViewProfileDescription = profilefragment_textview_profile_description
        imageViewUploadPost = profilefragment_imageview_upload_post
        imageViewEditProfile = profilefragment_imageview_edit_profile

        textViewLocation1 = profilefragment_textview_location1
        textViewLocation2 = profilefragment_textview_location2

        val recyclerView = view.findViewById<View>(R.id.profilefragment_recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        recyclerView.adapter = ProfileFragmentRecyclerViewAdapter()

        profileFragmentInit()

        textViewNickname?.text = userInfo?.userNickname
        textViewLocation?.text = userInfo?.userLocation

        textViewNumlike?.text = userInfo?.userNumLike.toString()
        textViewProfileDescription?.text = userInfo?.userIntroduction

        Glide.with(inflater?.context.applicationContext).load(userInfo?.profileImageUri).apply(RequestOptions().circleCrop()).into(imageViewProfileImageBackground!!)

        imageViewEditProfile?.setOnClickListener() {
            val manageProfileActivityIntent = Intent(this.context, ManageProfileActivity::class.java)

            manageProfileActivityIntent.putExtra("userNickname", uid)
            manageProfileActivityIntent.putExtra("userLocation", userInfo?.userLocation)
            manageProfileActivityIntent.putExtra("userNumlike", userInfo?.userNumLike.toString())
            manageProfileActivityIntent.putExtra("userProfileIntroduction", userInfo?.userIntroduction)
            manageProfileActivityIntent.putExtra("userProfileImageUri", userInfo?.profileImageUri)
            startActivity(manageProfileActivityIntent)

        }

        imageViewUploadPost?.setOnClickListener() {
            val uploadPostActivityIntent = Intent(this.context, UploadPostActivity::class.java)
        }

        return view
    }


    fun profileFragmentInit() {
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseDatabase.getInstance().reference.child("users").child("$uid").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userInfo = dataSnapshot.getValue(UserModel::class.java)

            }
        })
    }

    internal inner class ProfileFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> () {

//        private val userModels = ArrayList<ChatModel>()
        private val imageModels = ArrayList<ImageModel>()
        init {
            FirebaseDatabase.getInstance().reference.child("images").orderByChild("users/$uid")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(dataSnapshot : DataSnapshot) {
                        imageModels.clear()
                        for(item in dataSnapshot.children) {
                            if(item.child("userId").value!!.equals(uid)) {
                                imageModels.add(item.getValue(ImageModel::class.java)!!)
                            }
                        }
                    }
                })
/*            FirebaseDatabase.getInstance().reference.child("users").orderByChild("users/$uid")
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
                })*/
        }

        override fun getItemCount(): Int {
            return imageModels.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            val customViewHolder = p0 as ProfileFragment.ProfileFragmentRecyclerViewAdapter.CustomViewHolder

            customViewHolder.textViewPostDescription.text = imageModels[p1].explain
            customViewHolder.textViewPostPhotoNumLike.text = imageModels[p1].favoriteCount.toString()

            Glide.with(customViewHolder.itemView.context).load(imageModels!![p1].imageUrl)
                .apply(RequestOptions()).into(customViewHolder.imageViewPostPhoto)

            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val unixTime = imageModels[p1].timestamp as Long
            val date = Date(unixTime)
            customViewHolder.textViewPostDate.text = simpleDateFormat.format(date)
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_profile_feed, p0, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var textViewPostDate: TextView
            var imageViewPostPhoto: ImageView
            var textViewPostPhotoNumLike: TextView
            var textViewPostTime: TextView
            var textViewPostTitle: TextView
            var textViewPostDescription: TextView


            init {
                textViewPostDate = profilefeeditem_textview_postdate
                imageViewPostPhoto = profilefeeditem_imageview_postphoto
                textViewPostPhotoNumLike = profilefeeditem_textview_postphoto_numlike
                textViewPostTime = profilefeeditem_textview_posttime
                textViewPostTitle = profilefeeditem_textview_posttitle
                textViewPostDescription = profilefeeditem_textview_postdescription
            }
        }
    }
}