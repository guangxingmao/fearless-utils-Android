package jp.co.soramitsu.fearless_utils_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.EncryptionType
import jp.co.soramitsu.fearless_utils.encrypt.KeypairFactory
import jp.co.soramitsu.fearless_utils.encrypt.Signer
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedDecoder
import jp.co.soramitsu.fearless_utils.encrypt.json.JsonSeedEncoder
import jp.co.soramitsu.fearless_utils.ss58.AddressType
import jp.co.soramitsu.fearless_utils.ss58.SS58Encoder
import org.spongycastle.util.encoders.Hex
import java.security.SecureRandom

private val SEED = Hex.decode("3132333435363738393031323334353637383930313233343536373839303132")
private val PASSWORD = "12345"
private val NAME = "name"

private val gson = Gson()
private val ss58 = SS58Encoder()
private val keypairFactory = KeypairFactory()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shouldSignMessage()

        shouldEncodeSr25519Json()
    }

    private fun shouldEncodeSr25519Json() {
        val keypairExpected = keypairFactory.generate(EncryptionType.SR25519, SEED)

        val decoder = JsonSeedDecoder(
            gson,
            ss58,
            keypairFactory
        )

        val encoder = JsonSeedEncoder(gson, ss58, SecureRandom())

        val json = encoder.generate(
            keypair = keypairExpected,
            seed = null,
            password = PASSWORD,
            name = NAME,
            encryptionType = EncryptionType.SR25519,
            addressType = AddressType.WESTEND
        )

        val decoded = decoder.decode(json, PASSWORD)

        with(decoded) {
            require(keypairExpected.publicKey.contentEquals(keypair.publicKey))
            require(keypairExpected.privateKey.contentEquals(keypair.privateKey))
            require(keypairExpected.nonce!!.contentEquals(keypair.nonce!!))
            require(NAME == username)
            require(seed == null)
        }
    }

    private fun shouldSignMessage() {
        val messageHex = "this is a message"

        val keypair = keypairFactory.generate(EncryptionType.SR25519, SEED, "")

        val signer = Signer()

        val result = signer.sign(EncryptionType.SR25519, messageHex.toByteArray(), keypair)

        require(
            signer.verifySr25519(
                messageHex.toByteArray(),
                result.signature,
                keypair.publicKey
            )
        )
    }
}