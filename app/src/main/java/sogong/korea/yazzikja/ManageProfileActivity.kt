package sogong.korea.yazzikja

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_manage_profile.*
import sogong.korea.yazzikja.model.UserModel

class ManageProfileActivity : AppCompatActivity() {

    private var textViewNickname: TextView? = null
    private var textViewLocation: TextView? = null
    private var textViewNumLike: TextView? = null
    private var editTextNickname: EditText? = null
    private var editTextLocation: EditText? = null
    private var editTextIntroduction: EditText? = null
    private var imageButtonPhotoSelect: ImageButton? = null
    private var imageButtonConfirm: Button? = null

    private var imageViewUserPhotoLarge: ImageView? = null
    private var imageViewUserPhoto: ImageView? = null

    private var uid: String? = null

    private val PICK_IMAGE = 1
    private val PICK_FROM_ALBUM = 10

    private var userModel: UserModel? = null
    private var imageUri: Uri? = null
    private var profileImageUri: String? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_manage_profile)

        initManageProfileActivity()

//        manageProfileActivityIntent.putExtra("userNickname", uid)
//        manageProfileActivityIntent.putExtra("userLocation", userInfo?.userLocation)
//        manageProfileActivityIntent.putExtra("userNumlike", userInfo?.userNumLike.toString())
//        manageProfileActivityIntent.putExtra("userProfileIntroduction", userInfo?.userIntroduction)
        var extras: Bundle? = null
        extras = intent.extras
        textViewNickname?.text = extras.getString("userNickname")
        textViewLocation?.text = extras.getString("userLocation")

        val stringNumLike = extras.getString("userNumlike")
        if(stringNumLike.toString().equals("null")) textViewNumLike?.text = "0"
        else textViewNumLike?.text = stringNumLike

//        textViewNumLike?.text = extras.getString("userNumlike")

        editTextNickname?.setText(extras.getString("userNickname"))
        editTextLocation?.setText(extras.getString("userLocaion"))
        editTextIntroduction?.setText(extras.getString("userIntroduction"))

        profileImageUri = extras.getString("userProfileImageUri")

        Glide.with(applicationContext).load(profileImageUri).apply(RequestOptions()).into(imageViewUserPhotoLarge!!)
        Glide.with(applicationContext).load(profileImageUri).apply(RequestOptions().circleCrop()).into(imageViewUserPhoto!!)

        imageButtonPhotoSelect?.setOnClickListener() {
            val albumIntent = Intent(Intent.ACTION_PICK)
            albumIntent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(albumIntent, PICK_FROM_ALBUM)
        }

        imageButtonConfirm?.setOnClickListener() {
            imageButtonConfirm?.setOnClickListener{

                // is this work fine without unknown parameter when upload??

                userModel!!.userNickname = editTextNickname?.text.toString()
                userModel!!.userLocation = editTextLocation?.text.toString()
                userModel!!.userIntroduction = editTextIntroduction?.text.toString()

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

        }
    }

    private fun initManageProfileActivity() {

        textViewNickname = manageprofileactivity_textview_nickname
        textViewLocation = manageprofileactivity_textview_location
        textViewNumLike = manageprofileactivity_textview_num_like

        editTextNickname = manageprofileactivity_edittext_nickname
        editTextLocation = manageprofileactivity_edittext_location
        editTextIntroduction = manageprofileactivity_edittext_introduction

        imageButtonPhotoSelect = manageprofileactivity_photo_select
        imageButtonConfirm = manageprofileactivity_button_confirm

        imageViewUserPhotoLarge = manageprofileactivity_profile_background_image_large
        imageViewUserPhoto = manageprofileactivity_user_photodisplay
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // response for the activity PICK_FROM_ALBUM
        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            imageViewUserPhoto?.setImageURI(data!!.data) // 가운데 뷰를 바꿈
            imageViewUserPhotoLarge?.setImageURI(data!!.data)
            imageUri = data!!.data// 이미지 경로 원본
        }
    }
}