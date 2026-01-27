package com.colorcall.callerscreen.base

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding


abstract class BaseActivity<B : ViewBinding>(
    bindingInflater: (LayoutInflater) -> B
) : AppCompatActivity() {
    //override val scope: Scope by activityScope()
    //private val appOpenLifecycle by inject<AppOpenLifecycle>()
   // private val premiumListener by inject<PremiumListener>()
    val openAdsLoadingDialog by lazy { OpenAdsLoadingDialog(this) }

    val binding by lazy { bindingInflater.invoke(layoutInflater) }
    private var toast: Toast? = null
    var inForeground = false

    //Navigate Activity Launcher
    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        launcherResult?.invoke(it)
    }
    var launcherResult: ((ActivityResult) -> Unit)? = null
    var statusBarHeight = 0
    var bottomBarHeight = 0

    protected abstract fun onCreate()
    protected abstract fun onView()

    open fun beforeOnCreate() {

    }

    open fun beforeShowAppOpen() {
        openAdsLoadingDialog.show()
    }

    open fun onAppOpenShowed() {

    }

    open fun onAppOpenDismiss() {
        openAdsLoadingDialog.dismiss()
    }

    open fun onAppOpenFailedToShow() {
        openAdsLoadingDialog.dismiss()
    }

    open fun canShowAppOpen() = false

    open fun enableState() = false

    open fun setStatusBarStyle(lightMode: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            lightMode
    }

    open fun setNavigationBarStyle(lightMode: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
            lightMode
    }

    open fun setSystemBarStyle(lightMode: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            lightMode
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
            lightMode
    }

    open fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    open fun hideBottomNavigationBar(viewRoot: View) {
        ViewCompat.setOnApplyWindowInsetsListener(viewRoot) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                0
            )
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    open fun consumeSystemBars(
        allowPadding: Boolean = false,
        callback: ((statusBarHeight: Int, bottomBarHeight: Int) -> Unit)? = null
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            bottomBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            if (allowPadding && binding.root is ViewGroup && binding.root.paddingTop + binding.root.paddingBottom != statusBarHeight + bottomBarHeight) {
                binding.root.setPadding(0, statusBarHeight, 0, bottomBarHeight)
            }
            callback?.invoke(statusBarHeight, bottomBarHeight)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideBottomNavigationBar(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        beforeOnCreate()
        super.onCreate(if (enableState()) savedInstanceState else null)
        enableEdgeToEdge()
        onCreate()
        setContentView(binding.root)
        //binding.lifecycleOwner = this
        //setupKoinFragmentFactory(scope)
        binding.root.post { if (isActive()) onView() }
    }

    override fun onStart() {
        if (!isPremium()) {
            //appOpenLifecycle.onStart(this)
           // appOpenLifecycle.loadOpenAds()
        }
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        inForeground = true
    }

    override fun onPause() {
        inForeground = false
        super.onPause()
    }

    override fun onDestroy() {
        //binding.unbind()
        //if (scope.isNotClosed() && !isPremium()) appOpenLifecycle.onDestroy()
        super.onDestroy()
        //if (scope.isNotClosed()) scope.close()
    }

    fun isActive(): Boolean {
        return !isFinishing && !isDestroyed
    }

    fun isPremium(): Boolean {
         return true
       //return premiumListener.isPremium(this)
    }

    fun showToast(message: String) {
        toast?.cancel()
        toast = null
        toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast?.setText(message)
        toast?.show()
    }

}
