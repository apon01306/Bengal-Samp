package com.bengalsamp.launcher

/**
 * সব সেটিংস এক জায়গায় — Shahriar, এই ফাইলটাই তোমাকে বদলাতে হবে।
 *
 * ALL SETTINGS IN ONE PLACE — Shahriar, this is the file you must edit.
 */
object Config {

    // ------------------------------------------------------------------
    // গেম ফাইলের ডাউনলোড লিংক — GAME FILES DOWNLOAD LINK
    // ------------------------------------------------------------------
    // ⚠️ এটা এখন শুধু একটা নমুনা (placeholder) লিংক। আসল লিংক বসাও:
    // 1. GitHub-এ তোমার repo-তে গিয়ে Releases → "Draft a new release"
    // 2. তোমার গেম ফাইলের zip-টা আপলোড করো (নিচের সতর্কতা পড়ো!)
    // 3. Release publish করার পর zip ফাইলের লিংকটা কপি করো।
    //    লিংকটা এমন দেখাবে:
    //    https://github.com/TOMAR-USERNAME/TOMAR-REPO/releases/download/v1.0/game-files.zip
    // 4. নিচের লিংকটার জায়গায় ওই আসল লিংকটা বসিয়ে দাও।
    //
    // ⚠️ WARNING — Rockstar-এর GTA San Andreas গেমের আসল ফাইল
    // (gta3.img, .obb, .apk ইত্যাদি) GitHub-এ আপলোড করো না!
    // GitHub সেগুলো মুছে দেয়, আর একটা ফাইল 100 MB-এর বেশি হলে
    // আপলোডই হবে না। শুধু তোমার নিজের সার্ভারের ফাইল / মোডপ্যাক
    // (যেগুলোর কপিরাইট তোমার বা যেগুলো ফ্রি) আপলোড করো।
    // বিস্তারিত README.md-তে আছে।
    const val GAME_FILES_URL =
        "https://github.com/USERNAME/REPO/releases/download/v1.0/game-files.zip"

    // ডাউনলোড করা ফাইলটা ফোনে এই নামে জমা হবে।
    const val GAME_FILE_NAME = "game-files.zip"

    // ------------------------------------------------------------------
    // সার্ভার — SERVER (নমুনা / SAMPLE — আসল IP এখনো বসানো হয়নি)
    // ------------------------------------------------------------------
    // Shahriar এখনো আসল সার্ভারের IP দেয়নি, তাই এটা নমুনা।
    // আসল IP পেলেই এখানে বসিয়ে দাও, যেমন: "103.45.67.89:7777"
    const val DEFAULT_SERVER_NAME = "HighPixel SAMP (নমুনা)"
    const val DEFAULT_SERVER_IP = "0.0.0.0:7777"
    const val DEFAULT_SERVER_PLAYERS = "0/100"
    const val DEFAULT_SERVER_MODE = "Roleplay"

    // ------------------------------------------------------------------
    // PLAY — সরাসরি সার্ভারে ঢোকা (direct join)
    // ------------------------------------------------------------------
    // PLAY চাপলে লঞ্চার প্রথমে নিচের লিংকগুলো চেষ্টা করে — সার্ভারের
    // IP:Port সহ, যাতে গেম অ্যাপ সরাসরি ওই সার্ভারে ঢুকে যায়, আর
    // মাঝখানে ব্যবহারকারীকে কিছু করতে না হয়।
    //
    // যাচাইয়ের অবস্থা (2026-11-13): ReSA-এর কোনো official deep-link /
    // join-link (যেমন resa://) প্রকাশ্যে পাওয়া যায়নি — resa-mp.com-এও
    // এমন কোনো লিংক নেই। তাই এখানে SA-MP-এর standard join লিংক
    // "samp://IP:port" ব্যবহার করা হয়েছে — PC-র SA-MP ক্লায়েন্ট আর
    // সার্ভার তালিকাগুলো এই ফরম্যাটেই সার্ভারে ঢোকায়। ReSA এই লিংক
    // ধরে কি না, সেটা যাচাই করা যায়নি — আসল ফোনে পরীক্ষা করতে হবে।
    // লিংক না ধরলে লঞ্চার ইনস্টল করা ক্লায়েন্ট অ্যাপটা সার্ভার+নাম
    // (extras-এ) সহ খুলে দেবে; ক্লায়েন্ট সেটা না পড়লে ব্যবহারকারীকে
    // একবার সার্ভার বেছে নিতে হবে।
    //
    // {IP} = সার্ভারের IP:Port, {NICKNAME} = ব্যবহারকারীর নাম।
    val PLAY_LINK_TEMPLATES = listOf(
        "samp://{IP}"
    )

    // ------------------------------------------------------------------
    // ReSA / SAMP ক্লায়েন্ট — GAME CLIENT APPS
    // ------------------------------------------------------------------
    // PLAY চাপলে লঞ্চার নিচের অ্যাপগুলো ফোনে খুঁজবে — যেটা পাবে
    // সেটা খুলে দেবে।
    //
    // ReSA Multiplayer-এর Android package name আমি নির্ভরযোগ্যভাবে
    // খুঁজে পাইনি (resa-mp.com-এ এটা প্রকাশ করা নেই)। তাই এখানে
    // শুধু যাচাই করা (verified) package-টাই আছে:
    //   ro.alynsampmobile.launcher — Alyn SA-MP Mobile (Google Play-এ
    //   যাচাই করা, ReSA-এর আগের ভার্সন, একই ডেভেলপার Alyn-এর বানানো)
    //
    // ReSA-এর package name জানতে: ফোনে ReSA ইনস্টল করে
    // Settings → Apps → ReSA → App info-তে দেখো, অথবা কম্পিউটারে
    //   adb shell pm list packages | grep -i resa
    // চালিয়ে দেখো। পেলে নিচের লিস্টের প্রথমে যোগ করে দাও, যেমন:
    //   "com.resa.mp"  ← এটা শুধু উদাহরণ, যাচাই না করে বসিও না।
    val GAME_CLIENT_PACKAGES = listOf(
        "ro.alynsampmobile.launcher" // Alyn SA-MP Mobile — VERIFIED (Google Play)
    )

    // ReSA না থাকলে এই ওয়েবসাইট খুলে দেওয়া হবে।
    const val RESA_WEBSITE_URL = "https://resa-mp.com/"

    // HighPixel Discord invite.
    const val DISCORD_URL = "https://discord.gg/DfdvbxNSKs"
}
