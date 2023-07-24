package com.tools.calculator.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object NumberFormatter {
    var decimalSeparatorSymbol = DecimalFormatSymbols.getInstance(Locale.US).decimalSeparator.toString()
    var groupingSeparatorSymbol = DecimalFormatSymbols.getInstance(Locale.US).groupingSeparator.toString()
    private val symbolsEN_US: DecimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US)
    var formatEN_US = DecimalFormat("#,###.#", symbolsEN_US)
    var formatEN_US2 = DecimalFormat("#,###.##", symbolsEN_US)
    var formatEN_US3 = DecimalFormat("#,###.######", symbolsEN_US)
    var formatEN_US4 = DecimalFormat("0",symbolsEN_US)

    private val numberRegex = "(\\d+\\$decimalSeparatorSymbol\\d+)|(\\d+\\$decimalSeparatorSymbol)|(\\$decimalSeparatorSymbol\\d+)|(\\$decimalSeparatorSymbol)|(\\d+)".toRegex()

    @JvmName("getDecimalSeparatorSymbol1")
    fun getDecimalSeparatorSymbol(): String {
        decimalSeparatorSymbol = DecimalFormatSymbols.getInstance(Locale.US).decimalSeparator.toString()
        return decimalSeparatorSymbol
    }

    @JvmName("getGroupingSeparatorSymbol1")
    fun getGroupingSeparatorSymbol(): String {
        groupingSeparatorSymbol = DecimalFormatSymbols.getInstance(Locale.US).groupingSeparator.toString()
        return groupingSeparatorSymbol
    }

    fun format(text: String): String {
        val textNoSeparator = removeSeparators(text)
        val numbersList = extractNumbers(textNoSeparator)
        val numbersWithSeparators = addSeparators(numbersList)
        var textWithSeparators = textNoSeparator
        numbersList.forEachIndexed { index, number ->
            textWithSeparators = textWithSeparators.replace(number, numbersWithSeparators[index])
        }
        return textWithSeparators
    }

    fun extractNumbers(text: String): List<String> {
        val results = numberRegex.findAll(text)
        return results.map { it.value }.toList()
    }

    private fun addSeparators(numbersList: List<String>): List<String> {
        return numbersList.map {
            if (it.contains(decimalSeparatorSymbol)) {
                if (it.first() == decimalSeparatorSymbol[0]) {
                    //this means the floating point number doesn't have integers
                    it
                } else {
                    val integersPart = it.substring(0, it.indexOf(decimalSeparatorSymbol))
                    val fractions = it.substring(it.indexOf(decimalSeparatorSymbol) + 1)
                    formatEN_US.format(integersPart.toBigDecimal()) + decimalSeparatorSymbol + fractions
                }
            } else {
                formatEN_US.format(it.toBigDecimal())
            }
        }
    }

    private fun removeSeparators(text: String): String {
        return text.replace(groupingSeparatorSymbol, "")
    }


}
