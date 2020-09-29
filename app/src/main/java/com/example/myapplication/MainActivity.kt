package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
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
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseMVVMActivity<ActivityMainBinding, MainActivityViewModel>(),
    MainActivityNavigator, NotesAdapter.OnNoteItemClickListener {

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
    private var selectedNoteIds: List<Int?> = listOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding
        mViewModel.setNavigator(this)

        setUp()
        setUpRecyclerView()

        mBinding.newNoteFab.setOnClickListener {
            startActivity(AddNoteActivity.getStartIntent(this))
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setUp() {
        initToolbar(R.id.toolbar, R.string.notes)
        setHomeAsUp(false)
        fetchNotes()
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

    private fun setUpAdapter(notes: List<Note>) {
        mAdapter.setItems(notes)
        mBinding.recyclerView.adapter = mAdapter
        mAdapter.setOnNoteItemClickListener(this)
        mAdapter.notifyDataSetChanged()
        setUpTracker()
    }

    private fun setUpTracker() {
        mTracker = SelectionTracker.Builder<Long>(
            "noteSelection",
            recycler_view,
            MyItemKeyProvider(recycler_view),
            MyItemDetailsLookup(recycler_view),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        mTracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    selectedNoteIds = mTracker?.selection!!.map {
                        mAdapter.notesList[it.toInt()].id
                    }.toList()
                    mCustomMenu.findItem(R.id.delete_note).isVisible = mTracker?.hasSelection()!!
                }
            }
        )
        mAdapter.mTracker = mTracker
    }

    private fun fetchNotes() {
        mViewModel.notesList.observe(this, Observer {
            setUpAdapter(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mCustomMenu = menu!!
        menuInflater.inflate(R.menu.main_screen_menu, menu)

        if (mPref.isGrid()) {
            mCustomMenu.findItem(R.id.changeListGrid).setIcon(R.drawable.ic_dashboard_white_36)
        } else {
            mCustomMenu.findItem(R.id.changeListGrid).setIcon(R.drawable.ic_horizontal_white_36dp)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.changeListGrid -> {
                mPref.setGrid(!mPref.isGrid())
                if (mPref.isGrid()) {
                    mCustomMenu.findItem(R.id.changeListGrid)
                        .setIcon(R.drawable.ic_dashboard_white_36)
                    mBinding.recyclerView.layoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                } else {
                    mCustomMenu.findItem(R.id.changeListGrid)
                        .setIcon(R.drawable.ic_horizontal_white_36dp)
                    mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
                }
            }
            R.id.delete_note -> deleteMultipleNotes()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteMultipleNotes() {
        mViewModel.deleteMultipleNotesById(selectedNoteIds)
        mAdapter.notifyDataSetChanged()
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

    override fun onLongClick(position: Int) {
        setUpTracker()
        /*val menuItem = mCustomMenu.findItem(R.id.delete_note)
        menuItem.isVisible = true
   */
    }


}
