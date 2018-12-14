package sogong.korea.yazzikja

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView

class UploadPostActivity : AppCompatActivity() {

    private var imageViewTargetPhoto: ImageView? = null
    private var editTextUploadTitle: EditText? = null
    private var checkBoxUploadPrivate: CheckBox? = null
    private var editTextUploadLocation: EditText? = null
    private var editTextUploadTag: EditText? = null
    private var editTextUploadDescription: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_upload_photo)

        initUploadPostActivity()
    }

    private fun initUploadPostActivity() {
        
    }
}