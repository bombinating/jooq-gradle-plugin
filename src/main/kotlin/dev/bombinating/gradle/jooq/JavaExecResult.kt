package dev.bombinating.gradle.jooq

import org.apache.commons.lang3.exception.ExceptionUtils
import org.gradle.process.ExecResult

data class JavaExecResult(val result: ExecResult? = null, val exception: Throwable? = null) {

    val isSuccess: Boolean
        get() = exception == null && result?.exitValue == 0

    val errorMsg: String?
        get() = exception?.let { ExceptionUtils.getRootCauseMessage(it) }

}