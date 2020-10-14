package com.example.myapplication.ui.main.view_note

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.example.myapplication.App
import com.example.myapplication.BR
import com.example.myapplication.R
import com.example.myapplication.data.local.db.entity.Note
import com.example.myapplication.databinding.ActivityViewNoteBinding
import com.example.myapplication.di.di_utils.ViewModelFactory
import com.example.myapplication.ui.base.BaseMVVMActivity
import com.example.myapplication.ui.main.add_note.AddNoteActivity
import com.example.myapplication.utils.AppUtils
import javax.inject.Inject

class ViewNoteActivity : BaseMVVMActivity<ActivityViewNoteBinding, ViewNoteViewModel>(),
    ViewNoteNavigator {

    companion object {
        private const val NOTE_ID_KEY = "NOTE"

        fun getStartIntent(context: Context, noteId: Int): Intent {
            val intent = Intent(context, ViewNoteActivity::class.java)
            intent.putExtra(NOTE_ID_KEY, noteId)
            return intent
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var mViewModel: ViewNoteViewModel

    private lateinit var mBinding: ActivityViewNoteBinding

    private var mNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding
        mViewModel.setNavigator(this)

        setUp()
        mBinding.viewFab.setOnClickListener {
            onEditClick()
        }
    }

    private fun setUp() {
        initToolbar(R.id.toolbar, "")
        fetchNote()
    }

    private fun fetchNote() {
        val noteId = intent.getIntExtra(NOTE_ID_KEY, -1)
        mViewModel.getNoteById(noteId).observe(this, {
            mNote = it
            populateData(it)
        })
    }

    private fun populateData(note: Note?) {
        mBinding.noteTitleTv.text = note?.title ?: ""
        mBinding.noteDateTv.text = note?.date ?: ""
        mBinding.viewScreenContent.text = note?.content ?: ""
        note?.imgUri?.let {
            mBinding.viewScreenContent.append(
                AppUtils.toSpannableString(
                    AppUtils.toRoundedBitmapDrawable(this, Uri.parse(it)),
                    null
                )
            )
        }
        val color = note?.color?.toInt()
        color?.let { setDefaultColor(it) }
    }

    private fun setDefaultColor(color: Int) {
        mBinding.root.setBackgroundColor(color)
        mBinding.toolbar.toolbar.setBackgroundColor(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> onBackPressed()
            R.id.view_note_delete -> showDeleteDialog()
            R.id.view_note_share -> shareNote()
            R.id.view_note_copy_clipboard -> copyToClipBoard()
            R.id.view_note_notify -> buildNotification()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog() {
        val dialog = MaterialDialog(this)
            .message(R.string.dialog_message)
            .cancelOnTouchOutside(false)
            .positiveButton(R.string.yes) { deleteNote() }
            .negativeButton(R.string.cancel) {
                it.dismiss()
            }
        if (!isFinishing) dialog.show()
    }

    private fun shareNote() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, mNote?.title + "\n" + mNote?.content)
        intent.type = "text/plain"
        startActivity(intent)
    }

    private fun buildNotification() {
        val builder: NotificationCompat.Builder
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "demo.channel.id", "Demo notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(false)
            nm.createNotificationChannel(notificationChannel)
            builder = NotificationCompat.Builder(this, "demo.channel.id")
                .setSmallIcon(R.drawable.ic_note)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_note))
                .setContentTitle(mNote?.title)
                .setContentText(mNote?.content)
        } else {
            @Suppress("DEPRECATION")
            builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_note)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_note))
                .setContentTitle(mNote?.title)
                .setContentText(mNote?.content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mNote?.content))
        }
        nm.notify(0, builder.build())
    }

    private fun copyToClipBoard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData =
            ClipData.newPlainText(
                "Копированный тектс",
                mNote?.title + "\n" + mNote?.content + "\n" + mNote?.date
            )
        clipboard.setPrimaryClip(clipData)
        showToast(R.string.copied_to_clipboard)
    }

    private fun deleteNote() {
        mNote?.imgUri?.let {
            AppUtils.deleteImageFromUri(it)
        }
        mNote?.let { mViewModel.deleteNote(it) }
        showToast(R.string.deleted)
        supportFinishAfterTransition()
    }


    override fun getViewModel(): ViewNoteViewModel {
        mViewModel =
            ViewModelProvider(this, this.viewModelFactory).get(ViewNoteViewModel::class.java)
        return mViewModel
    }

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.activity_view_note

    override fun performDependencyInjection() {
        App.get(this).getComponent().inject(this)
    }

    override fun onEditClick() {
        startActivity(AddNoteActivity.getStartIntent(this, mNote?.id ?: -1))
        Log.d("NOTE ID ------> ", mNote?.id.toString())
    }
}