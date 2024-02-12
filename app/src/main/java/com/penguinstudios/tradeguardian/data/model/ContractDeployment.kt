package com.penguinstudios.tradeguardian.data.model

import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.math.BigInteger

class ContractDeployment private constructor(
    val network: Network,
    val feeRecipientAddress: String,
    val userRole: UserRole,
    val itemPriceDecimal: BigDecimal,
    val itemPriceWei: BigInteger,
    val itemPriceFormatted: String,
    val description: String,
    val userWalletAddress: String,
    val counterPartyAddress: String,
    val sellerAddress: String,
    val buyerAddress: String,
    val counterPartyRole: UserRole
) {
    interface NetworkStep {
        fun network(network: Network): FeeRecipientAddressStep
    }

    interface FeeRecipientAddressStep {
        fun feeRecipientAddress(address: String): UserRoleStep
    }

    interface UserRoleStep {
        fun userRole(userRole: UserRole): ItemPriceStep
    }

    interface ItemPriceStep {
        fun itemPrice(itemPrice: String): DescriptionStep
    }

    interface DescriptionStep {
        fun description(description: String): UserWalletAddressStep
    }

    interface UserWalletAddressStep {
        fun userWalletAddress(address: String): CounterPartyAddressStep
    }

    interface CounterPartyAddressStep {
        fun counterPartyAddress(address: String): BuildStep
    }

    interface BuildStep {
        fun build(): ContractDeployment
    }

    companion object {
        fun builder(): NetworkStep = Builder()
    }

    private class Builder : NetworkStep, FeeRecipientAddressStep, UserRoleStep, ItemPriceStep,
        DescriptionStep, UserWalletAddressStep, CounterPartyAddressStep, BuildStep {
        private lateinit var network: Network
        private lateinit var feeRecipientAddress: String
        private lateinit var userRole: UserRole
        private lateinit var itemPrice: String
        private lateinit var description: String
        private lateinit var userWalletAddress: String
        private lateinit var counterPartyAddress: String

        override fun network(network: Network) = apply { this.network = network }

        override fun feeRecipientAddress(address: String) = apply {
            require(WalletUtils.isValidAddress(address)) { "Not a valid fee recipient address" }
            this.feeRecipientAddress = address
        }

        override fun userRole(userRole: UserRole) = apply {
            require(userRole in UserRole.values()) { "Select your role" }
            this.userRole = userRole
        }

        override fun itemPrice(itemPrice: String) = apply {
            require(itemPrice.isNotEmpty()) { "No item price entered" }
            //Item price must not start with '0' unless it is a decimal number starting with '0.'
            if (itemPrice.startsWith("0") && !itemPrice.matches(Regex("^0\\.\\d+$"))) {
                throw IllegalArgumentException("Invalid item price")
            }

            //Item price must not have trailing decimal points '1.'
            if (itemPrice.matches(Regex(".*\\.$"))) {
                throw IllegalArgumentException("Invalid item price: trailing decimal point")
            }

            this.itemPrice = itemPrice
        }

        override fun description(description: String) = apply {
            require(description.length <= 300) { "Description must be less than 300 characters" }
            this.description = description
        }

        override fun userWalletAddress(address: String) = apply {
            require(address.isNotEmpty()) { "No user wallet address entered" }
            require(WalletUtils.isValidAddress(address)) { "Not a valid user wallet address" }
            this.userWalletAddress = address
        }

        override fun counterPartyAddress(address: String) = apply {
            require(address.isNotEmpty()) { "No counterparty address entered" }
            require(WalletUtils.isValidAddress(address)) { "Not a valid counterparty address" }
            this.counterPartyAddress = address
        }

        override fun build(): ContractDeployment {
            val buyerAddress: String
            val counterPartyRole: UserRole
            val sellerAddress: String

            if (userRole == UserRole.BUYER) {
                buyerAddress = userWalletAddress
                counterPartyRole = UserRole.SELLER
                sellerAddress = counterPartyAddress
            } else {
                sellerAddress = userWalletAddress
                counterPartyRole = UserRole.BUYER
                buyerAddress = counterPartyAddress
            }

            val itemPriceDecimal = try {
                BigDecimal(itemPrice)
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Invalid item price, not a number")
            }

            require(itemPriceDecimal > BigDecimal.ZERO) { "Item price must be greater than 0" }
            val itemPriceWei = itemPriceDecimal.multiply(BigDecimal.TEN.pow(18)).toBigInteger()
            val itemPriceFormatted = itemPrice + " " + network.networkTokenName

            require(WalletUtils.isValidAddress(sellerAddress)) { "Not a valid seller address" }
            require(WalletUtils.isValidAddress(buyerAddress)) { "Not a valid buyer address" }
            require(sellerAddress.lowercase() != buyerAddress.lowercase()) { "Buyer and seller addresses cannot be the same" }

            return ContractDeployment(
                network,
                feeRecipientAddress,
                userRole,
                itemPriceDecimal,
                itemPriceWei,
                itemPriceFormatted,
                description,
                userWalletAddress,
                counterPartyAddress,
                sellerAddress,
                buyerAddress,
                counterPartyRole
            )
        }
    }
}
