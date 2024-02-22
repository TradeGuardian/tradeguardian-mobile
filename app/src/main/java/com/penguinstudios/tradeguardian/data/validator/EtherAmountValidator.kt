package com.penguinstudios.tradeguardian.data.validator

object EtherAmountValidator {

    fun validateAmount(amount: String) {
        require(amount.isNotEmpty()) { "No amount entered" }

        // Item price must not start with '0' unless it is a decimal number starting with '0.'
        if (amount.startsWith("0") && !amount.matches(Regex("^0\\.\\d+$"))) {
            throw IllegalArgumentException("Invalid amount")
        }

        // Item price must not have trailing decimal points '1.'
        if (amount.matches(Regex(".*\\.$"))) {
            throw IllegalArgumentException("Invalid amount: trailing decimal point")
        }
    }
}
