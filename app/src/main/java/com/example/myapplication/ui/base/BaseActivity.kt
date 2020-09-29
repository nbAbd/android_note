package com.example.myapplication.ui.base

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.myapplication.R

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    private var mToast: Toast? = null
    private var progressDialog: MaterialDialog? = null

    // Toast
    fun showToast(@StringRes message: Int) {
        showToast(getString(message))
    }

    fun showToast(message: String) {
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        mToast!!.show()
    }

    // progress
    open fun showProgress(title: String?) {
        hideProgress()

        progressDialog = MaterialDialog(this)
            .cancelable(false)
            .customView(viewRes = R.layout.custom_progress)

        if (title != null) {
            progressDialog?.title(text = title)
        }

        if (!this.isFinishing) {
            progressDialog?.show()
        }
    }

    open fun showProgress() {
        showProgress(getString(R.string.please_wait))
    }

    open fun hideProgress() {
        progressDialog?.dismiss()
    }

    // ActionBar
    fun setActionBarTitle(title: String) {
        val actionBar: ActionBar? = supportActionBar
        actionBar?.title = title
    }

    fun setActionBarTitle(@StringRes title: Int) {
        setActionBarTitle(getString(title))
    }

    fun showActionBar() {
        val actionBar: ActionBar? = supportActionBar
        actionBar?.show()
    }

    fun hideActionBar() {
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
    }

    // Toolbar
    private fun initToolbar(toolbar: Toolbar, title: String?) {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title?.let { supportActionBar?.setTitle(title) }
    }

    fun initToolbar(@IdRes toolbarId: Int, @StringRes title: Int) {
        initToolbar(findViewById<Toolbar>(toolbarId), getString(title))
    }

    fun initToolbar(@IdRes toolbarId: Int, title: String?) {
        initToolbar(findViewById<Toolbar>(toolbarId), title)
    }


    fun setHomeAsUp(status: Boolean) {
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(status)
    }
}