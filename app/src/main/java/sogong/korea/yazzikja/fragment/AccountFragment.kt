package sogong.korea.yazzikja.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import sogong.korea.yazzikja.R
import sogong.korea.yazzikja.model.UserModel




class AccountFragment : Fragment() {

    private var textViewNickname: TextView? = null
    private var textViewLocation: TextView ? = null
    private var textViewNumLike: TextView ? = null
    private var editTextNickname: EditText ? = null
    private var editTextLocation: EditText ? = null
    private var editTextIntroduction: EditText ? = null
    private var imageButtonPhotoSelect: ImageButton? = null
    private var imageButtonConfirm: Button? = null

    private var imageViewUserPhotoLarge: ImageView? = null
    private var imageViewUserPhoto: ImageView? = null

    private var uid: String ? = null

    private val PICK_IMAGE = 1
    private val PICK_FROM_ALBUM = 10

    private var userModel: UserModel ? = null
    private var imageUri: Uri? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        uid = FirebaseAuth.getInstance().currentUser!!.uid

        textViewNickname = view.findViewById<View>(R.id.accountfragment_textview_nickname) as TextView
        textViewLocation = view.findViewById<View>(R.id.accountfragment_textview_location) as TextView
        textViewNumLike = view.findViewById<View>(R.id.accountfragment_textview_num_like) as TextView

        editTextNickname = view.findViewById(R.id.accountfragment_edittext_nickname) as EditText
        editTextLocation = view.findViewById(R.id.accountfragment_edittext_locaiton) as EditText
        editTextIntroduction = view.findViewById(R.id.accountfragment_edittext_introduction) as EditText

        imageButtonPhotoSelect = view.findViewById<View>(R.id.accountfragment_photo_select) as ImageButton
        imageButtonConfirm = view.findViewById<View>(R.id.accountfragment_button_confirm) as Button

        imageViewUserPhotoLarge = view.findViewById<View>(R.id.accountfragment_profile_background_image_large) as ImageView
        imageViewUserPhoto = view.findViewById<View> (R.id.accountfragment_user_photodisplay) as ImageView

//        val myUid = FirebaseAuth.getInstance().currentUser!!.uid


        FirebaseDatabase.getInstance().reference.child("users/$uid").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userModel = dataSnapshot.getValue(UserModel::class.java)
                textViewNickname?.text = userModel?.userNickname
                textViewLocation?.text = userModel?.userLocation
                if(userModel?.userNumLike.toString().equals("null")) textViewNumLike?.text = "0"
                else textViewNumLike?.text = userModel?.userNumLike.toString()

                editTextNickname?.setText(userModel?.userNickname.toString())
                editTextLocation?.setText(userModel?.userLocation.toString())
                editTextIntroduction?.setText(userModel?.userIntroduction.toString())

            /*    GlideApp.with(inflater.context)
                    .load(userModel?.profileImageUri)
                    .placeholder(R.drawable.profile_background)
                    .fitCenter()
                    .into(imageViewUserPhoto);*/

                Glide.with(inflater?.context.applicationContext).load(userModel?.profileImageUri).apply(RequestOptions().circleCrop()).into(imageViewUserPhoto!!)
                Glide.with(inflater?.context.applicationContext).load(userModel?.profileImageUri).apply(RequestOptions()).into(imageViewUserPhotoLarge!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
//        imageButtonPhotoSelect?.setOnClickListener{
        imageViewUserPhoto?.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, PICK_FROM_ALBUM)
/*                val galleryIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, PICK_IMAGE)*/
            }
 //       }
        imageButtonConfirm?.setOnClickListener{
//            val userModel = UserModel()

            userModel!!.userNickname = editTextNickname?.text.toString()
            userModel!!.userLocation = editTextLocation?.text.toString()
            userModel!!.userIntroduction = editTextIntroduction?.text.toString()
//            userModel.profileImageUri = imageUrl
//            userModel!!.uid = FirebaseAuth.getInstance().getCurrentUser()!!.getUid() // already got this before
//            userModel.photographer = photographerCheckbox!!.isChecked.toString()
            val profileImageRef =
                FirebaseStorage.getInstance().getReference().child("userImages").child(userModel!!.uid!!)
            if(imageUri==null) {
                FirebaseDatabase.getInstance().getReference().child("users")
                    .child(userModel!!.uid!!).setValue(userModel)
            }
            else {
                profileImageRef.putFile(imageUri!!)
                    .addOnCompleteListener(object: OnCompleteListener<UploadTask.TaskSnapshot> {
                        override fun onComplete(task: Task<UploadTask.TaskSnapshot>) {
                            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            profileImageRef.getDownloadUrl()
                                .addOnSuccessListener(object: OnSuccessListener<Uri> {
                                    override fun onSuccess(downloadUri : Uri) {
                                        var downloadImageUri : String? = null
                                        downloadImageUri = downloadUri.toString()
                                        userModel!!.profileImageUri = downloadImageUri

                                        FirebaseDatabase.getInstance().getReference().child("users")
                                            .child(userModel!!.uid!!).setValue(userModel)
                                    }
                                })
                        }
                    })
            }

        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            imageViewUserPhoto?.setImageURI(data!!.data) // 가운데 뷰를 바꿈
            imageUri = data!!.data// 이미지 경로 원본
        }
    }

}
