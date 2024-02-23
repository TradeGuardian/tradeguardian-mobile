package com.penguinstudios.tradeguardian.data.model

enum class Network(
    val id: Int,
    val baseUrl: String,
    val networkName: String,
    val networkTokenName: String,
    val chainId: Int,
    val explorerUrl: String
) {
    TEST_NET(
        0,
        "https://data-seed-prebsc-1-s1.bnbchain.org:8545/",
        "BNB Smart Chain Testnet",
        "BNB",
        97,
        "https://testnet.bscscan.com/address/"
    ),
    MAIN_NET(
        1,
        "https://1rpc.io/bnb",
        "BNB Smart Chain Mainnet",
        "BNB",
        56,
        "https://bscscan.com/address/"
    );

    companion object {
        fun getNetworkById(id: Int): Network {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalStateException("Invalid id: $id")
        }
    }
}
