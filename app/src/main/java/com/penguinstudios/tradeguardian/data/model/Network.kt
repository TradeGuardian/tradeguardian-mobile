package com.penguinstudios.tradeguardian.data.model

import com.penguinstudios.tradeguardian.R

enum class Network(
    val id: Int,
    val baseUrl: String,
    val networkName: String,
    val networkTokenName: String,
    val chainId: Int,
    val explorerUrl: String,
    val networkImage: Int,
    val priceQuerySymbol: String,
    val explorerName: String
) {
    BOTANIX_TESTNET(
        0,
         "https://node.botanixlabs.dev",
        "Botanix Testnet",
        "BTC",
        3636,
        "https://blockscout.botanixlabs.dev/address/",
        R.drawable.botanix,
        "BTCUSDT",
        "Blockscout"
    ),
    SEPOLIA_TESTNET(
        1,
        "https://ethereum-sepolia-rpc.publicnode.com",
        "Sepolia Testnet",
        "ETH",
        11155111,
        "https://sepolia.etherscan.io/",
        R.drawable.ethereum,
        "ETHUSDT",
        "Etherscan"
    ),
    ETHEREUM_MAINNET(
        2,
        "https://ethereum-rpc.publicnode.com",
        "Ethereum Mainnet",
        "ETH",
        1,
        "https://etherscan.io/",
        R.drawable.ethereum,
        "ETHUSDT",
        "Etherscan"
    );

    companion object {
        fun getNetworkById(id: Int): Network {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalStateException("Invalid id: $id")
        }

        fun getFirstAvailableNetwork(): Network {
            return values().firstOrNull()
                ?: throw NoSuchElementException("No networks are available.")
        }
    }
}
