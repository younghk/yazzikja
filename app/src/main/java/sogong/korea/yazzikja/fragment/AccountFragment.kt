package sogong.korea.yazzikja.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_account.*
import sogong.korea.yazzikja.R
import sogong.korea.yazzikja.model.UserModel


class AccountFragment : Fragment() {

    private var textViewNickname: TextView? = null
    private var textViewLocation: TextView ? = null
    private var textViewNumLike: TextView ? = null
    private var editTextNickname: EditText ? = null
    private var editTextLocation: EditText ? = null
    private var editTextIntroduction: EditText ? = null
    private var imageButtonPhoto: ImageButton? = null
    private var imageButtonConfirm: ImageButton? = null

    private var imageViewUserPhotoLarge: ImageView? = null
    private var imageViewUserPhoto: ImageView? = null

    private var uid: String ? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        uid = FirebaseAuth.getInstance().currentUser!!.uid

        textViewNickname = view.findViewById<View>(R.id.accountfragment_textview_nickname) as TextView
        textViewLocation = view.findViewById<View>(R.id.accountfragment_textview_location) as TextView
        textViewNumLike = view.findViewById<View>(R.id.accountfragment_textview_num_like) as TextView
        editTextNickname = accountfragment_edittext_nickname
        editTextLocation = accountfragment_edittext_introduction
        editTextIntroduction = accountfragment_edittext_introduction
        imageButtonPhoto = accountfragment_button_confirm
        imageButtonConfirm = accountfragment_button_confirm

        imageViewUserPhotoLarge = view.findViewById<View>(R.id.accountfragment_profile_background_image_large) as ImageView
        imageViewUserPhoto = accountfragment_user_photodisplay

//        val myUid = FirebaseAuth.getInstance().currentUser!!.uid
        var userModel: UserModel ? = null

        FirebaseDatabase.getInstance().reference.child("users/$uid").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userModel = dataSnapshot.getValue(UserModel::class.java)
                textViewNickname!!.text = userModel?.userNickname
                textViewLocation!!.text = userModel?.userLocation
                textViewNumLike!!.text = userModel?.userNumLike.toString()
                editTextNickname!!.setText(userModel?.userNickname)
                editTextLocation!!.setText(userModel?.userLocation)
                editTextIntroduction!!.setText(userModel?.userIntroduction)
            /*    GlideApp.with(inflater.context)
                    .load(userModel?.profileImageUri)
                    .placeholder(R.drawable.profile_background)
                    .fitCenter()
                    .into(imageViewUserPhoto);*/

                Glide.with(inflater.context).load(userModel?.profileImageUri).apply(RequestOptions().circleCrop()).into(imageViewUserPhoto!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })




        imageButtonConfirm!!.setOnClickListener{

        }

        return view
    }


}
