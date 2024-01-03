package com.kuaishou.commercial.utility.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension

class IOCRegisterPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    println("IOCRegisterPlugin apply")
    when {
      target.plugins.hasPlugin("com.android.application") ->
        target.extensions.getByType(AppExtension::class.java)
          .registerTransform(IOCRegisterTransform())
      target.plugins.hasPlugin("com.android.library") ->
        target.extensions.getByType(LibraryExtension::class.java)
          .registerTransform(IOCRegisterLibTransform())
    }
  }
}