package com.example.myapplication.ui.main.add_note

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.example.myapplication.App
import com.example.myapplication.BR
import com.example.myapplication.R
import com.example.myapplication.data.local.db.entity.Note
import com.example.myapplication.databinding.ActivityAddNoteBinding
import com.example.myapplication.di.di_utils.ViewModelFactory
import com.example.myapplication.ui.base.BaseMVVMActivity
import org.xdty.preference.colorpicker.ColorPickerDialog
import java.util.*
import javax.inject.Inject

class AddNoteActivity : BaseMVVMActivity<ActivityAddNoteBinding, AddNoteViewModel>(),
    AddNoteNavigator {

    companion object {
        const val NOTE_ID_KEY = "ID"

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


    lateinit var mViewModel: AddNoteViewModel

    lateinit var mBinding: ActivityAddNoteBinding

    var isAddNote: Boolean = true
    var defaultColor: Int? = null
    var noteId: Int? = null
    var previousColor: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.setNavigator(this)
        mBinding = viewDataBinding

        setUp()
        checkAction()
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
            applyChanges(Note(title, content, defaultColor.toString(), currentTime))
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
        mViewModel.getNoteById(noteId!!)?.observe(this, Observer {
            if (it != null) {
                populateNote(it)
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
        mBinding.toolbar.setBackgroundColor(Integer.valueOf(note?.color.toString()))
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
            R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
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
        colorPickerDialog?.show(fragmentManager, "Color dialog")
    }

    private fun setDefaultColor(color: Int) {
        mBinding.toolbar.setBackgroundColor(color)
        mBinding.root.setBackgroundColor(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
    }

    override fun onBackPressed() {
        validateInputFields()
    }

}