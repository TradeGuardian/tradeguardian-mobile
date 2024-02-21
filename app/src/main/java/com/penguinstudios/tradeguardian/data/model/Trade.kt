package com.penguinstudios.tradeguardian.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.penguinstudios.tradeguardian.util.Constants
import com.penguinstudios.tradeguardian.util.WalletUtil
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(tableName = "trades")
class Trade(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "network") val networkId: Int,
    @ColumnInfo(name = "contract_address") val contractAddress: String,
    @ColumnInfo(name = "contract_status_id") val contractStatusId: Int,
    @ColumnInfo(name = "date_created_millis") val dateCreatedMillis: Long,
    @ColumnInfo(name = "item_price_wei") val itemPriceWei: Long,
    @ColumnInfo(name = "gas_cost_wei") val gasCostWei: Long,
    @ColumnInfo(name = "user_role_id") val userRoleId: Int,
    @ColumnInfo(name = "user_wallet_address") val userWalletAddress: String,
    @ColumnInfo(name = "counter_party_role_id") val counterPartyRoleId: Int,
    @ColumnInfo(name = "counter_party_wallet_address") val counterPartyWalletAddress: String,
    @ColumnInfo(name = "description") val description: String
) {
    interface NetworkStep {
        fun network(network: Network): ContractAddressStep
    }

    interface ContractAddressStep {
        fun contractAddress(address: String): ContractStatusIdStep
    }

    interface ContractStatusIdStep {
        fun contractStatus(status: ContractStatus): DateCreatedMillisStep
    }

    interface DateCreatedMillisStep {
        fun dateCreatedSeconds(dateCreatedSeconds: BigInteger): ItemPriceWeiStep
    }

    interface ItemPriceWeiStep {
        fun itemPriceWei(priceWei: BigInteger): GasCostWeiStep
    }

    interface GasCostWeiStep {
        fun gasCostWei(costWei: BigInteger): UserRoleIdStep
    }

    interface UserRoleIdStep {
        fun userRole(role: UserRole): UserWalletAddressStep
    }

    interface UserWalletAddressStep {
        fun userWalletAddress(address: String): CounterPartyRoleIdStep
    }

    interface CounterPartyRoleIdStep {
        fun counterPartyRole(role: UserRole): CounterPartyWalletAddressStep
    }

    interface CounterPartyWalletAddressStep {
        fun counterPartyWalletAddress(address: String): DescriptionStep
    }

    interface DescriptionStep {
        fun description(description: String): BuildStep
    }

    interface BuildStep {
        fun build(): Trade
    }

    companion object {
        private val dateFormatter = SimpleDateFormat(Constants.DATE_PATTERN, Locale.US)
        fun builder(): NetworkStep = Builder()
    }

    private class Builder : NetworkStep, ContractAddressStep, ContractStatusIdStep,
        DateCreatedMillisStep,
        ItemPriceWeiStep, GasCostWeiStep, UserRoleIdStep, UserWalletAddressStep,
        CounterPartyRoleIdStep,
        CounterPartyWalletAddressStep, DescriptionStep, BuildStep {
        private var id: Int = 0 //Placeholder, will be replaced by Room upon insertion
        private var networkId: Int = 0
        private lateinit var contractAddress: String
        private var contractStatusId: Int = 0
        private var dateCreatedMillis: Long = 0L
        private var itemPriceWei: Long = 0L
        private var gasCostWei: Long = 0L
        private var userRoleId: Int = 0
        private lateinit var userWalletAddress: String
        private var counterPartyRoleId: Int = 0
        private lateinit var counterPartyWalletAddress: String
        private lateinit var description: String

        override fun network(network: Network) = apply {
            this.networkId = network.id
        }

        override fun contractAddress(address: String) = apply {
            this.contractAddress = address
        }

        override fun contractStatus(status: ContractStatus) = apply {
            this.contractStatusId = status.id
        }

        override fun dateCreatedSeconds(dateCreatedSeconds: BigInteger) = apply {
            //Date format requires milliseconds
            this.dateCreatedMillis = dateCreatedSeconds.toLong() * 1000
        }

        override fun itemPriceWei(priceWei: BigInteger) = apply {
            this.itemPriceWei = priceWei.toLong()
        }

        override fun gasCostWei(costWei: BigInteger) = apply {
            this.gasCostWei = costWei.toLong()
        }

        override fun userRole(role: UserRole) = apply {
            this.userRoleId = role.id
        }

        override fun userWalletAddress(address: String) = apply {
            this.userWalletAddress = address
        }

        override fun counterPartyRole(role: UserRole) = apply {
            this.counterPartyRoleId = role.id
        }

        override fun counterPartyWalletAddress(address: String) = apply {
            this.counterPartyWalletAddress = address
        }

        override fun description(description: String) = apply {
            this.description = description
        }

        override fun build(): Trade {
            return Trade(
                id, //Will be ignored by Room on insert due to autoGenerate
                networkId,
                contractAddress,
                contractStatusId,
                dateCreatedMillis,
                itemPriceWei,
                gasCostWei,
                userRoleId,
                userWalletAddress,
                counterPartyRoleId,
                counterPartyWalletAddress,
                description
            )
        }
    }

    fun getFormattedItemPrice(): String {
        return WalletUtil.weiToEther(itemPriceWei.toBigInteger()).toString() + " " +
                Network.getNetworkById(networkId).networkTokenName
    }

    fun getFormattedGasCost(): String {
        return WalletUtil.weiToEther(gasCostWei.toBigInteger()).toString() + " " +
                Network.getNetworkById(networkId).networkTokenName
    }

    fun getFormattedDateCreated(): String {
        return dateFormatter.format(dateCreatedMillis)
    }

    fun getNetwork(): Network {
        return Network.getNetworkById(networkId)
    }

    fun getUserRole(): UserRole {
        return UserRole.getUserRoleById(userRoleId)
    }

    fun getCounterpartyRole(): UserRole {
        return UserRole.getUserRoleById(counterPartyRoleId)
    }

    fun getSellerDepositAmount(): BigInteger {
        return BigInteger.valueOf(itemPriceWei)
            .multiply(Constants.SELLER_DEPOSIT_MULTIPLIER.toBigInteger())
    }

    fun getBuyerDepositAmount(): BigInteger {
        return BigInteger.valueOf(itemPriceWei)
            .multiply(Constants.BUYER_DEPOSIT_MULTIPLIER.toBigInteger())
    }

    fun getFormattedSellerDepositAmountEther(): String {
        return WalletUtil.weiToEther(getSellerDepositAmount())
            .toString() + " " + getNetwork().networkTokenName
    }

    fun getFormattedBuyerDepositAmountEther(): String {
        return WalletUtil.weiToEther(getBuyerDepositAmount())
            .toString() + " " + getNetwork().networkTokenName
    }

    private fun calculateSuccessfulTxFeePerParty(): BigInteger {
        val onePercentOfItemPrice = BigInteger.valueOf(itemPriceWei)
            .multiply(BigInteger.valueOf(Constants.FEE_PERCENTAGE.toLong())) / BigInteger.valueOf(
            100
        )
        return onePercentOfItemPrice.divide(BigInteger.valueOf(2))
    }

    private fun getAmountReturnedToSeller(): BigInteger {
        return BigInteger.valueOf(itemPriceWei) - calculateSuccessfulTxFeePerParty()
    }

    private fun getAmountReturnedToBuyer(): BigInteger {
        return BigInteger.valueOf(itemPriceWei) - calculateSuccessfulTxFeePerParty()
    }

    fun getFormattedAmountReturnedToSeller(): String {
        return WalletUtil.weiToEther(getAmountReturnedToSeller())
            .toString() + " " + getNetwork().networkTokenName
    }

    fun getFormattedAmountReturnedToBuyer(): String {
        return WalletUtil.weiToEther(getAmountReturnedToBuyer())
            .toString() + " " + getNetwork().networkTokenName
    }

    fun getFormattedPercentFeePerParty(): String {
        return WalletUtil.weiToEther(calculateSuccessfulTxFeePerParty())
            .toString() + " " + getNetwork().networkTokenName
    }
}
