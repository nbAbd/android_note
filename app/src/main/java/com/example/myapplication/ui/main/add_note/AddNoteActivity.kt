package com.example.myapplication.ui.main.add_note

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils.concat
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.App
import com.example.myapplication.BR
import com.example.myapplication.R
import com.example.myapplication.data.local.db.entity.Note
import com.example.myapplication.databinding.ActivityAddNoteBinding
import com.example.myapplication.di.di_utils.ViewModelFactory
import com.example.myapplication.ui.base.BaseMVVMActivity
import com.example.myapplication.ui.camera.CaptureImageActivity
import com.example.myapplication.utils.AppUtils
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import org.xdty.preference.colorpicker.ColorPickerDialog
import java.util.*
import javax.inject.Inject

class AddNoteActivity : BaseMVVMActivity<ActivityAddNoteBinding, AddNoteViewModel>(),
    AddNoteNavigator {

    companion object {
        const val NOTE_ID_KEY = "ID"
        const val CAMERA_REQUEST_CODE = 99

        fun getStartIntent(context: Context, id: Int? = null): Intent {
            val intent = Intent(context, AddNoteActivity::class.java)
            if (id != null) {
                intent.putExtra(NOTE_ID_KEY, id)
            }
            return intent
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory


    private lateinit var mViewModel: AddNoteViewModel

    private lateinit var mBinding: ActivityAddNoteBinding

    private var isAddNote: Boolean = true
    private var defaultColor: Int? = null
    private var noteId: Int? = null
    private var previousColor: String? = null
    private var spannableString: SpannableString? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.setNavigator(this)
        mBinding = viewDataBinding

        setUp()
        checkAction()
        observeCapturedImage()
    }

    private fun setUp() {
        initToolbar(R.id.toolbar, " ")
        setHomeAsUp(true)
    }

    private fun checkAction() {
        if (intent != null && intent.extras?.containsKey(NOTE_ID_KEY) == true) {
            onEditNote()
            isAddNote = false
        } else {
            onAddNewNote()
        }
    }


    private fun validateInputFields() {

        val currentTime = Calendar.getInstance().time.toString().substring(0, 16)
        var title = mBinding.noteTitleEt.text.toString()
        val content = mBinding.noteContentEt.text.toString()

        if (title.trim().isBlank() && content.trim().isBlank()) {
            showToast(R.string.cannot_save_empty_note)
            finish()
        } else {
            if (title.trim().isBlank() && content.trim().isNotBlank()) {
                title = if (content.length <= 16) content.substring(
                    0,
                    content.length
                ) else content.substring(0, 16)
            }
            applyChanges(
                if (mViewModel.capturedImgPathLiveData.value != "") {
                    Note(
                        title,
                        content,
                        mViewModel.capturedImgPathLiveData.value?.toString(),
                        defaultColor.toString(),
                        currentTime
                    )
                } else {
                    Note(
                        title,
                        content,
                        null,
                        defaultColor.toString(),
                        currentTime
                    )
                }
            )
        }
    }

    private fun applyChanges(note: Note) {
        if (isAddNote) {
            mViewModel.insertNote(note)
            showToast(R.string.saved_toast_message_text)
        } else {
            note.id = noteId
            mViewModel.updateNote(note)
            showToast(R.string.updated_toast_message_text)
        }
        finish()
    }

    override fun getViewModel(): AddNoteViewModel {
        mViewModel =
            ViewModelProvider(this, this.viewModelFactory).get(AddNoteViewModel::class.java)
        return mViewModel
    }

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.activity_add_note

    override fun performDependencyInjection() {
        App.get(this).getComponent().inject(this)
    }

    override fun onEditNote() {
        noteId = intent.getIntExtra(NOTE_ID_KEY, -1)
        mViewModel.getNoteById(noteId!!)?.observe(this, {
            if (it != null) {
                populateNote(it)
            }
        })
    }

    private fun observeCapturedImage() {
        mViewModel.capturedImgPathLiveData.observe(this, {
            if (it.toUri().isAbsolute && it != "") {
                mBinding.noteContentEt.editableText.removeSpan(spannableString)
                spannableString = AppUtils.toSpannableString(
                    AppUtils.toRoundedBitmapDrawable(this, it.toUri()),
                    object : ClickableSpan() {
                        override fun onClick(view: View) {
                            onImageSpanClick(view)
                        }
                    }
                )
                val end = mBinding.noteContentEt.text.toString().replace("\n", "")
                mBinding.noteContentEt.setText(concat(end, spannableString))
                mBinding.noteContentEt.movementMethod = LinkMovementMethod.getInstance()
            } else {
                mBinding.noteContentEt.editableText.clearSpans()
                val actualText = mBinding.noteContentEt.text.toString().replace("\n", "")
                mBinding.noteContentEt.setText(actualText)
                spannableString = null
            }
        })
    }

    override fun onAddNewNote() {
        defaultColor = getRandomColor()
        defaultColor?.let { setDefaultColor(it) }
    }

    private fun populateNote(note: Note?) {
        mBinding.noteTitleEt.setText(note?.title)
        mBinding.noteContentEt.setText(note?.content)
        note?.imgUri?.let {
            mViewModel.capturedImgPathLiveData.value = it
        }
        mBinding.toolbar.toolbar.setBackgroundColor(Integer.valueOf(note?.color.toString()))
        mBinding.root.setBackgroundColor(Integer.valueOf(note?.color.toString()))
        defaultColor = note?.color?.toInt()
    }

    private fun getRandomColor(): Int {
        val colorArray = resources.getIntArray(R.array.md_color_array)
        return colorArray.random()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pick_color -> onChooseColorClick()
            R.id.add_photo -> startCamera()
            R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startCamera() {
        startActivityForResult(CaptureImageActivity.getStartIntent(this), CAMERA_REQUEST_CODE)
    }

    override fun onChooseColorClick() {
        val colorPickerDialog = defaultColor?.let {
            ColorPickerDialog.newInstance(
                R.string.color_picker_default_title,
                resources.getIntArray(R.array.md_color_array),
                it,
                4,
                ColorPickerDialog.SIZE_SMALL
            )
        }

        colorPickerDialog?.setOnColorSelectedListener {
            defaultColor = it
            previousColor = it.toString()
            setDefaultColor(it)
        }
        @Suppress("DEPRECATION")
        colorPickerDialog?.show(fragmentManager, "Color dialog")
    }

    private fun setDefaultColor(color: Int) {
        mBinding.toolbar.toolbar.setBackgroundColor(color)
        mBinding.root.setBackgroundColor(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
    }

    fun onImageSpanClick(view: View) {
        val balloon = createBalloon(this) {
            setLayout(R.layout.custom_tooltip_layout)
            setArrowVisible(false)
            setBalloonAnimation(BalloonAnimation.ELASTIC)
            setLifecycleOwner(this@AddNoteActivity)
            setCornerRadiusResource(R.dimen.tooltip_corners_radius)
            setAutoDismissDuration(2000L)
        }
        val deleteImgBtn: ImageView =
            balloon.getContentView().findViewById(R.id.tooltip_delete_button)
        deleteImgBtn.setOnClickListener {
            if (mViewModel.capturedImgPathLiveData.value != "") {
                mViewModel.capturedImgPathLiveData.value?.let { uri ->
                    AppUtils.deleteImageFromUri(uri)
                    mViewModel.capturedImgPathLiveData.value = ""
                }
            }
            balloon.dismiss()
        }


        val cameraImgBtn: ImageView =
            balloon.getContentView().findViewById(R.id.tooltip_camera_button)
        cameraImgBtn.setOnClickListener {
            startActivityForResult(CaptureImageActivity.getStartIntent(this), CAMERA_REQUEST_CODE)
            balloon.dismiss()
        }
        balloon.show(view)
    }

    override fun onBackPressed() {
        validateInputFields()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (mViewModel.capturedImgPathLiveData.value != "") {
                    mViewModel.capturedImgPathLiveData.value?.let {
                        AppUtils.deleteImageFromUri(it)
                        mViewModel.capturedImgPathLiveData.value = ""
                    }
                }
                mViewModel.capturedImgPathLiveData.value =
                    data?.extras?.get("imgUri").toString()
            }
        }
    }
}