// 11.13
// login possible using firebase without hashing

package sogong.korea.yazzikja

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import sogong.korea.yazzikja.R.id.*


class SigninActivity : AppCompatActivity() {

    private var email: EditText? = null
    private var userPassword: EditText? = null
    private var photographer: CheckBox? = null
    private var signinButton: Button? = null
    private var signupButton: TextView? = null


    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var mFirebaseAuth: FirebaseAuth? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // remove notificationbar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_signin)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseAuth!!.signOut()
        val splash_background = mFirebaseRemoteConfig!!.getString(getString(R.string.rc_color))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor(splash_background)
        }



        email = findViewById(R.id.signinactivity_edittext_email) as EditText
        userPassword = findViewById(R.id.signinactivity_edittext_password) as EditText
        photographer = findViewById(R.id.signinactivity_checkbox) as CheckBox
        signinButton = findViewById<View>(R.id.signinactivity_button_signin) as Button
        signupButton = findViewById<View>(R.id.signinactivity_button_signup) as TextView

/*        loginButton!!.setBackgroundColor(Color.parseColor(splash_background))
        registerButton!!.setBackgroundColor(Color.parseColor(splash_background))*/

        signinButton!!.setOnClickListener { this.onSigninButtonClick(it)}
        signupButton!!.setOnClickListener { this.onSignupButtonClick(it) }

        authStateListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                val user = firebaseAuth.getCurrentUser()
                if (user != null) {
                    val homeIntent = Intent(this@SigninActivity, HomeActivity::class.java)
                    startActivity(homeIntent)
                    finish()
                } else {

                }
            }
        }
    }

    internal fun loginEvent() {
        mFirebaseAuth!!.signInWithEmailAndPassword(email!!.text.toString(), userPassword!!.text.toString())
            .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                override fun onComplete(task: Task<AuthResult>) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this@SigninActivity, task.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    private fun onSigninButtonClick(v: View) {

        loginEvent()

    }

    private fun onSignupButtonClick(v: View) {
        val signupIntent = Intent(this, RegisterActivity::class.java)
        startActivity(signupIntent)
    }

    override fun onStart() {
        super.onStart()
        authStateListener?.let { mFirebaseAuth!!.addAuthStateListener(it) }
    }

    override fun onStop() {
        super.onStop()
        authStateListener?.let { mFirebaseAuth!!.removeAuthStateListener(it) }
    }
}
