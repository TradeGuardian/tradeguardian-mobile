package com.penguinstudios.tradeguardian


import com.penguinstudios.tradeguardian.data.WalletRepository
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.web3j.protocol.Web3j
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
class WalletRepositoryTest {

    @Inject
    private lateinit var web3j: Web3j

    private lateinit var walletRepository: WalletRepository

    @Before
    fun setUp() {
        web3j = Mockito.mock(Web3j::class.java)
        walletRepository = WalletRepository(web3j)
    }

    @Test
    fun `generateMnemonic returns mnemonic with unique words`() {
        val password = "testPassword"
        val filesDirectory = RuntimeEnvironment.getApplication().filesDir
        val mnemonic = walletRepository.generateMnemonic(password, filesDirectory)

        val words = mnemonic.split(" ")
        val uniqueWords = words.toSet()

        assertTrue("Mnemonic should have all unique words", uniqueWords.size == words.size)
    }
}

