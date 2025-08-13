// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties

val apikeyPropertiesFile = rootProject.file("apikey.properties")
val apikeyProperties = Properties()
if (apikeyPropertiesFile.exists()) {
    apikeyProperties.load(apikeyPropertiesFile.inputStream())
}

extra["OPENWEATHER_API_KEY"] = apikeyProperties.getProperty("OPENWEATHER_API_KEY", "")

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}