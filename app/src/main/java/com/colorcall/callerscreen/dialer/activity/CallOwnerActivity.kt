package com.colorcall.callerscreen.dialer.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.colorcall.callerscreen.BuildConfig
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.databinding.ActivityCallOwnerBinding
import com.colorcall.callerscreen.dialer.OPEN_DIAL_PAD_AT_LAUNCH
import com.colorcall.callerscreen.dialer.RecentsHelper
import com.colorcall.callerscreen.dialer.adapter.ViewPagerAdapter
import com.colorcall.callerscreen.dialer.dialog.ChangeSortingDialog
import com.colorcall.callerscreen.dialer.dialog.FilterContactSourcesDialog
import com.colorcall.callerscreen.dialer.extensions.beGoneIf
import com.colorcall.callerscreen.dialer.extensions.config
import com.colorcall.callerscreen.dialer.extensions.launchCreateNewContactIntent
import com.colorcall.callerscreen.dialer.fragments.ContactsFragment
import com.colorcall.callerscreen.dialer.fragments.DialpadFragment
import com.colorcall.callerscreen.dialer.fragments.FavoritesFragment
import com.colorcall.callerscreen.dialer.fragments.MyViewPagerFragment
import com.colorcall.callerscreen.dialer.fragments.RecentsFragment
import com.colorcall.callerscreen.dialer.tabsList
import com.simplemobiletools.commons.dialogs.ChangeViewTypeDialog
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.appLaunched
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.convertToBitmap
import com.simplemobiletools.commons.extensions.getBottomNavigationBackgroundColor
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.extensions.getProperBackgroundColor
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.launchActivityIntent
import com.simplemobiletools.commons.extensions.onGlobalLayout
import com.simplemobiletools.commons.extensions.onTabSelectionChanged
import com.simplemobiletools.commons.extensions.shortcutManager
import com.simplemobiletools.commons.extensions.telecomManager
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateBottomTabItemColors
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.extensions.viewBinding
import com.simplemobiletools.commons.helpers.CONTACTS_GRID_MAX_COLUMNS_COUNT
import com.simplemobiletools.commons.helpers.KEY_PHONE
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CONTACTS
import com.simplemobiletools.commons.helpers.REQUEST_CODE_SET_DEFAULT_CALLER_ID
import com.simplemobiletools.commons.helpers.REQUEST_CODE_SET_DEFAULT_DIALER
import com.simplemobiletools.commons.helpers.TAB_CONTACTS
import com.simplemobiletools.commons.helpers.TAB_DIAPAD
import com.simplemobiletools.commons.helpers.isNougatMR1Plus
import com.simplemobiletools.commons.helpers.isQPlus
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.contacts.Contact
import me.grantland.widget.AutofitHelper

class CallOwnerActivity : DialerActivity() {
    private val binding by viewBinding(ActivityCallOwnerBinding::inflate)
    private var launchedDialer = false
    private var storedShowTabs = 0
    private var storedFontSize = 0
    private var storedStartNameWithSurname = false
    var cachedContacts = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupOptionsMenu()
        refreshMenuItems()
        updateMaterialActivityViews(binding.mainCoordinator, binding.mainHolder, useTransparentNavigation = false, useTopSearchMenu = true)

        launchedDialer = savedInstanceState?.getBoolean(OPEN_DIAL_PAD_AT_LAUNCH) ?: false
        binding.btnBack.setColorFilter(getProperTextColor())

        if (isQPlus() && (config.blockUnknownNumbers || config.blockHiddenNumbers)) {
            setDefaultCallerIdApp()
        }
        setupTabs()
        Contact.sorting = config.sorting
        checkContactPermissions()
        binding.btnBack.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        if (storedShowTabs != config.showTabs) {
            config.lastUsedViewPagerPage = 0
            System.exit(0)
            return
        }

        updateMenuColors()
        val properPrimaryColor = getProperPrimaryColor()
        val dialpadIcon = resources.getColoredDrawableWithColor(R.drawable.ic_dialpad_vector, properPrimaryColor.getContrastColor())
        binding.mainDialpadButton.setImageDrawable(dialpadIcon)

        updateTextColors(binding.mainHolder)
        setupTabColors()
        for (fragment in getAllFragments()) {
            fragment?.setupColors(getProperTextColor(), getProperPrimaryColor(), getProperPrimaryColor())
        }

