package com.penguinstudios.tradeguardian.ui.createtrade

import com.penguinstudios.tradeguardian.data.ContractDeployment

sealed class CreateTradeUIState {
    data class SuccessDeployContract(val txHash: String, val contractAddress: String) :
        CreateTradeUIState()

    data class ConfirmContractDeployment(
        val contractDeployment: ContractDeployment,
        val itemCostUsd: String?,
        val totalDeploymentGasCostEther: String?
    ) : CreateTradeUIState()

    data class Error(val message: String) : CreateTradeUIState()
}