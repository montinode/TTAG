package com.reown.sample.dapp.util

/**
 * Converts a UTF-8 string to its hex-encoded byte representation with an "0x" prefix,
 * as expected by EVM wallets for methods like `personal_sign`.
 *
 * Example: `"hello"` → `"0x68656c6c6f"`
 */
fun String.encodeToHex(): String =
    toByteArray(Charsets.UTF_8)
        .joinToString("") { "%02x".format(it) }
        .let { "0x$it" }
