package com.penguinstudios.tradeguardian.data.model

enum class UserRole(val id: Int, val roleName: String) {
    SELLER(0, "Seller"),
    BUYER(1, "Buyer")
}
