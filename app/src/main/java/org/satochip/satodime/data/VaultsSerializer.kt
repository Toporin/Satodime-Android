package org.satochip.satodime.data

import android.util.Log
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import java.io.InputStream//TODO platform specific
import java.io.OutputStream

private const val TAG = "VaultsSerializer"
// TODO: deprecate

//@Suppress("BlockingMethodInNonBlockingContext")
//object VaultsSerializer : Serializer<List<Vault?>> {
//    override val defaultValue: List<Vault?>
//        get() = listOf()
//
////    @OptIn(ExperimentalSerializationApi::class) //decodeFromStream is still experimental
//    override suspend fun readFrom(input: InputStream): List<Vault?> {
//        return try {
//            Json.decodeFromString(
//                deserializer = ListSerializer(Vault.serializer().nullable),
//                string = input.readBytes().decodeToString()
//            )
////            Json.decodeFromStream(
////                ListSerializer(Vault.serializer()),
////                input
////            )
//        } catch (e: SerializationException) {
//            Log.e(TAG, "Failed to read vaults.")
//            Log.e(TAG, Log.getStackTraceString(e))
//            defaultValue
//        }
//    }
//
//    override suspend fun writeTo(t: List<Vault?>, output: OutputStream) {
//        output.write(
//            Json.encodeToString(
//                serializer = ListSerializer(Vault.serializer().nullable),
//                value = t
//            ).encodeToByteArray()
//        )
//    }
//}