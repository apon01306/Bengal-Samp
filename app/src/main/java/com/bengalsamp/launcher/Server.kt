package com.bengalsamp.launcher

/**
 * একটা SAMP সার্ভারের তথ্য।
 *
 * @param name সার্ভারের নাম (তালিকায় দেখাবে)
 * @param ip IP:Port, যেমন "103.45.67.89:7777"
 * @param players "45/100"-এর মতো, জানা না থাকলে "—"
 * @param mode গেম মোড, যেমন "Roleplay"
 * @param isSample true হলে এটা নমুনা সার্ভার — আসল IP এখনো বসানো হয়নি
 */
data class Server(
    val name: String,
    val ip: String,
    val players: String,
    val mode: String,
    val isSample: Boolean = false
)
