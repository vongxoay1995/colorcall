package com.colorcall.callerscreen.dialer.dialog

import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.databinding.DialogChangeSortingBinding
import com.colorcall.callerscreen.dialer.extensions.config
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.viewBinding
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_CREATED
import com.simplemobiletools.commons.helpers.SORT_BY_FIRST_NAME
import com.simplemobiletools.commons.helpers.SORT_BY_FULL_NAME
import com.simplemobiletools.commons.helpers.SORT_BY_MIDDLE_NAME
import com.simplemobiletools.commons.helpers.SORT_BY_SURNAME
import com.simplemobiletools.commons.helpers.SORT_DESCENDING

class ChangeSortingDialog(val activity: BaseSimpleActivity, private val showCustomSorting: Boolean = false, private val callback: () -> Unit) {
    private val binding by activity.viewBinding(DialogChangeSortingBinding::inflate)

    private var currSorting = 0
    private var config = activity.config

    init {
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.sort_by)
            }

        currSorting = if (showCustomSorting && config.isCustomOrderSelected) {
            SORT_BY_CUSTOM
        } else {
            config.sorting
        }

        setupSortRadio()
        setupOrderRadio()
    }

    private fun setupSortRadio() {
        binding.apply {
            sortingDialogRadioSorting.setOnCheckedChangeListener { group, checkedId ->
                val isCustomSorting = checkedId == sortingDialogRadioCustom.id
                sortingDialogRadioOrder.beGoneIf(isCustomSorting)
                divider.root.beGoneIf(isCustomSorting)
            }

            val sortBtn = when {
                currSorting and SORT_BY_FIRST_NAME != 0 -> sortingDialogRadioFirstName
                currSorting and SORT_BY_MIDDLE_NAME != 0 -> sortingDialogRadioMiddleName
                currSorting and SORT_BY_SURNAME != 0 -> sortingDialogRadioSurname
                currSorting and SORT_BY_FULL_NAME != 0 -> sortingDialogRadioFullName
                currSorting and SORT_BY_CUSTOM != 0 -> sortingDialogRadioCustom
                else -> sortingDialogRadioDateCreated
            }
            sortBtn.isChecked = true

            if (showCustomSorting) {
                sortingDialogRadioCustom.isChecked = config.isCustomOrderSelected
            }
            sortingDialogRadioCustom.beGoneIf(!showCustomSorting)
        }
    }

    private fun setupOrderRadio() {
        var orderBtn = binding.sortingDialogRadioAscending
        if (currSorting and SORT_DESCENDING != 0) {
            orderBtn = binding.sortingDialogRadioDescending
        }

        orderBtn.isChecked = true
    }

    private fun dialogConfirmed() {
        var sorting = when (binding.sortingDialogRadioSorting.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_first_name -> SORT_BY_FIRST_NAME
            R.id.sorting_dialog_radio_middle_name -> SORT_BY_MIDDLE_NAME
            R.id.sorting_dialog_radio_surname -> SORT_BY_SURNAME
            R.id.sorting_dialog_radio_full_name -> SORT_BY_FULL_NAME
            R.id.sorting_dialog_radio_custom -> SORT_BY_CUSTOM
            else -> SORT_BY_DATE_CREATED
        }

        if (sorting != SORT_BY_CUSTOM && binding.sortingDialogRadioOrder.checkedRadioButtonId == R.id.sorting_dialog_radio_descending) {
            sorting = sorting or SORT_DESCENDING
        }

        if (showCustomSorting) {
            if (sorting == SORT_BY_CUSTOM) {
                config.isCustomOrderSelected = true
            } else {
                config.isCustomOrderSelected = false
                config.sorting = sorting
            }
        } else {
            config.sorting = sorting
        }

        callback()
    }
}