        val configStartNameWithSurname = config.startNameWithSurname
        if (storedStartNameWithSurname != configStartNameWithSurname) {
            getContactsFragment()?.startNameWithSurnameChanged(configStartNameWithSurname)
           // getFavoritesFragment()?.startNameWithSurnameChanged(configStartNameWithSurname)
            storedStartNameWithSurname = config.startNameWithSurname
        }

        if (!binding.mainMenu.isSearchOpen) {
            refreshItems(true)
        }

        val configFontSize = config.fontSize
        if (storedFontSize != configFontSize) {
            for (fragment in getAllFragments()) {
                fragment?.fontSizeChanged()
            }
        }

        checkShortcuts()
  /*      Handler().postDelayed({
            getRecentsFragment()?.refreshItems()
        }, 2000)*/
    }

    override fun onPause() {
        super.onPause()
        storedShowTabs = config.showTabs
        storedStartNameWithSurname = config.startNameWithSurname
        config.lastUsedViewPagerPage = binding.viewPager.currentItem
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // we don't really care about the result, the app can work without being the default Dialer too
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            checkContactPermissions()
        } else if (requestCode == REQUEST_CODE_SET_DEFAULT_CALLER_ID && resultCode != Activity.RESULT_OK) {
            toast(R.string.must_make_default_caller_id_app, length = Toast.LENGTH_LONG)
            baseConfig.blockUnknownNumbers = false
            baseConfig.blockHiddenNumbers = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(OPEN_DIAL_PAD_AT_LAUNCH, launchedDialer)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshItems()
    }

    override fun onBackPressed() {
        if (binding.mainMenu.isSearchOpen) {
            binding.mainMenu.closeSearch()
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshMenuItems() {
        val currentFragment = getCurrentFragment()
        binding.mainMenu.getToolbar().menu.apply {
            //findItem(R.id.clear_call_history).isVisible = currentFragment == getRecentsFragment()
            findItem(R.id.sort).isVisible = currentFragment != getRecentsFragment()
            findItem(R.id.create_new_contact).isVisible = currentFragment == getContactsFragment()
           // findItem(R.id.change_view_type).isVisible = currentFragment == getFavoritesFragment()
          //  findItem(R.id.column_count).isVisible = currentFragment == getFavoritesFragment() && config.viewType == VIEW_TYPE_GRID
        }
    }
    private fun addNumberToContact() {
        Log.e("TAN", "addNumberToContact: getCurrentFragment ${binding.viewPager.currentItem} ", )
        if (binding.viewPager.currentItem == 0){
            Log.e("TAN", "addNumberToContact: ", )
            val value = (getCurrentFragment() as DialpadFragment).getEdtNumber().text.toString()
            Intent().apply {
                action = Intent.ACTION_INSERT_OR_EDIT
                type = "vnd.android.cursor.item/contact"
                putExtra(KEY_PHONE, value)
                launchActivityIntent(this)
            }
        }

    }
    private fun setupOptionsMenu() {
        val menuItem = binding.dialpadToolbar.menu.findItem(R.id.add_number_to_contact)
        menuItem?.icon?.setTint(resources.getColor(R.color.black))
        binding.dialpadToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_number_to_contact -> addNumberToContact()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
        binding.mainMenu.apply {
            getToolbar().inflateMenu(R.menu.menu)
            toggleHideOnScroll(false)
            setupMenu()

            onSearchClosedListener = {
                for (fragment in getAllFragments()) {
                    fragment?.onSearchQueryChanged("")
                }
            }

            onSearchTextChangedListener = { text ->
                getCurrentFragment()?.onSearchQueryChanged(text)
            }

            getToolbar().setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                 //   R.id.clear_call_history -> clearCallHistory()
                    R.id.create_new_contact -> launchCreateNewContactIntent()
               //     R.id.sort -> showSortingDialog(showCustomSorting = getCurrentFragment() is FavoritesFragment)
                    R.id.filter -> showFilterDialog()
                //    R.id.settings -> launchSettings()
                    R.id.change_view_type -> changeViewType()
                    R.id.column_count -> changeColumnCount()
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun changeColumnCount() {
        val items = ArrayList<RadioItem>()
        for (i in 1..CONTACTS_GRID_MAX_COLUMNS_COUNT) {
            items.add(RadioItem(i, resources.getQuantityString(R.plurals.column_counts, i, i)))
        }

        val currentColumnCount = config.contactsGridColumnCount
        RadioGroupDialog(this, ArrayList(items), currentColumnCount) {
            val newColumnCount = it as Int
            if (currentColumnCount != newColumnCount) {
                config.contactsGridColumnCount = newColumnCount
                getFavoritesFragment()?.columnCountChanged()
            }
        }
    }

    private fun changeViewType() {
        ChangeViewTypeDialog(this) {
            refreshMenuItems()
            getFavoritesFragment()?.refreshItems()
        }
    }

    private fun updateMenuColors() {
        updateStatusbarColor(getProperBackgroundColor())
        binding.mainMenu.updateColors()
    }

    private fun checkContactPermissions() {
        handlePermission(PERMISSION_READ_CONTACTS) {
            initFragments()
        }
    }

    private fun clearCallHistory() {
        val confirmationText = "${getString(R.string.clear_history_confirmation)}\n\n${getString(R.string.cannot_be_undone)}"
        ConfirmationDialog(this, confirmationText) {
            RecentsHelper(this).removeAllRecentCalls(this) {
                runOnUiThread {
                    getRecentsFragment()?.refreshItems()
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun checkShortcuts() {
        val appIconColor = config.appIconColor
        if (isNougatMR1Plus() && config.lastHandledShortcutColor != appIconColor) {
            val launchDialpad = getLaunchDialpadShortcut(appIconColor)

            try {
                shortcutManager.dynamicShortcuts = listOf(launchDialpad)
                config.lastHandledShortcutColor = appIconColor
            } catch (ignored: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getLaunchDialpadShortcut(appIconColor: Int): ShortcutInfo {
        val newEvent = getString(R.string.dialpad)
        val drawable = resources.getDrawable(R.drawable.shortcut_dialpad)
        (drawable as LayerDrawable).findDrawableByLayerId(R.id.shortcut_dialpad_background).applyColorFilter(appIconColor)
        val bmp = drawable.convertToBitmap()

        val intent = Intent(this, DialpadActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        return ShortcutInfo.Builder(this, "launch_dialpad")
            .setShortLabel(newEvent)
            .setLongLabel(newEvent)
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(intent)
            .build()
    }

    private fun setupTabColors() {
        val activeView = binding.mainTabsHolder.getTabAt(binding.viewPager.currentItem)?.customView
        updateBottomTabItemColors(activeView, true, getSelectedTabDrawableIds()[binding.viewPager.currentItem])

        getInactiveTabIndexes(binding.viewPager.currentItem).forEach { index ->
            val inactiveView = binding.mainTabsHolder.getTabAt(index)?.customView
            Log.e("TAN", "setupTabColors: $index", )
            updateBottomTabItemColors(inactiveView, false, getDeselectedTabDrawableIds()[index])
        }

        val bottomBarColor = getBottomNavigationBackgroundColor()
        binding.mainTabsHolder.setBackgroundColor(bottomBarColor)
        updateNavigationBarColor(bottomBarColor)
    }

    private fun getInactiveTabIndexes(activeIndex: Int) = (0 until binding.mainTabsHolder.tabCount).filter { it != activeIndex }

    private fun getSelectedTabDrawableIds(): List<Int> {
        val showTabs = config.showTabs
        val icons = mutableListOf<Int>()
        if (showTabs and TAB_DIAPAD != 0) {
            icons.add(R.drawable.diapad_fill)
        }
        if (showTabs and TAB_CONTACTS != 0) {
            icons.add(R.drawable.ic_person_vector)
        }



/*
        if (showTabs and TAB_FAVORITES != 0) {
            icons.add(R.drawable.ic_star_vector)
        }

        if (showTabs and TAB_CALL_HISTORY != 0) {
            icons.add(R.drawable.ic_clock_filled_vector)
        }*/

        return icons
    }

    private fun getDeselectedTabDrawableIds(): ArrayList<Int> {
        val showTabs = config.showTabs
        val icons = ArrayList<Int>()
        if (showTabs and TAB_DIAPAD != 0) {
            icons.add(R.drawable.diapad_stroke)
        }
        if (showTabs and TAB_CONTACTS != 0) {
            icons.add(R.drawable.ic_person_outline_vector)
        }

       /* if (showTabs and TAB_FAVORITES != 0) {
            icons.add(R.drawable.ic_star_outline_vector)
        }

        if (showTabs and TAB_CALL_HISTORY != 0) {
            icons.add(R.drawable.ic_clock_vector)
        }*/

        return icons
    }

    private fun initFragments() {
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                binding.mainTabsHolder.getTabAt(position)?.select()
                getAllFragments().forEach {
                    it?.finishActMode()
                }
                if(position == 0){
                    binding.mainMenu.visibility = View.GONE
                    binding.dialpadToolbar.visibility = View.VISIBLE
                }else{
                    binding.mainMenu.visibility = View.VISIBLE
                    binding.dialpadToolbar.visibility = View.GONE
                }
                refreshMenuItems()
            }
        })

        // selecting the proper tab sometimes glitches, add an extra selector to make sure we have it right
        binding.mainTabsHolder.onGlobalLayout {
            Handler().postDelayed({
                var wantedTab = getDefaultTab()

                // open the Recents tab if we got here by clicking a missed call notification
             /*   if (intent.action == Intent.ACTION_VIEW && config.showTabs and TAB_CALL_HISTORY > 0) {
                    wantedTab = binding.mainTabsHolder.tabCount - 1

                    ensureBackgroundThread {
                        clearMissedCalls()
                    }
                }*/

                binding.mainTabsHolder.getTabAt(wantedTab)?.select()
                refreshMenuItems()
            }, 100L)
        }

        binding.mainDialpadButton.setOnClickListener {
            launchDialpad()
        }

        binding.viewPager.onGlobalLayout {
            refreshMenuItems()
        }

        if (config.openDialPadAtLaunch && !launchedDialer) {
            launchDialpad()
            launchedDialer = true
        }
    }

    private fun setupTabs() {
        binding.viewPager.adapter = null
        binding.mainTabsHolder.removeAllTabs()
        tabsList.forEachIndexed { index, value ->
            if (config.showTabs and value != 0) {
                binding.mainTabsHolder.newTab().setCustomView(R.layout.bottom_tablayout_item).apply {
                    customView?.findViewById<ImageView>(R.id.tab_item_icon)?.setImageDrawable(getTabIcon(index))
                    customView?.findViewById<TextView>(R.id.tab_item_label)?.text = getTabLabel(index)
                    AutofitHelper.create(customView?.findViewById(R.id.tab_item_label))
                    binding.mainTabsHolder.addTab(this)
                }
            }
        }

        binding.mainTabsHolder.onTabSelectionChanged(
            tabUnselectedAction = {
                updateBottomTabItemColors(it.customView, false, getDeselectedTabDrawableIds()[it.position])
            },
            tabSelectedAction = {
                binding.mainMenu.closeSearch()
                binding.viewPager.currentItem = it.position
                updateBottomTabItemColors(it.customView, true, getSelectedTabDrawableIds()[it.position])
            }
        )

        binding.mainTabsHolder.beGoneIf(binding.mainTabsHolder.tabCount == 1)
        storedShowTabs = config.showTabs
        storedStartNameWithSurname = config.startNameWithSurname
    }

    private fun getTabIcon(position: Int): Drawable {
        val drawableId = when (position) {
            0 -> R.drawable.diapad_fill
            else -> R.drawable.ic_person_vector
        }

        return resources.getColoredDrawableWithColor(drawableId, getProperTextColor())
    }

    private fun getTabLabel(position: Int): String {
        val stringId = when (position) {
            0 -> R.string.dialpad
           // 1 -> R.string.favorites_tab
           // else -> R.string.call_history_tab
            else -> R.string.contacts_tab
        }

        return resources.getString(stringId)
    }

    private fun refreshItems(openLastTab: Boolean = false) {
        if (isDestroyed || isFinishing) {
            return
        }

        binding.apply {
            if (viewPager.adapter == null) {
                viewPager.adapter = ViewPagerAdapter(this@CallOwnerActivity)
                viewPager.currentItem = if (openLastTab) config.lastUsedViewPagerPage else getDefaultTab()
                viewPager.onGlobalLayout {
                    refreshFragments()
                }
            } else {
                refreshFragments()
            }
        }
    }

    private fun launchDialpad() {
        Log.e("TAN", "launchDialpad: ", )
        Intent(applicationContext, DialpadActivity::class.java).apply {
            startActivity(this)
        }
    }

    fun refreshFragments() {
        getContactsFragment()?.refreshItems()
        //getFavoritesFragment()?.refreshItems()
        //getRecentsFragment()?.refreshItems()
    }

    private fun getAllFragments(): ArrayList<MyViewPagerFragment<*>?> {
        val showTabs = config.showTabs
        val fragments = arrayListOf<MyViewPagerFragment<*>?>()

        if (showTabs and TAB_DIAPAD > 0) {
            fragments.add(getDiapadFragment())
        }
        if (showTabs and TAB_CONTACTS > 0) {
            fragments.add(getContactsFragment())
        }

       /* if (showTabs and TAB_FAVORITES > 0) {
            fragments.add(getFavoritesFragment())
        }

        if (showTabs and TAB_CALL_HISTORY > 0) {
            fragments.add(getRecentsFragment())
        }*/

        return fragments
    }

    private fun getCurrentFragment(): MyViewPagerFragment<*>? = getAllFragments().getOrNull(binding.viewPager.currentItem)

    private fun getContactsFragment(): ContactsFragment? = findViewById(R.id.contacts_fragment)

    private fun getDiapadFragment(): DialpadFragment? = findViewById(R.id.diapad_fragment)

    private fun getFavoritesFragment(): FavoritesFragment? = findViewById(R.id.favorites_fragment)

    private fun getRecentsFragment(): RecentsFragment? = findViewById(R.id.recents_fragment)

    private fun getDefaultTab(): Int {
        val showTabsMask = config.showTabs
        /*return when (config.defaultTab) {
            TAB_LAST_USED -> if (config.lastUsedViewPagerPage < binding.mainTabsHolder.tabCount) config.lastUsedViewPagerPage else 0
            TAB_CONTACTS -> 0
            TAB_FAVORITES -> if (showTabsMask and TAB_CONTACTS > 0) 1 else 0
            else -> {
                if (showTabsMask and TAB_CALL_HISTORY > 0) {
                    if (showTabsMask and TAB_CONTACTS > 0) {
                        if (showTabsMask and TAB_FAVORITES > 0) {
                            2
                        } else {
                            1
                        }
                    } else {
                        if (showTabsMask and TAB_FAVORITES > 0) {
                            1
                        } else {
                            0
                        }
                    }
                } else {
                    0
                }
            }
        }*/
        return 0
    }

    @SuppressLint("MissingPermission")
    private fun clearMissedCalls() {
        try {
            // notification cancellation triggers MissedCallNotifier.clearMissedCalls() which, in turn,
            // should update the database and reset the cached missed call count in MissedCallNotifier.java
            // https://android.googlesource.com/platform/packages/services/Telecomm/+/master/src/com/android/server/telecom/ui/MissedCallNotifierImpl.java#170
            telecomManager.cancelMissedCallsNotification()
        } catch (ignored: Exception) {
        }
    }

    private fun launchSettings() {
    //    hideKeyboard()
     //   startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun showSortingDialog(showCustomSorting: Boolean) {
        ChangeSortingDialog(this, showCustomSorting) {
            getFavoritesFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(binding.mainMenu.getCurrentQuery())
                }
            }

            getContactsFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(binding.mainMenu.getCurrentQuery())
                }
            }
        }
    }

    private fun showFilterDialog() {
        FilterContactSourcesDialog(this) {
            getFavoritesFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(binding.mainMenu.getCurrentQuery())
                }
            }

            getContactsFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(binding.mainMenu.getCurrentQuery())
                }
            }

            getRecentsFragment()?.refreshItems {
                if (binding.mainMenu.isSearchOpen) {
                    getCurrentFragment()?.onSearchQueryChanged(binding.mainMenu.getCurrentQuery())
                }
            }
        }
    }

    fun cacheContacts(contacts: List<Contact>) {
        try {
            cachedContacts.clear()
            cachedContacts.addAll(contacts)
        } catch (e: Exception) {
        }
    }

    fun visibleMenuAdd(value:Boolean) {
        Log.e("TAN", "visibleMenuAdd: value $value", )
        val menuItem = binding.dialpadToolbar.menu.findItem(R.id.add_number_to_contact)
        menuItem.isVisible = value

    }
}
