package levkaantonov.com.study.training

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bundle = bundleOf("argOne" to "parameter")
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainerView, BlankFragment::class.java, bundle, null)
            .commit()
    }
}

fun <T : ViewBinding> Fragment.viewBinding(inflaterFactory: (inflater: LayoutInflater) -> T) =
    ViewBindingDelegate(this, inflaterFactory)

class ViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val inflaterFactory: (inflater: LayoutInflater) -> T
) : ReadOnlyProperty<Fragment, T>, LifecycleObserver {

    private var binding: T? = null
    private val viewLifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        private fun onDestroy() {
            binding = null
            fragment.viewLifecycleOwner.lifecycle.removeObserver(this)
        }
    }
    private val viewLifecycleOwnerObserver = Observer<LifecycleOwner?> { viewLifecycleOwner ->
        viewLifecycleOwner ?: return@Observer
        viewLifecycleOwner.lifecycle.addObserver(viewLifecycleObserver)
    }

    init {
        fragment.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        fragment.viewLifecycleOwnerLiveData.observeForever(viewLifecycleOwnerObserver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        fragment.viewLifecycleOwnerLiveData.removeObserver(viewLifecycleOwnerObserver)
        fragment.lifecycle.removeObserver(this)
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (binding != null) {
            return binding!!
        }
        val state = fragment.viewLifecycleOwner.lifecycle.currentState
        if (!state.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException(
                "Should not attempt to get bindings when Fragment views are destroyed."
            )
        }
        binding = inflaterFactory(thisRef.layoutInflater)
        return binding as T
    }
}


inline fun <reified T> Fragment.args(): LazyProvider<Fragment, T> =
    ArgsDelegate {
        it.arguments ?: throw RuntimeException("No args passed")
    }

interface LazyProvider<A, T> {
    operator fun provideDelegate(thisRef: A, prop: KProperty<*>): Lazy<T>
}

class ArgsDelegate<F, T> constructor(
    private val bundleFactory: (F) -> Bundle
) : LazyProvider<F, T> {

    override fun provideDelegate(thisRef: F, property: KProperty<*>) =
        lazy {
            val bundle = bundleFactory(thisRef)
            bundle.get(property.name) as T
        }
}