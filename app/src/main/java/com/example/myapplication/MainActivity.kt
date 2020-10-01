package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myapplication.data.local.AppPreferencesHelper
import com.example.myapplication.data.local.db.entity.Note
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.di.di_utils.ViewModelFactory
import com.example.myapplication.ui.adapter.NotesAdapter
import com.example.myapplication.ui.base.BaseMVVMActivity
import com.example.myapplication.ui.main.MainActivityNavigator
import com.example.myapplication.ui.main.MainActivityViewModel
import com.example.myapplication.ui.main.add_note.AddNoteActivity
import com.example.myapplication.ui.main.view_note.ViewNoteActivity
import com.example.myapplication.utils.MyItemDetailsLookup
import com.example.myapplication.utils.MyItemKeyProvider
import javax.inject.Inject

class MainActivity : BaseMVVMActivity<ActivityMainBinding, MainActivityViewModel>(),
    MainActivityNavigator, NotesAdapter.OnNoteItemClickListener, ActionMode.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var mViewModel: MainActivityViewModel

    @Inject
    lateinit var mPref: AppPreferencesHelper

    @Inject
    lateinit var mAdapter: NotesAdapter

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mCustomMenu: Menu
    private var mTracker: SelectionTracker<Long>? = null
    private var selectedNoteIds: List<Int>? = null
    private var mActionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding
        mViewModel.setNavigator(this)

        setUp()
        setUpRecyclerView()
        fetchNotes()
        setUpAdapter()
        mBinding.newNoteFab.setOnClickListener {
            startActivity(AddNoteActivity.getStartIntent(this))
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setUp() {
        initToolbar(R.id.toolbar, R.string.notes)
        setHomeAsUp(false)
    }

    private fun setUpRecyclerView() {
        if (!mPref.isGrid()) {
            mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        } else {
            mBinding.recyclerView.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
        mBinding.recyclerView.scrollToPosition(0)
    }

    private fun setUpAdapter() {
        mBinding.recyclerView.adapter = mAdapter
        mAdapter.setOnNoteItemClickListener(this)
        mAdapter.notifyDataSetChanged()
        setUpTracker()
    }

    private fun setUpTracker() {
        mTracker = SelectionTracker.Builder(
            "multi_selection",
            mBinding.recyclerView,
            MyItemKeyProvider(mBinding.recyclerView),
            MyItemDetailsLookup(mBinding.recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        mTracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    mTracker?.let {
                        selectedNoteIds = null
                        selectedNoteIds =
                            mTracker!!.selection.map { mAdapter.notesList[it.toInt()].id!! }

                        if (selectedNoteIds!!.isEmpty()) {
                            mActionMode?.finish()
                        } else {
                            if (mActionMode == null) mActionMode =
                                startSupportActionMode(this@MainActivity)
                            mActionMode?.title = "Выбрано: ${selectedNoteIds!!.size}"
                        }
                        mBinding.newNoteFab.isEnabled = !mTracker!!.hasSelection()
                    }
                }
            }
        )
        mAdapter.mTracker = mTracker
    }

    private fun fetchNotes() {
        mViewModel.notesList.observe(this, {
            mAdapter.setItems(items = it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mCustomMenu = menu!!
        menuInflater.inflate(R.menu.main_screen_menu, menu)

        if (mPref.isGrid()) {
            mCustomMenu.findItem(R.id.changeListGrid).setIcon(R.drawable.ic_grid)
        } else {
            mCustomMenu.findItem(R.id.changeListGrid).setIcon(R.drawable.ic_list)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.changeListGrid -> {
                mPref.setGrid(!mPref.isGrid())
                if (mPref.isGrid()) {
                    mCustomMenu.findItem(R.id.changeListGrid)
                        .setIcon(R.drawable.ic_grid)
                    mBinding.recyclerView.layoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                } else {
                    mCustomMenu.findItem(R.id.changeListGrid)
                        .setIcon(R.drawable.ic_list)
                    mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
                }
            }
            R.id.delete_note -> deleteMultipleNotes()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteMultipleNotes() {
        selectedNoteIds?.let {
            mViewModel.deleteMultipleNotesById(it)
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        mAdapter.notifyDataSetChanged()
    }


    override fun getViewModel(): MainActivityViewModel {
        mViewModel =
            ViewModelProvider(this, this.viewModelFactory).get(MainActivityViewModel::class.java)
        return mViewModel
    }

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun performDependencyInjection() {
        App.get(this).getComponent().inject(this)
    }

    override fun onClick(note: Note) {
        startActivity(ViewNoteActivity.getStartIntent(this, note.id!!))
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let { mode.menuInflater.inflate(R.menu.action_mode_menu, menu) }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.delete_note -> {
                deleteMultipleNotes()
                showToast(R.string.deleted)
                mode?.finish()
                true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        mTracker?.clearSelection()
        selectedNoteIds = null
        mAdapter.notifyDataSetChanged()
        mActionMode = null
    }

}
