# don't minimize my own classes for better debugging
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontobfuscate
-keep class de.marmaro.krt.ffupdater.** {*; }
-keepattributes SourceFile,LineNumberTable

# R8 (AGP 9) strips the no-arg constructor of the Room databases that are created by reflection
# -> "NoSuchMethodException: androidx.work.impl.WorkDatabase_Impl.<init> []" at startup
-keep class * extends androidx.room.RoomDatabase { <init>(...); }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(...); }
-keep class androidx.work.impl.** { *; }
-keep class androidx.startup.** { *; }
