package com.penguinstudios.tradeguardian.data.model

enum class Network(val id: Int, val baseUrl: String?, val networkName: String, val chainId: Int, val explorerUrl: String?) {
    NONE_SELECTED(0, null, "Select Network", -1, null),
    TEST_NET(1, "", "t", 1, "");

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
