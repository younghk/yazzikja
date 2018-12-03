package sogong.korea.yazzikja

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.LinearLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    private var linearLayout: LinearLayout? = null
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        linearLayout = splashactivity_linearlayout

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        mFirebaseRemoteConfig!!.setConfigSettings(configSettings)
        mFirebaseRemoteConfig!!.setDefaults(R.xml.default_config)
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        mFirebaseRemoteConfig!!.fetch(0)
            .addOnCompleteListener(this, object : OnCompleteListener<Void> {
                override fun onComplete(task: Task<Void>) {
                    if (task.isSuccessful()) {

                        // After config data is successfully fetched, it must be activated before newly fetched
                        // values are returned.
                        mFirebaseRemoteConfig!!.activateFetched()
                    } else {

                    }
                    displayMessage()
                }
            })

    }

    internal fun displayMessage() {
        val splash_background = mFirebaseRemoteConfig!!.getString("splash_background")
        val caps = mFirebaseRemoteConfig!!.getBoolean("splash_message_caps")
        val splash_message = mFirebaseRemoteConfig!!.getString("splash_message")

        linearLayout!!.setBackgroundColor(Color.parseColor(splash_background))

        if (caps) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(splash_message).setPositiveButton("확인",
                DialogInterface.OnClickListener { dialogInterface, i -> finish() })

            builder.create().show()

        } else {
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
        }

    }
}
