package sogong.korea.yazzikja.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import sogong.korea.yazzikja.R

import java.util.HashMap

class AccountFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        val button = view.findViewById<View>(R.id.fragmentaccount_button_statusmessage) as Button
        button.setOnClickListener { showDialog(view.context) }

        return view
    }

    internal fun showDialog(context: Context) {
        val builder = AlertDialog.Builder(context)

        val layoutInflater = activity!!.layoutInflater
        val view = layoutInflater.inflate(R.layout.dialog_comment, null)
        val editText = view.findViewById<EditText>(R.id.dialogcomment_edittext)
        builder.setView(view).setPositiveButton("OK") { dialog, which ->
            val stringObjectMap = HashMap<String, Any>()
            val uid = FirebaseAuth.getInstance().getCurrentUser()!!.getUid()
            stringObjectMap["statusMessage"] = editText.text.toString()
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(stringObjectMap)
        }.setNegativeButton("Cancel") { dialog, which -> }

        builder.show()
    }
}
