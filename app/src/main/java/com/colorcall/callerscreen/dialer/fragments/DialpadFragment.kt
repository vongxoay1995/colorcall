package com.colorcall.callerscreen.dialer.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.databinding.FragmentDiapadBinding
import com.colorcall.callerscreen.databinding.FragmentDiapadLayoutBinding
import com.colorcall.callerscreen.dialer.DIALPAD_TONE_LENGTH_MS
import com.colorcall.callerscreen.dialer.ToneGeneratorHelper
import com.colorcall.callerscreen.dialer.activity.CallOwnerActivity
import com.colorcall.callerscreen.dialer.activity.DialerActivity
import com.colorcall.callerscreen.dialer.adapter.ContactsAdapter
import com.colorcall.callerscreen.dialer.extensions.areMultipleSIMsAvailable
import com.colorcall.callerscreen.dialer.extensions.boundingBox
import com.colorcall.callerscreen.dialer.extensions.config
import com.colorcall.callerscreen.dialer.extensions.startCallIntent
import com.colorcall.callerscreen.dialer.models.SpeedDial
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.simplemobiletools.commons.dialogs.CallConfirmationDialog
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.getColorStateList
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.extensions.getMyContactsCursor
import com.simplemobiletools.commons.extensions.normalizeString
import com.simplemobiletools.commons.extensions.onTextChangeListener
import com.simplemobiletools.commons.extensions.performHapticFeedback
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.commons.helpers.ContactsHelper
import com.simplemobiletools.commons.helpers.MyContactsContentProvider
import com.simplemobiletools.commons.models.contacts.Contact
import com.simplemobiletools.dialer.extensions.addCharacter
import com.simplemobiletools.dialer.extensions.disableKeyboard
import com.simplemobiletools.dialer.extensions.getKeyEvent
import java.util.Locale
import kotlin.math.roundToInt

