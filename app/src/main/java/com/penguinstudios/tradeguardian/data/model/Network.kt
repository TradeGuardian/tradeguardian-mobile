package com.penguinstudios.tradeguardian.data.model

enum class Network(val id: Int, val baseUrl: String?, val networkName: String, val chainId: Int, val explorerUrl: String?) {
    NONE_SELECTED(0, null, "Select Network", -1, null),
    TEST_NET(1, "https://data-seed-prebsc-1-s1.bnbchain.org:8545/", "BNB Smart Chain Testnet", 97, "https://testnet.bscscan.com/"),
    MAIN_NET(2, "https://1rpc.io/bnb", "BNB Smart Chain Mainnet", 56, "https://bscscan.com/");

    companion object {
        fun getNetworkBySpinnerPosition(position: Int): Network {
            for (network in values()) {
                if (network.id == position) {
                    return network
                }
            }
            throw IllegalStateException("Invalid spinner position")
        }
    }
}
