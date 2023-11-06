package org.satochip.satodime.data

import android.util.Log
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.InputStream//TODO platform specific
import java.io.OutputStream

private const val TAG = "CardAuthSerializer"

@Suppress("BlockingMethodInNonBlockingContext")
object CardsAuthSerializer : Serializer<List<CardAuth>> {
    override val defaultValue: List<CardAuth>
        get() = listOf()

    //    @OptIn(ExperimentalSerializationApi::class) //decodeFromStream is still experimental
    override suspend fun readFrom(input: InputStream): List<CardAuth> {
        return try {
            Json.decodeFromString(
                deserializer = ListSerializer(CardAuth.serializer()),
                string = input.readBytes().decodeToString()
            )
//            Json.decodeFromStream(
//                ListSerializer(Vault.serializer()),
//                input
//            )
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to read cards auth.")
            Log.e(TAG, Log.getStackTraceString(e))
            defaultValue
        }
    }

    override suspend fun writeTo(t: List<CardAuth>, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = ListSerializer(CardAuth.serializer()),
                value = t
            ).encodeToByteArray()
        )
    }
}