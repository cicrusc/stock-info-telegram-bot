plugins {
    kotlin("jvm") version "1.6.10" // Assicurati che la versione corrisponda a quella che stai usando
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation ("com.github.kotlin-telegram-bot.kotlin-telegram-bot:dispatcher:6.1.0")
    implementation ("com.google.code.gson:gson:2.8.6")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
}

    // Aggiungi altre dipendenze qui se necessario


// Se hai bisogno di configurare un task per creare un JAR eseguibile, includi qui il task JAR
