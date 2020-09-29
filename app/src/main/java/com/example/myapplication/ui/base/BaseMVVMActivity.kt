package com.example.myapplication.ui.base


import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding


abstract class BaseMVVMActivity<T : ViewDataBinding, V : BaseViewModel<*>> : BaseActivity() {

    lateinit var viewDataBinding: T
        private set
    private var mViewModel: V? = null

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract fun getViewModel(): V

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    abstract fun getBindingVariable(): Int


    /**
     * @return layout resource id
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDependencyInjection()
        performDataBinding()
    }

    private fun performDataBinding() {
        viewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        this.mViewModel = if (mViewModel == null) getViewModel() else mViewModel
        viewDataBinding.setVariable(getBindingVariable(), mViewModel)
        viewDataBinding.executePendingBindings()
    }


    abstract fun performDependencyInjection()

}
