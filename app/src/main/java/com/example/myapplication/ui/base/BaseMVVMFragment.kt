package com.example.myapplication.ui.base


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.ParameterizedType

abstract class BaseMVVMFragment<T : ViewDataBinding, V : BaseViewModel<*>> : BaseFragment() {

    lateinit var viewDataBinding: T
        private set

    private lateinit var mViewModel: V

    lateinit var mRootView: View


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
    open fun getBindingVariable(): Int = BR.viewModel

    /**
     * @return layout resource id
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun performDependencyInjection()

    override fun onCreate(savedInstanceState: Bundle?) {
        performDependencyInjection()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        mRootView = viewDataBinding.root
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelClazz: Class<V> =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<V>
        this.mViewModel = ViewModelProvider(this).get(viewModelClazz)
        viewDataBinding.setVariable(getBindingVariable(), mViewModel)
        viewDataBinding.executePendingBindings()
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }


}
