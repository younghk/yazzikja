// home activity

package sogong.korea.yazzikja

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult

import java.util.HashMap

import sogong.korea.yazzikja.fragment.AccountFragment
import sogong.korea.yazzikja.fragment.ChatFragment
import sogong.korea.yazzikja.fragment.PeopleFragment

import android.view.View

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportFragmentManager.beginTransaction().replace(R.id.homeactivity_framelayout, PeopleFragment()).commit()

        val bottomNavigationView = findViewById<View>(R.id.homeactivity_bottomnavigationview) as BottomNavigationView

        bottomNavigationView.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_home -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.homeactivity_framelayout, PeopleFragment()).commit()
                        return true
                    }
                    R.id.action_search -> return true
                    R.id.action_chat -> {
                        supportFragmentManager.beginTransaction().replace(R.id.homeactivity_framelayout, ChatFragment())
                            .commit()
                        return true
                    }
                    R.id.action_account -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.homeactivity_framelayout, AccountFragment()).commit()
                        return true
                    }
                }
                return false
            }
        })
        passPushTokenToServer()
    }

    internal fun passPushTokenToServer() {
        val uid = FirebaseAuth.getInstance().getCurrentUser()!!.getUid()
        val token = arrayOfNulls<String>(1)
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnSuccessListener(object : OnSuccessListener<InstanceIdResult> {
                override fun onSuccess(instanceIdResult: InstanceIdResult) {
                    token[0] = instanceIdResult.getToken()
                    val map = HashMap<String, Any>()
                    map.plus(Pair("pushToken",token[0]))

                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map)
                }
            })
    }
}
