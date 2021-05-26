package com.onreg.sdk

private const val KOTLIN_VERSION = "1.4.32"
private const val COROUTINES_VERSION = "1.4.3"
private const val LIFECYCLE_VIEW_MODEL_ERSION = "2.3.0"

private const val JUNIT_VERSION = "5.7.1"
private const val TURBINE_VERSION = "0.4.1"
private const val COROUTINES_TEST_VERSION = "1.4.3"


object Dependencies {
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$LIFECYCLE_VIEW_MODEL_ERSION"

    /*testing*/
    const val junit = "junit:junit:$JUNIT_VERSION"
    const val turbine = "app.cash.turbine:turbine:$TURBINE_VERSION"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$COROUTINES_TEST_VERSION"
}