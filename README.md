# Bengal Samp — SAMP লঞ্চার APK

**Bengal Samp** — Android ফোনের জন্য SAMP সার্ভার লঞ্চার অ্যাপ।
ইউজার APK ডাউনলোড করে, **UPDATE** চাপলে গেম ফাইল ডাউনলোড হবে,
তারপর **PLAY** চাপলে ReSA Multiplayer অ্যাপ খুলে সার্ভারে ঢুকে খেলা যাবে।

**Bengal Samp** is an Android launcher app for a SAMP server.
The user installs the APK, taps **UPDATE** to download the game files,
then taps **PLAY** — the ReSA Multiplayer app opens and joins the server.

> ⚠️ এই অ্যাপটা শুধু **লঞ্চার** — এটা নিজে গেম না। গেম খেলতে ফোনে
> **ReSA Multiplayer** অ্যাপ লাগবে (https://resa-mp.com — ফ্রি, Android 7.0+)।
> This app is only a **launcher**, not the game itself. Playing needs the
> **ReSA Multiplayer** app installed on the phone.

---

## কীভাবে কাজ করে / How it works

1. **নাম লেখো** — গেমে যে নাম দেখাবে।
2. **UPDATE চাপো** — `Config.kt`-এ বসানো লিংক থেকে গেম ফাইলের zip
   ডাউনলোড হবে। প্রোগ্রেস বার, %, আর MB দেখাবে। ডাউনলোড শেষ হলে
   PLAY বাটন চালু হবে।
3. **PLAY চাপো** — অ্যাপ সরাসরি বেছে নেওয়া সার্ভারে ঢুকিয়ে দেবে।
   প্রথমে SA-MP-এর standard join লিংক (`samp://IP:port`) চেষ্টা করে,
   তারপর ইনস্টল করা গেম অ্যাপটা সার্ভার+নাম সহ খুলে দেয়। ব্যবহারকারীকে
   মাঝখানে আর কিছু করতে হয় না — Bengal Samp-ই গেম মনে হবে।

> ⚠️ সত্যি কথাটা: ReSA-এর কোনো official deep-link / join-link
> প্রকাশ্যে পাওয়া যায়নি (resa-mp.com-এও নেই), তাই সরাসরি ঢোকা
> `samp://` লিংকের ওপর নির্ভর করে — ReSA এই লিংক ধরে কি না, আসল
> ফোনে পরীক্ষা না করে বলা যাবে না। লিংক না ধরলে অ্যাপ গেম অ্যাপটা
> খুলে দেবে, তখন একবার সার্ভার বেছে নিতে হবে (ReSA-তে সার্ভার
> favourite করলে পরেরবার এক চাপেই ঢোকা যায়)। গেম অ্যাপ ফোনে না
> থাকলে PLAY একটাই বাটন দেখাবে — **ডাউনলোড করো** — চাপলেই
> resa-mp.com খুলবে, ডাউনলোড করে ফিরে এসে আবার PLAY চাপো।

আরও আছে: সার্ভার তালিকা, নিজের সার্ভার (IP:Port) যোগ করা,
সার্ভারের IP কপি করার বাটন, আর HighPixel Discord লিংক
https://discord.gg/DfdvbxNSKs।

## ১. GitHub-এ প্রজেক্টটা তোলা / Put the project on GitHub

1. GitHub-এ গিয়ে **New repository** বানাও — নাম যেমন `bengal-samp-launcher`।
2. এই ফোল্ডারের সব ফাইল ওই repo-তে আপলোড করো (GitHub ওয়েবসাইটে
   "uploading an existing file" দিয়ে, অথবা কম্পিউটারে git দিয়ে):
   ```bash
   git init
   git add .
   git commit -m "Bengal Samp launcher"
   git branch -M main
   git remote add origin https://github.com/TOMAR-USERNAME/bengal-samp-launcher.git
   git push -u origin main
   ```
3. Push করলেই GitHub Actions APK বানানো শুরু করবে (নিচে দেখো)।

## ২. গেম ফাইলের লিংক বসানো / Set the game-files URL

`app/src/main/java/com/bengalsamp/launcher/Config.kt` ফাইল খুলে
`GAME_FILES_URL`-এর জায়গায় তোমার আসল লিংক বসাও:

```kotlin
const val GAME_FILES_URL =
    "https://github.com/USERNAME/REPO/releases/download/v1.0/game-files.zip"
```

লিংকটা পাবে এভাবে:
1. তোমার repo-তে **Releases → Draft a new release**-এ যাও।
2. তোমার গেম ফাইলের zip আপলোড করো, tag দাও যেমন `v1.0`, Release publish করো।
3. zip ফাইলের লিংক কপি করে `GAME_FILES_URL`-এ বসাও।

একই ফাইলে সার্ভারের আসল IP-ও বসাতে পারো (`DEFAULT_SERVER_IP`,
এখন নমুনা `0.0.0.0:7777` দেওয়া আছে)।

## ৩. APK পাওয়া / Get the APK

কোড বদলে `main` branch-এ push করলেই GitHub Actions
(`.github/workflows/build-apk.yml`) APK বানাবে:
**JDK 17 + Android SDK + Gradle 8.2 → `assembleDebug`**।

APK ডাউনলোড: repo-তে **Actions** ট্যাব → সবশেষ run → **Artifacts**
→ **Bengal-Samp-APK** → ভেতরে `app-debug.apk` পাবে।

কম্পিউটারে Android Studio থাকলে প্রজেক্ট খুলে **Build → Build APK**
দিয়েও APK বানাতে পারো। (এই প্রজেক্টে `gradlew` স্ক্রিপ্ট দেওয়া নেই —
Android Studio প্রথমবার খুললেই wrapper বানিয়ে নেয়, আর GitHub
Actions-এ wrapper লাগে না।)

## ⚠️ সতর্কতা — GitHub-এ GTA গেম ফাইল আপলোড করো না / Do NOT upload GTA files

**Rockstar-এর GTA San Andreas-এর আসল গেম ফাইল (gta3.img, .obb,
মূল .apk, গেমের ডেটা) GitHub-এ আপলোড করো না!**
- ওগুলো কপিরাইট করা ফাইল — GitHub DMCA-তে সেগুলো **মুছে দেয়**,
  আর তোমার repo/account বন্ধও হয়ে যেতে পারে।
- GitHub-এ একটা ফাইলের সীমা **100 MB** — বড় গেম ফাইল আপলোডই হবে না।
- শুধু **তোমার নিজের** সার্ভারের ফাইল / মোডপ্যাক (যেগুলোর কপিরাইট
  তোমার, বা যেগুলো ফ্রি/ওপেন) আপলোড করো।

গেমের মূল ফাইল ReSA অ্যাপ নিজেই প্রথমবার চালু হলে ডাউনলোড করে নেয় —
এই লঞ্চারের কাজ শুধু তোমার সার্ভারের ফাইল দেওয়া আর ReSA খুলে দেওয়া।

## খুঁটিনাটি / Notes

- **Package:** `com.bengalsamp.launcher` • **App name:** Bengal Samp
- **minSdk 24 (Android 7.0), targetSdk 34**, Kotlin, AndroidX only
  (appcompat, material, constraintlayout) — DownloadManager দিয়ে
  ডাউনলোড হয়, কোনো বাড়তি permission লাগে না।
- **ReSA package name:** ReSA-এর Android package name প্রকাশ্যে
  নির্ভরযোগ্যভাবে পাওয়া যায়নি (resa-mp.com-এ নেই), তাই PLAY বাটন
  যাচাই করা Alyn SA-MP Mobile package (`ro.alynsampmobile.launcher`,
  Google Play-এ যাচাই করা) খোঁজে। ReSA-এর package name জানলে
  `Config.kt`-এ `GAME_CLIENT_PACKAGES` লিস্টের প্রথমে যোগ করে দাও।
  জানার উপায়: ফোনে ReSA ইনস্টল করে Settings → Apps → ReSA দেখো,
  অথবা `adb shell pm list packages | grep -i resa` চালাও।
- ডাউনলোড মাঝপথে বাতিল করলে অসম্পূর্ণ ফাইল মুছে যায়। Resume নেই —
  আবার UPDATE চাপলেই নতুন করে ডাউনলোড হবে।
- APK বাইনারি এই repo-তে নেই — GitHub Actions বানিয়ে দেবে।
  এই VM-এ JDK/Android SDK নেই বলে এখানে APK compile করা হয়নি।