class DialpadFragment(context: Context, attributeSet: AttributeSet) :
    MyViewPagerFragment<MyViewPagerFragment.DiapadInnerBinding>(context, attributeSet) {

    private lateinit var binding: FragmentDiapadLayoutBinding
    private var allContacts = ArrayList<Contact>()
    private var speedDialValues = ArrayList<SpeedDial>()
    private val russianCharsMap = HashMap<Char, Int>()
    private var hasRussianLocale = false
    private var toneGeneratorHelper: ToneGeneratorHelper? = null
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val pressedKeys = mutableSetOf<Char>()

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = FragmentDiapadLayoutBinding.bind(FragmentDiapadBinding.bind(this).diapadFragment)
        innerBinding = DiapadInnerBinding(binding)
    }

    override fun setupFragment() {
        hasRussianLocale = Locale.getDefault().language == "ru"
        toneGeneratorHelper = ToneGeneratorHelper(context, DIALPAD_TONE_LENGTH_MS)
        speedDialValues = context.config.getSpeedDialValues()

        binding.apply {
            dialpadWrapper.apply {
                if (context.config.hideDialpadNumbers) {
                    dialpad1Holder.isVisible = false
                    dialpad2Holder.isVisible = false
                    dialpad3Holder.isVisible = false
                    dialpad4Holder.isVisible = false
                    dialpad5Holder.isVisible = false
                    dialpad6Holder.isVisible = false
                    dialpad7Holder.isVisible = false
                    dialpad8Holder.isVisible = false
                    dialpad9Holder.isVisible = false
                    dialpadPlusHolder.isVisible = true
                    dialpad0Holder.visibility = View.INVISIBLE
                }

                arrayOf(
                    dialpad0Holder, dialpad1Holder, dialpad2Holder, dialpad3Holder,
                    dialpad4Holder, dialpad5Holder, dialpad6Holder, dialpad7Holder,
                    dialpad8Holder, dialpad9Holder, dialpadPlusHolder,
                    dialpadAsteriskHolder, dialpadHashtagHolder
                ).forEach {
                    it.background = ResourcesCompat.getDrawable(resources, R.drawable.pill_background, null)
                    it.background?.alpha = com.simplemobiletools.commons.helpers.LOWER_ALPHA_INT
                }
                if (hasRussianLocale) {
                    initRussianChars()
                    dialpad2Letters.append("\nАБВГ")
                    dialpad3Letters.append("\nДЕЁЖЗ")
                    dialpad4Letters.append("\nИЙКЛ")
                    dialpad5Letters.append("\nМНОП")
                    dialpad6Letters.append("\nРСТУ")
                    dialpad7Letters.append("\nФХЦЧ")
                    dialpad8Letters.append("\nШЩЪЫ")
                    dialpad9Letters.append("\nЬЭЮЯ")

                    val fontSize = resources.getDimension(R.dimen.small_text_size)
                    arrayOf(
                        dialpad2Letters, dialpad3Letters, dialpad4Letters, dialpad5Letters,
                        dialpad6Letters, dialpad7Letters, dialpad8Letters, dialpad9Letters
                    ).forEach {
                        it.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, fontSize)
                    }
                }

                setupCharClick(dialpad1Holder, '1')
                setupCharClick(dialpad2Holder, '2')
                setupCharClick(dialpad3Holder, '3')
                setupCharClick(dialpad4Holder, '4')
                setupCharClick(dialpad5Holder, '5')
                setupCharClick(dialpad6Holder, '6')
                setupCharClick(dialpad7Holder, '7')
                setupCharClick(dialpad8Holder, '8')
                setupCharClick(dialpad9Holder, '9')
                setupCharClick(dialpad0Holder, '0')
                setupCharClick(dialpadPlusHolder, '+', longClickable = false)
                setupCharClick(dialpadAsteriskHolder, '*', longClickable = false)
                setupCharClick(dialpadHashtagHolder, '#', longClickable = false)
            }



            dialpadClearChar.setOnClickListener { clearChar(it) }
            dialpadClearChar.setOnLongClickListener { clearInput(); true }
            dialpadCallButton.setOnClickListener { initCall(dialpadInput.value, 0) }
            dialpadInput.onTextChangeListener { dialpadValueChanged(it) }
            dialpadInput.requestFocus()
            dialpadInput.disableKeyboard()
/*
            if (context.areMultipleSIMsAvailable()) {
                val callIcon = resources.getColoredDrawableWithColor(R.drawable.ic_phone_two_vector, context.getProperPrimaryColor().getContrastColor())
                dialpadCallTwoButton.setImageDrawable(callIcon)
                dialpadCallTwoButton.background.applyColorFilter(context.getProperPrimaryColor())
                dialpadCallTwoButton.beVisible()
                dialpadCallTwoButton.setOnClickListener { initCall(dialpadInput.value, 1) }
            }*/
        }

        ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
            gotContacts(contacts)
        }
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
        binding.apply {
            dialpadClearChar.applyColorFilter(textColor)

            val callIconId = if (context.areMultipleSIMsAvailable()) R.drawable.ic_phone_one_vector else R.drawable.ic_phone_vector
            val callIcon = resources.getColoredDrawableWithColor(callIconId, properPrimaryColor.getContrastColor())
           /// dialpadCallButton.setImageDrawable(callIcon)
            dialpadCallButton.background.applyColorFilter(properPrimaryColor)

            letterFastscroller.textColor = textColor.getColorStateList()
            letterFastscroller.pressedTextColor = properPrimaryColor
            letterFastscrollerThumb.setupWithFastScroller(letterFastscroller)
            letterFastscrollerThumb.textColor = properPrimaryColor.getContrastColor()
            letterFastscrollerThumb.thumbColor = properPrimaryColor.getColorStateList()

            (dialpadList?.adapter as? ContactsAdapter)?.updateTextColor(textColor)
        }
    }

    override fun onSearchClosed() {
        binding.dialpadInput.setText("")
        dialpadValueChanged("")
    }

    override fun onSearchQueryChanged(text: String) {
        dialpadValueChanged(text)
    }

    private fun gotContacts(newContacts: ArrayList<Contact>) {
        allContacts = newContacts
        val privateCursor = context.getMyContactsCursor(false, true)
        val privateContacts = MyContactsContentProvider.getContacts(context, privateCursor)
        if (privateContacts.isNotEmpty()) {
            allContacts.addAll(privateContacts)
            allContacts.sort()
        }
        activity?.runOnUiThread {
            dialpadValueChanged(binding.dialpadInput.value)
        }
    }

    private fun dialpadValueChanged(text: String) {
        Log.e("TAN", "dialpadValueChanged: ", )
        if (text.isNotEmpty()){
            (activity as CallOwnerActivity).visibleMenuAdd(true)
        }else{
            (activity as CallOwnerActivity).visibleMenuAdd(false)
        }
        val filtered = allContacts.filter {
            var convertedName = PhoneNumberUtils.convertKeypadLettersToDigits(it.name.normalizeString())
            if (hasRussianLocale) {
                var currConvertedName = ""
                convertedName.lowercase(Locale.getDefault()).forEach { char ->
                    val convertedChar = russianCharsMap.getOrElse(char) { char }
                    currConvertedName += convertedChar
                }
                convertedName = currConvertedName
            }
            it.doesContainPhoneNumber(text) || convertedName.contains(text, true)
        }.sortedWith(compareBy { !it.doesContainPhoneNumber(text) }).toMutableList() as ArrayList<Contact>

        binding.letterFastscroller.setupWithRecyclerView(binding.dialpadList, { position ->
            try {
                val name = filtered[position].getNameToDisplay()
                val character = if (name.isNotEmpty()) name.substring(0, 1) else ""
                FastScrollItemIndicator.Text(character.uppercase(Locale.getDefault()))
            } catch (e: Exception) {
                FastScrollItemIndicator.Text("")
            }
        })

        if (filtered.isEmpty()) {
            binding.dialpadPlaceholder.beVisible()
            binding.dialpadList.beGone()
        } else {
            binding.dialpadPlaceholder.beGone()
            binding.dialpadList.beVisible()
        }

        val adapter = binding.dialpadList.adapter as? ContactsAdapter
        if (adapter == null) {
            ContactsAdapter(
                activity = activity as DialerActivity,
                contacts = filtered,
                recyclerView = binding.dialpadList,
                highlightText = text
            ) {
                val contact = it as Contact
                if (context.config.showCallConfirmation) {
                    CallConfirmationDialog(activity!!, contact.getNameToDisplay()) {
                        activity?.startCallIntent(contact.getPrimaryNumber() ?: return@CallConfirmationDialog)
                    }
                } else {
                    activity?.startCallIntent(contact.getPrimaryNumber() ?: return@ContactsAdapter)
                }
            }.apply {
                binding.dialpadList.adapter = this
            }
        } else {
            adapter.updateItems(filtered, text)
        }
    }

    private fun initCall(number: String, handleIndex: Int) {
        if (number.isNotEmpty()) {
           /* if (handleIndex != -1 && context.areMultipleSIMsAvailable()) {
                if (context.config.showCallConfirmation) {
                    CallConfirmationDialog(activity!!, number) {
                        activity?.callContactWithSim(number, handleIndex == 0)
                    }
                } else {
                    activity?.callContactWithSim(number, handleIndex == 0)
                }
            } else {
                if (context.config.showCallConfirmation) {
                    CallConfirmationDialog(activity!!, number) {
                        activity?.startCallIntent(number)
                    }
                } else {
                    activity?.startCallIntent(number)
                }
            }*/
            activity?.startCallIntent(number)
        }
    }

    private fun dialpadPressed(char: Char, view: View?) {
        binding.dialpadInput.addCharacter(char)
        maybePerformDialpadHapticFeedback(view)
    }

    private fun clearChar(view: View) {
        binding.dialpadInput.dispatchKeyEvent(binding.dialpadInput.getKeyEvent(android.view.KeyEvent.KEYCODE_DEL))
        maybePerformDialpadHapticFeedback(view)
    }

    private fun clearInput() {
        binding.dialpadInput.setText("")
    }

    private fun speedDial(id: Int): Boolean {
        if (binding.dialpadInput.value.length == 1) {
            val speedDial = speedDialValues.firstOrNull { it.id == id }
            if (speedDial?.isValid() == true) {
                initCall(speedDial.number, -1)
                return true
            }
        }
        return false
    }

    private fun initRussianChars() {
        russianCharsMap['а'] = 2; russianCharsMap['б'] = 2; russianCharsMap['в'] = 2; russianCharsMap['г'] = 2
        russianCharsMap['д'] = 3; russianCharsMap['е'] = 3; russianCharsMap['ё'] = 3; russianCharsMap['ж'] = 3; russianCharsMap['з'] = 3
        russianCharsMap['и'] = 4; russianCharsMap['й'] = 4; russianCharsMap['к'] = 4; russianCharsMap['л'] = 4
        russianCharsMap['м'] = 5; russianCharsMap['н'] = 5; russianCharsMap['о'] = 5; russianCharsMap['п'] = 5
        russianCharsMap['р'] = 6; russianCharsMap['с'] = 6; russianCharsMap['т'] = 6; russianCharsMap['у'] = 6
        russianCharsMap['ф'] = 7; russianCharsMap['х'] = 7; russianCharsMap['ц'] = 7; russianCharsMap['ч'] = 7
        russianCharsMap['ш'] = 8; russianCharsMap['щ'] = 8; russianCharsMap['ъ'] = 8; russianCharsMap['ы'] = 8
        russianCharsMap['ь'] = 9; russianCharsMap['э'] = 9; russianCharsMap['ю'] = 9; russianCharsMap['я'] = 9
    }

    private fun startDialpadTone(char: Char) {
        if (context.config.dialpadBeeps) {
            pressedKeys.add(char)
            toneGeneratorHelper?.startTone(char)
        }
    }

    private fun stopDialpadTone(char: Char) {
        if (context.config.dialpadBeeps) {
            if (!pressedKeys.remove(char)) return
            if (pressedKeys.isEmpty()) {
                toneGeneratorHelper?.stopTone()
            } else {
                startDialpadTone(pressedKeys.last())
            }
        }
    }

    private fun maybePerformDialpadHapticFeedback(view: View?) {
        if (context.config.dialpadVibration) {
            view?.performHapticFeedback()
        }
    }

    private fun performLongClick(view: View, char: Char) {
        if (char == '0') {
            clearChar(view)
            dialpadPressed('+', view)
        } else {
            val result = speedDial(char.digitToInt())
            if (result) {
                stopDialpadTone(char)
                clearChar(view)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCharClick(view: View, char: Char, longClickable: Boolean = true) {
        view.isClickable = true
        view.isLongClickable = true
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dialpadPressed(char, view)
                    startDialpadTone(char)
                    if (longClickable) {
                        longPressHandler.removeCallbacksAndMessages(null)
                        longPressHandler.postDelayed({
                            performLongClick(view, char)
                        }, longPressTimeout)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopDialpadTone(char)
                    if (longClickable) {
                        longPressHandler.removeCallbacksAndMessages(null)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val viewContainsTouchEvent = if (event.rawX.isNaN() || event.rawY.isNaN()) {
                        false
                    } else {
                        view.boundingBox.contains(event.rawX.roundToInt(), event.rawY.roundToInt())
                    }
                    if (!viewContainsTouchEvent) {
                        stopDialpadTone(char)
                        if (longClickable) {
                            longPressHandler.removeCallbacksAndMessages(null)
                        }
                    }
                }
            }
            false
        }
    }
    fun getEdtNumber():EditText {
        return binding.dialpadInput
    }
}