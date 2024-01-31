package com.penguinstudios.tradeguardian


import com.penguinstudios.tradeguardian.data.SharedPrefManager
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.util.WalletUtil
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.web3j.protocol.Web3j

@RunWith(RobolectricTestRunner::class)
class WalletRepositoryTest {

    private lateinit var web3j: Web3j
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var walletRepository: WalletRepository

    @Before
    fun setUp() {
        web3j = Mockito.mock(Web3j::class.java)
        sharedPrefManager = Mockito.mock(SharedPrefManager::class.java)

        walletRepository = WalletRepository(web3j, sharedPrefManager)
    }

    @Test
    fun `deriveKeyPairFromMnemonic returns same wallet address as generateWallet`() {

    }
}


