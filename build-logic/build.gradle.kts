plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
    implementation("com.android.tools.build:gradle:9.2.1")
}

gradlePlugin {
    plugins {
        create("nativeConventions") {
            id = "kmplitert.native-conventions"
            implementationClass = "io.github.kmplitert.NativeConventionsPlugin"
        }
    }
}
