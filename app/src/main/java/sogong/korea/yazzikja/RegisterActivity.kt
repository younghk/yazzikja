package sogong.korea.yazzikja

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.firebase.ui.auth.data.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import sogong.korea.yazzikja.model.UserModel

import android.R.attr.data
import android.app.Activity
import android.view.WindowManager
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var email: EditText? = null
    private var nickname: EditText? = null
    private var password: EditText? = null
    private var passwordConfirm: EditText? = null
    private var photographerCheckbox: CheckBox? = null
    private var registerButton: Button? = null
    private var profileImage: ImageView? = null
    private var imageUri: Uri? = null

    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null

    private var splash_background: String? = null

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove notificationbar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_register)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        splash_background = mFirebaseRemoteConfig!!.getString(getString(R.string.rc_color))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor(splash_background)
        }

        email = findViewById<View>(R.id.registeractivity_email) as EditText
        nickname = findViewById<View>(R.id.registeractivity_nickname) as EditText
        password = findViewById<View>(R.id.registeractivity_password) as EditText
        passwordConfirm = findViewById<View>(R.id.registeractivity_password_confirm) as EditText
        photographerCheckbox = registeractivity_checkbox

        registerButton = findViewById<View>(R.id.registeractivity_register_button) as Button
        registerButton!!.setBackgroundColor(Color.parseColor(splash_background))

 /*       profileImage = findViewById<View>(R.id.registeractivity_profile_image) as ImageView
        profileImage!!.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, PICK_FROM_ALBUM)
        }*/


        registerButton!!.setOnClickListener {
            if (email!!.text.toString() == null || nickname!!.text.toString() == null || password!!.text.toString() == null) {
                Toast.makeText(this@RegisterActivity, "Please enter the information correctly", LENGTH_SHORT).show()
            }
            else if (password!!.text.toString() == passwordConfirm!!.text.toString()) {
                //mAuth = FirebaseAuth.getInstance()
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email!!.text.toString(), password!!.text.toString())
                    .addOnCompleteListener(this@RegisterActivity, object : OnCompleteListener<AuthResult> {
                        override fun onComplete(task: Task<AuthResult>) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                //                                        Log.d("TAG", "createUserWithEmail:success");
                                val uid = task.getResult()!!.getUser().getUid()
                                val userProfileChangeRequest =
                                    UserProfileChangeRequest.Builder().setDisplayName(nickname!!.text.toString())
                                        .build()

                                task.getResult()!!.getUser().updateProfile(userProfileChangeRequest)
                                // Registartion information without profile image
/*                                val userModel = UserModel()
                                userModel.userNickname = nickname!!.text.toString()
                                userModel.uid = FirebaseAuth.getInstance().currentUser!!.uid
                                if(photographerCheckbox!!.isChecked) {
                                    userModel.photographer = true;
                                } else {
                                    userModel.photographer = false;
                                }

                                FirebaseDatabase.getInstance().getReference().child("users")
                                    .child(uid).setValue(userModel)
                                this@RegisterActivity.finish()*/


                                // Registration information with profile image
                                val profileImageRef =
                                    FirebaseStorage.getInstance().getReference().child("userImages").child(uid)
                                if (imageUri == null) {
                                    imageUri =
                                            Uri.parse("android.resource://" + packageName + "/" + R.drawable.baseline_account_circle_black_24)
                                }
                                profileImageRef.putFile(imageUri!!)
                                    .addOnCompleteListener(object : OnCompleteListener<UploadTask.TaskSnapshot> {

                                        override fun onComplete(task: Task<UploadTask.TaskSnapshot>) {
                                            //                                                @SuppressWarnings("VisibleForTest")
                                            val addOnSuccessListener = profileImageRef.getDownloadUrl()
                                                .addOnSuccessListener(object : OnSuccessListener<Uri> {
                                                    override fun onSuccess(downloadUrl: Uri) {
                                                        //do something with downloadurl
                                                        val imageUrl: String
                                                        imageUrl = downloadUrl.toString()

                                                        val userModel = UserModel()
                                                        userModel.userNickname = nickname!!.text.toString()
                                                        userModel.profileImageUri = imageUrl
                                                        userModel.uid =
                                                                FirebaseAuth.getInstance().getCurrentUser()!!.getUid()
                                                        userModel.photographer = photographerCheckbox!!.isChecked.toString()
                                                        FirebaseDatabase.getInstance().getReference().child("users")
                                                            .child(uid).setValue(userModel)
                                                        this@RegisterActivity.finish()
                                                    }
                                                })
                                           /* Toast.makeText(
                                                this@RegisterActivity,
                                                task.getResult().toString(),
                                                LENGTH_SHORT
                                            ).show()*/
                                        }

                                    })

                                //                                        UserModel userModel = new UserModel();
                                //                                        userModel.userNickname = nickname.getText().toString();

                                //                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.getException())
                                Toast.makeText(
                                    this@RegisterActivity, "Authentication failed.",
                                    LENGTH_SHORT
                                ).show()
                            }
                            //Toast.makeText(RegisterActivity.this,"done..?",LENGTH_SHORT).show();
                            //                                    String uid = task.getResult().getUser().getUid();
                        }
                    })
            } else {
                Toast.makeText(this@RegisterActivity, "Please check the password", LENGTH_SHORT).show()
            }
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            imageUri = data!!.data
            profileImage!!.setImageURI(imageUri)
        }
    }

    companion object {

        private val PICK_FROM_ALBUM = 10
    }
}
