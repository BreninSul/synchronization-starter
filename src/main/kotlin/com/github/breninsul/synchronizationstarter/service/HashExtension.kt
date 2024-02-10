/*
 * MIT License
 *
 * Copyright (c) 2024 BreninSul
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.breninsul.synchronizationstarter.service

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 *  Constant representing the algorithm to be used for hash generation
 */
const val ALGORITHM = "SHA-1"

/**
 * This extension function generates a unique long value hash for any object using SHA-1.
 *
 * @receiver The object for which the hash is to be generated.
 * @return A unique long value representing the hash of the object.
 */
fun Any.longHash(): Long {
    val bytes = MessageDigest.getInstance(ALGORITHM).digest(this.toBytes()) // generating the hash
    var result = 0L

    // shifting and adding bytes to form a long
    for (i in 0 until Long.SIZE_BYTES) {
        result = (result shl 8) or (bytes[i].toLong() and 0xffL)
    }
    return result
}

/**
 * This extension function converts any object into an array of bytes.
 * It uses different methods of conversion depending on the type of the receiver object.
 *
 * @receiver The object that needs to be converted to a byte array.
 * @return Byte array representation of the object.
 */
private fun Any.toBytes(): ByteArray {
    return when (this) {
        is ByteArray -> this
        is Char -> this.code.toBytes()
        is String -> this.toByteArray()
        is Byte -> ByteBuffer.allocate(1).put(this).array()
        is Short -> ByteBuffer.allocate(2).putShort(this).array()
        is Int -> ByteBuffer.allocate(4).putInt(this).array()
        is Long -> ByteBuffer.allocate(8).putLong(this).array()
        is Float -> ByteBuffer.allocate(4).putFloat(this).array()
        is Double -> ByteBuffer.allocate(8).putDouble(this).array()
        is UByte -> this.toByte().toBytes()
        is UShort -> this.toShort().toBytes()
        is UInt -> this.toInt().toBytes()
        is ULong -> this.toLong().toBytes()
        is BigInteger -> this.toByteArray()
        is BigDecimal -> this.unscaledValue().toByteArray()
        else -> {
            val bos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(bos)
            oos.writeObject(this)
            oos.flush()
            return bos.toByteArray()
        }
    }
}
