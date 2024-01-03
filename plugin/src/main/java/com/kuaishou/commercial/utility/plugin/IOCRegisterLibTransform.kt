package com.kuaishou.commercial.utility.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager

class IOCRegisterLibTransform: IOCRegisterTransform() {
  override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.PROJECT_ONLY
}