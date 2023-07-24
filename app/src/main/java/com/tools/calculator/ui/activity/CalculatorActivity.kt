package com.tools.calculator.ui.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tools.calculator.R
import com.tools.calculator.databinding.ActivityCalculatorBinding
import com.tools.calculator.sharepreference.MyPreferences
import com.tools.calculator.util.Fraction
import com.tools.calculator.util.NumberFormatter
import com.tools.calculator.util.fractionutil.EvaluateConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import android.content.pm.ActivityInfo.*
import com.tools.calculator.util.*

class CalculatorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalculatorBinding
    private var decimalSeparatorSymbol = NumberFormatter.getDecimalSeparatorSymbol()
    private var groupingSeparatorSymbol = NumberFormatter.getGroupingSeparatorSymbol()
    private var isEqualLastAction = false
    private var isDegreeModeActivated = true // Set degree by default
    private var isFractionModeActivated = false // set normal mode by default
    private var errorStatusOld = false
    private var isInSimpleMode = false // set Modern Type by default
    private val fraction = Fraction()
    private lateinit var config: EvaluateConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setEventClick(binding.root)
    }

    private fun setupView() {
        config = EvaluateConfig.loadFromSetting(
            applicationContext
        )

        // Disable the keyboard on display EditText
        binding.input.showSoftInputOnFocus = false

        updateState()

        // Detect long press
        binding.btnBackspace.setOnLongClickListener {
            binding.input.setText("")
            binding.resultDisplay.setText("")
            true
        }

        // Focus input view by default
        binding.input.requestFocus()

        // Make the input take the whole width of the screen by default
        val screenWidthPx = resources.displayMetrics.widthPixels
        binding.input.minWidth =
            screenWidthPx - (binding.input.paddingRight + binding.input.paddingLeft) // remove the paddingHorizontal

        // Do not clear after equal button if you move the cursor
        binding.input.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                    isEqualLastAction = false
                }
                if (!binding.input.isCursorVisible) {
                    binding.input.isCursorVisible = true
                }
            }
        }

        // LongClick on result to copy it
        binding.resultDisplay.setOnLongClickListener {
            when {
                binding.resultDisplay.text.toString() != "" -> {
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboardManager.setPrimaryClip(
                        ClipData.newPlainText(
                            "Copied result", binding.resultDisplay.text
                        )
                    )

                    // Only show a  Toast for Android 12 and lower
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        Toast.makeText(
                            this@CalculatorActivity,
                            "Copied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
                else -> false
            }
        }


        // Handle cut & paste events to update resultDisplay
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding.input.addTextChangedListener(object : TextWatcher {
            private var beforeTextLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                beforeTextLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val afterTextLength = s?.length ?: 0

                // If the afterTextLength is equals to 0 we have to clear resultDisplay
                if (afterTextLength == 0) {
                    binding.resultDisplay.setText("")
                }

                /*
                    We check if the length of the text entered into the EditText
                    is greater than the length of the text before the change (beforeTextLength)
                    by more than 1 character. If it is, we assume that this is a paste event.
                 */
                val clipData = clipboardManager.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val clipText =
                        clipData.getItemAt(0).coerceToText(this@CalculatorActivity).toString()

                    if (s != null) {
                        val newValue = s.subSequence(start, start + count).toString()
                        // Support 1+ new character if it is equals to the lastest element from the clipboard
                        if ((afterTextLength - beforeTextLength > 1) || (afterTextLength - beforeTextLength >= 1 && clipText == newValue)) {
                            // Handle paste event
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
    }

    private fun setEventClick(view: View) {
        if (binding.btnChangeType != null) {
            binding.btnChangeType!!.setOnClickListener {
                if (isInSimpleMode) changeToModernType()
                else changeToSimpleType()
            }
        }

        binding.btnOne.setOnClickListener {
            updateDisplay(view, getString(R.string.one))
        }

        binding.btnTwo.setOnClickListener {
            updateDisplay(view, getString(R.string.two))
        }

        binding.btnThree.setOnClickListener {
            updateDisplay(view, getString(R.string.three))
        }

        binding.btnFour.setOnClickListener {
            updateDisplay(view, getString(R.string.four))
        }

        binding.btnFive.setOnClickListener {
            updateDisplay(view, getString(R.string.five))
        }

        binding.btnSix.setOnClickListener {
            updateDisplay(view, getString(R.string.six))
        }

        binding.btnSeven.setOnClickListener {
            updateDisplay(view, getString(R.string.seven))
        }

        binding.btnEight.setOnClickListener {
            updateDisplay(view, getString(R.string.eight))
        }

        binding.btnNine.setOnClickListener {
            updateDisplay(view, getString(R.string.nine))
        }

        binding.btnZero.setOnClickListener {
            updateDisplay(view, getString(R.string.zero))
        }

        binding.btnDot.setOnClickListener {
            updateDisplay(view, decimalSeparatorSymbol)
        }

        binding.btnBackspace.setOnClickListener {
            backspaceButton(view)
        }

        binding.btnEquals.setOnClickListener {
            equalsButton(view)
        }

        binding.btnAdd.setOnClickListener {
            addSymbol(view, "+")
        }

        binding.btnSubtract.setOnClickListener {
            addSymbol(view, "-")
        }

        binding.btnDivide.setOnClickListener {
            addSymbol(view, "÷")
        }

        binding.btnMultiply.setOnClickListener {
            addSymbol(view, "×")
        }

        binding.btnExponent.setOnClickListener {
            addSymbol(view, "^")
        }

        binding.btnParentheses.setOnClickListener {
            parenthesesButton(view)
        }

        binding.btnAC.setOnClickListener {
            clearButton(view)
        }

        binding.btnDeg.setOnClickListener {
            degreeButton(view)
        }

        binding.btnSquareRoot.setOnClickListener {
            updateDisplay(view, "√")
        }

        binding.btnCubeRoot.setOnClickListener {
            updateDisplay(view, "∛")
        }

        binding.btnDivideBy100.setOnClickListener {
            updateDisplay(view, "%")
        }

        binding.btnSin.setOnClickListener {
            updateDisplay(view, "sin(")
        }

        binding.btnCos.setOnClickListener {
            updateDisplay(view, "cos(")
        }

        binding.btnTan.setOnClickListener {
            updateDisplay(view, "tan(")
        }

        binding.btnLogarit.setOnClickListener {
            updateDisplay(view, "log(")
        }

        binding.btnNaturalLogarit.setOnClickListener {
            updateDisplay(view, "ln(")
        }

        binding.btnAbsolute.setOnClickListener {
            updateDisplay(view, "|")
        }

        binding.btnE.setOnClickListener {
            updateDisplay(view, "e")
        }

        binding.btnPi.setOnClickListener {
            updateDisplay(view, "π")
        }

        binding.btnPhi.setOnClickListener {
            updateDisplay(view, "φ")
        }

        binding.btnChangeMode.setOnClickListener {
            changeMode(view)
        }
    }


    private fun changeMode(view: View) {
        keyVibration(view)

        if (binding.btnChangeMode.text.toString() == "x/y") {
            binding.btnChangeMode.text = "x.y"
            isFractionModeActivated = true
        } else {
            binding.btnChangeMode.text = "x/y"
            isFractionModeActivated = false
        }

        updateResultDisplay()
    }

    private fun updateDisplay(view: View, value: String) {
        // Reset input with current number if following "equal"
        if (isEqualLastAction) {
            val anyNumber = "0123456789$decimalSeparatorSymbol".toCharArray().map {
                it.toString()
            }
            if (anyNumber.contains(value)) {
                binding.input.setText("")
            } else {
                binding.input.setSelection(binding.input.text.length)
                binding.inputHorizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
            }
            isEqualLastAction = false
        }

        if (!binding.input.isCursorVisible) {
            binding.input.isCursorVisible = true
        }

        lifecycleScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                // Vibrate when key pressed
                keyVibration(view)
            }

            try {
                val formerValue = binding.input.text.toString()
                val cursorPosition = binding.input.selectionStart
                val leftValue = formerValue.subSequence(0, cursorPosition).toString()
                val rightValue =
                    formerValue.subSequence(cursorPosition, formerValue.length).toString()

                val newValue = leftValue + value + rightValue

                var newValueFormatted = NumberFormatter.format(newValue)

                withContext(Dispatchers.Main) {
                    // Avoid two decimalSeparator in the same number
                    // 1. When you click on the decimalSeparator button
                    if (value == decimalSeparatorSymbol && decimalSeparatorSymbol in binding.input.text.toString()) {
                        try {
                            if (binding.input.text.toString().isNotEmpty()) {
                                var lastNumberBefore = ""
                                if (cursorPosition > 0 && binding.input.text.toString()
                                        .substring(0, cursorPosition)
                                        .last() in "0123456789\\$decimalSeparatorSymbol"
                                ) {
                                    lastNumberBefore = NumberFormatter.extractNumbers(
                                        binding.input.text.toString().substring(0, cursorPosition)
                                    ).last()
                                }
                                var firstNumberAfter = ""
                                if (cursorPosition < binding.input.text.length - 1) {
                                    firstNumberAfter = NumberFormatter.extractNumbers(
                                        binding.input.text.toString()
                                            .substring(cursorPosition, binding.input.text.length)
                                    ).first()
                                }
                                if (decimalSeparatorSymbol in lastNumberBefore || decimalSeparatorSymbol in firstNumberAfter) {
                                    return@withContext
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                    // 2. When you click on a former calculation from the history
                    if (binding.input.text.isNotEmpty() && cursorPosition > 0 && decimalSeparatorSymbol in value && value != decimalSeparatorSymbol // The value should not be *only* the decimal separator
                    ) {
                        if (NumberFormatter.extractNumbers(value).isNotEmpty()) {
                            val firstValueNumber = NumberFormatter.extractNumbers(value).first()
                            val lastValueNumber = NumberFormatter.extractNumbers(value).last()
                            if (decimalSeparatorSymbol in firstValueNumber || decimalSeparatorSymbol in lastValueNumber) {
                                var numberBefore =
                                    binding.input.text.toString().substring(0, cursorPosition)
                                if (numberBefore.last() !in "()*-/+^!√πe") {
                                    numberBefore =
                                        NumberFormatter.extractNumbers(numberBefore).last()
                                }
                                var numberAfter = ""
                                if (cursorPosition < binding.input.text.length - 1) {
                                    numberAfter = NumberFormatter.extractNumbers(
                                        binding.input.text.toString()
                                            .substring(cursorPosition, binding.input.text.length)
                                    ).first()
                                }
                                var tmpValue = value
                                var numberBeforeParenthesisLength = 0
                                if (decimalSeparatorSymbol in numberBefore) {
                                    numberBefore = "($numberBefore)"
                                    numberBeforeParenthesisLength += 2
                                }
                                if (decimalSeparatorSymbol in numberAfter) {
                                    tmpValue = "($value)"
                                }
                                val tmpNewValue = binding.input.text.toString().substring(
                                    0,
                                    (cursorPosition + numberBeforeParenthesisLength - numberBefore.length)
                                ) + numberBefore + tmpValue + rightValue
                                newValueFormatted = NumberFormatter.format(tmpNewValue)
                            }
                        }
                    }

                    try {
                        // Update Display
                        binding.input.setText(newValueFormatted)

                        // Increase cursor position
                        val cursorOffset = newValueFormatted.length - newValue.length
                        binding.input.setSelection(cursorPosition + value.length + cursorOffset)

                        // Update resultDisplay
                        updateResultDisplay()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    private fun updateResultDisplay() {
        lifecycleScope.launch(Dispatchers.Default) {
            // Reset text color
            setErrorColor(false)

            val calculation = binding.input.text.toString()

            if (calculation != "") {
                division_by_0 = false
                domain_error = false
                syntax_error = false

                val calculationTmp = Expression().getCleanExpression(binding.input.text.toString())
                var result = Calculator().evaluate(calculationTmp, isDegreeModeActivated)

                // If result is a number and it is finite
                if (!result.isNaN() && result.isFinite()) {
                    // Round at 10^-12
                    result = roundResult(result)
                    var formattedResult = NumberFormatter.format(
                        result.toString().replace(".", NumberFormatter.getDecimalSeparatorSymbol())
                    )

                    // If result = -0, change it to 0
                    if (result == -0.0) {
                        result = 0.0
                    }
                    // If the double ends with .0 we remove the .0
                    if ((result * 10) % 10 == 0.0) {
                        val resultString = String.format(Locale.US, "%.0f", result)
                        formattedResult = NumberFormatter.format(resultString)

                        withContext(Dispatchers.Main) {
                            if (formattedResult != calculation) {
                                if (!isFractionModeActivated) binding.resultDisplay.setText("="+" "+formattedResult)
                                else binding.resultDisplay.setText("="+" "+fraction.evaluateWithResultAsTex(calculationTmp, config))
                            } else {
                                binding.resultDisplay.setText("")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            if (formattedResult != calculation) {
                                if (!isFractionModeActivated) binding.resultDisplay.setText("="+" "+formattedResult)
                                else binding.resultDisplay.setText("="+" "+fraction.evaluateWithResultAsTex(calculationTmp, config))
                            } else {
                                binding.resultDisplay.setText("")
                            }
                        }
                    }
                } else withContext(Dispatchers.Main) {
                    if (result.isInfinite() && !division_by_0 && !domain_error) {
                        if (result < 0) binding.resultDisplay.setText("-" + "infinity")
                        else binding.resultDisplay.setText("value too large")
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.resultDisplay.setText("")
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.resultDisplay.setText("")
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun degreeButton(view: View) {
        keyVibration(view)

        if (binding.btnDeg.text.toString() == "deg") {
            binding.btnDeg.text = "rad"
            isDegreeModeActivated = false
        } else {
            binding.btnDeg.text = "deg"
            isDegreeModeActivated = true
        }

        updateResultDisplay()
    }

    private fun clearButton(view: View) {
        keyVibration(view)
        binding.input.setText("")
        binding.resultDisplay.setText("")
    }

    private fun parenthesesButton(view: View) {
        val cursorPosition = binding.input.selectionStart
        val textLength = binding.input.text.length

        var openParentheses = 0
        var closeParentheses = 0

        val text = binding.input.text.toString()

        for (i in 0 until cursorPosition) {
            if (text[i] == '(') {
                openParentheses += 1
            }
            if (text[i] == ')') {
                closeParentheses += 1
            }
        }

        if (!(textLength > cursorPosition && binding.input.text.toString()[cursorPosition] in "×÷+-^") && (openParentheses == closeParentheses || binding.input.text.toString()[cursorPosition - 1] == '(' || binding.input.text.toString()[cursorPosition - 1] in "×÷+-^")) {
            updateDisplay(view, "(")
        } else {
            updateDisplay(view, ")")
        }

        updateResultDisplay()
    }


    private fun addSymbol(view: View, currentSymbol: String) {
        // Get input text length
        val textLength = binding.input.text.length

        // If the input is not empty
        if (textLength > 0) {
            // Get cursor's current position
            val cursorPosition = binding.input.selectionStart

            // Get next / previous characters relative to the cursor
            val nextChar =
                if (textLength - cursorPosition > 0) binding.input.text[cursorPosition].toString() else "0" // use "0" as default like it's not a symbol
            val previousChar =
                if (cursorPosition > 0) binding.input.text[cursorPosition - 1].toString() else "0"

            if (currentSymbol != previousChar // Ignore multiple presses of the same button
                && currentSymbol != nextChar && previousChar != "√" // No symbol can be added on an empty square root
                && previousChar != decimalSeparatorSymbol // Ensure that the previous character is not a comma
                && nextChar != decimalSeparatorSymbol // Ensure that the next character is not a comma
                && (previousChar != "(" // Ensure that we are not at the beginning of a parenthesis
                        || currentSymbol == "-")
            ) { // Minus symbol is an override
                // If previous character is a symbol, replace it
                if (previousChar.matches("[+\\-÷×^]".toRegex())) {
                    keyVibration(view)

                    val leftString =
                        binding.input.text.subSequence(0, cursorPosition - 1).toString()
                    val rightString =
                        binding.input.text.subSequence(cursorPosition, textLength).toString()

                    // Add a parenthesis if there is another symbol before minus
                    if (currentSymbol == "-") {
                        if (previousChar in "+-") {
                            binding.input.setText(leftString + currentSymbol + rightString)
                            binding.input.setSelection(cursorPosition)
                        } else {
                            binding.input.setText(leftString + previousChar + currentSymbol + rightString)
                            binding.input.setSelection(cursorPosition + 1)
                        }
                    } else if (cursorPosition > 1 && binding.input.text[cursorPosition - 2] != '(') {
                        binding.input.setText(leftString + currentSymbol + rightString)
                        binding.input.setSelection(cursorPosition)
                    } else if (currentSymbol == "+") {
                        binding.input.setText(leftString + rightString)
                        binding.input.setSelection(cursorPosition - 1)
                    }
                }
                // If next character is a symbol, replace it
                else if (nextChar.matches("[+\\-÷×^%!]".toRegex()) && currentSymbol != "%") { // Make sure that percent symbol doesn't replace succeeding symbols
                    keyVibration(view)

                    val leftString = binding.input.text.subSequence(0, cursorPosition).toString()
                    val rightString =
                        binding.input.text.subSequence(cursorPosition + 1, textLength).toString()

                    if (cursorPosition > 0 && previousChar != "(") {
                        binding.input.setText(leftString + currentSymbol + rightString)
                        binding.input.setSelection(cursorPosition + 1)
                    } else if (currentSymbol == "+") binding.input.setText(leftString + rightString)
                }
                // Otherwise just update the display
                else if (cursorPosition > 0 || nextChar != "0" && currentSymbol == "-") {
                    updateDisplay(view, currentSymbol)
                } else keyVibration(view)
            } else keyVibration(view)

            // Update resultDisplay so changes are reflected
            updateResultDisplay()
        } else { // Allow minus symbol, even if the input is empty
            if (currentSymbol == "-") updateDisplay(view, currentSymbol)
            else keyVibration(view)
        }
    }


    @SuppressLint("SetTextI18n")
    fun equalsButton(view: View) {
        lifecycleScope.launch(Dispatchers.Default) {
            keyVibration(view)

            val calculation = binding.input.text.toString()

            if (calculation != "") {
                division_by_0 = false
                domain_error = false
                syntax_error = false

                val calculationTmp = Expression().getCleanExpression(binding.input.text.toString())
                val result = roundResult((Calculator().evaluate(calculationTmp, isDegreeModeActivated)))
                var resultString = result.toString()
                var formattedResult = NumberFormatter.format(
                    resultString.replace(
                        ".", NumberFormatter.getDecimalSeparatorSymbol()
                    )
                )

                // If result is a number and it is finite
                if (!result.isNaN() && result.isFinite()) {
                    // If there is an unused 0 at the end, remove it : 2.0 -> 2
                    if ((result * 10) % 10 == 0.0) {
                        resultString = String.format(Locale.US, "%.0f", result)
                        formattedResult = NumberFormatter.format(resultString)
                    }

                    // Hide the cursor before updating binding.input to avoid weird cursor movement
                    withContext(Dispatchers.Main) {
                        binding.input.isCursorVisible = false
                    }

                    // Display result
                    withContext(Dispatchers.Main) { binding.input.setText(formattedResult) }

                    // Set cursor
                    withContext(Dispatchers.Main) {
                        // Scroll to the end
                        binding.input.setSelection(binding.input.length())

                        // Hide the cursor (do not remove this, it's not a duplicate)
                        binding.input.isCursorVisible = false

                        // Clear resultDisplay
                        binding.resultDisplay.setText("")
                    }

                    if (calculation != formattedResult) {
                        // handle history
                    }
                    isEqualLastAction = true
                } else {
                    withContext(Dispatchers.Main) {
                        if (syntax_error) {
                            setErrorColor(true)
                            binding.resultDisplay.setText("syntax error")
                        } else if (domain_error) {
                            setErrorColor(true)
                            binding.resultDisplay.setText("domain error")
                        } else if (result.isInfinite()) {
                            if (division_by_0) {
                                setErrorColor(true)
                                binding.resultDisplay.setText("division by 0")
                            } else if (result < 0) binding.resultDisplay.setText("-" + "infinity")
                            else binding.resultDisplay.setText("Value too large")
                        } else if (result.isNaN()) {
                            setErrorColor(true)
                            binding.resultDisplay.setText("Math error")
                        } else {
                            binding.resultDisplay.setText(formattedResult)
                            isEqualLastAction = true // Do not clear the calculation (if you click into a number) if there is an error
                        }
                    }
                }

            } else {
                withContext(Dispatchers.Main) { binding.resultDisplay.setText("") }
            }
        }
    }

    private fun backspaceButton(view: View) {
        keyVibration(view)

        var cursorPosition = binding.input.selectionStart
        val textLength = binding.input.text.length
        var newValue = ""
        var isFunction = false
        var functionLength = 0

        if (isEqualLastAction) {
            cursorPosition = textLength
        }

        if (cursorPosition != 0 && textLength != 0) {
            // Check if it is a function to delete
            val functionsList = listOf(
                "cos⁻¹(",
                "sin⁻¹(",
                "tan⁻¹(",
                "cos(",
                "sin(",
                "tan(",
                "ln(",
                "log(",
                "exp(",
                "cosh⁻¹(",
                "sinh⁻¹(",
                "tanh⁻¹("
            )
            for (function in functionsList) {
                try {
                    val leftPart = binding.input.text.subSequence(0, cursorPosition).toString()
                    if (leftPart.endsWith(function)) {
                        newValue =
                            binding.input.text.subSequence(0, cursorPosition - function.length)
                                .toString() + binding.input.text.subSequence(
                                cursorPosition,
                                textLength
                            )
                                .toString()
                        isFunction = true
                        functionLength = function.length - 1
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            // Else
            if (!isFunction) {
                // remove the grouping separator
                val leftPart = binding.input.text.subSequence(0, cursorPosition).toString()
                val leftPartWithoutSpaces = leftPart.replace(groupingSeparatorSymbol, "")
                functionLength = leftPart.length - leftPartWithoutSpaces.length

                try {
                    newValue =
                        leftPartWithoutSpaces.subSequence(0, leftPartWithoutSpaces.length - 1)
                            .toString() + binding.input.text.subSequence(cursorPosition, textLength)
                            .toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            val newValueFormatted = NumberFormatter.format(newValue)
            var cursorOffset = newValueFormatted.length - newValue.length
            if (cursorOffset < 0) cursorOffset = 0

            try {
                binding.input.setText(newValueFormatted)
                binding.input.setSelection((cursorPosition - 1 + cursorOffset - functionLength).takeIf { it > 0 }
                    ?: 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        updateResultDisplay()
    }

    private fun setErrorColor(errorStatus: Boolean) {
        // Only run if the color needs to be updated
        try {
            if (errorStatus != errorStatusOld) {
                // Set error color
                if (errorStatus) {
                    binding.input.setTextColor(
                        ContextCompat.getColor(
                            this@CalculatorActivity, R.color.red
                        )
                    )
                    binding.resultDisplay.setTextColor(
                        ContextCompat.getColor(
                            this@CalculatorActivity, R.color.red
                        )
                    )
                }
                // Clear error color
                else {
                    binding.input.setTextColor(
                        ContextCompat.getColor(
                            this@CalculatorActivity, R.color.text
                        )
                    )
                    binding.resultDisplay.setTextColor(
                        ContextCompat.getColor(
                            this@CalculatorActivity, R.color.black
                        )
                    )
                }
                errorStatusOld = errorStatus
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun keyVibration(view: View) {
        if (MyPreferences(this).vibrationMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }
    }

    private fun roundResult(result: Double): Double {
        if (result.isNaN() || result.isInfinite()) {
            return result
        }
        return BigDecimal(result).setScale(12, RoundingMode.HALF_EVEN).toDouble()
    }

    private fun scientificSpace(isScientific: Boolean) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height: Int = displayMetrics.heightPixels

        val params = binding.viewSpace.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        if (isScientific) params.height = (height*0.15).toInt()
        else params.height = (height*0.2).toInt()
        binding.viewSpace.layoutParams = params
    }

    private fun changeToSimpleType() {
        binding.btnLogarit.visibility = View.GONE
        binding.btnDeg.visibility = View.GONE
        binding.btnSin.visibility = View.GONE
        binding.btnCos.visibility = View.GONE
        binding.btnTan.visibility = View.GONE
        binding.btnNaturalLogarit.visibility = View.GONE
        binding.btnPi.visibility = View.GONE
        binding.btnChangeMode.visibility = View.GONE
        binding.btnExponent.visibility = View.GONE
        binding.btnE.visibility = View.GONE
        binding.btnSquareRoot.visibility = View.GONE
        binding.btnCubeRoot.visibility = View.GONE
        binding.btnAbsolute.visibility = View.GONE
        binding.btnPhi.visibility = View.GONE
        binding.btnDivideBy100.visibility = View.GONE
        isInSimpleMode = true
        scientificSpace(false)
    }

    private fun changeToModernType() {
        binding.btnLogarit.visibility = View.VISIBLE
        binding.btnDeg.visibility = View.VISIBLE
        binding.btnSin.visibility = View.VISIBLE
        binding.btnCos.visibility = View.VISIBLE
        binding.btnTan.visibility = View.VISIBLE
        binding.btnNaturalLogarit.visibility = View.VISIBLE
        binding.btnPi.visibility = View.VISIBLE
        binding.btnChangeMode.visibility = View.VISIBLE
        binding.btnExponent.visibility = View.VISIBLE
        binding.btnE.visibility = View.VISIBLE
        binding.btnSquareRoot.visibility = View.VISIBLE
        binding.btnCubeRoot.visibility = View.VISIBLE
        binding.btnAbsolute.visibility = View.VISIBLE
        binding.btnPhi.visibility = View.VISIBLE
        binding.btnDivideBy100.visibility = View.VISIBLE
        isInSimpleMode = false
        scientificSpace(true)
    }

    private fun updateState() {
        if (isDegreeModeActivated) {
            binding.btnDeg.text = "rad"
        } else {
            binding.btnDeg.text = "deg"
        }

        if (isFractionModeActivated) {
            binding.btnChangeMode.text = "x.y"
        } else {
            binding.btnChangeMode.text = "x/y"
        }
    }
}