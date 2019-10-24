/*
 * Copyright 2019 Andrew Geery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.bombinating.gradle.jooq

import org.apache.commons.lang3.exception.ExceptionUtils
import org.gradle.process.ExecResult

/**
 * Result information about executing a Java process.
 *
 * @property result optional [ExecResult] that will be non-null if the process completed without throwing an exception
 * @property exception optional [Throwable] that will be non-null if the process threw an exception
 * @property isSuccess whether the Java process completed successfully
 * @property errorMsgLog the cause of the Java process not completing successfully
 */
data class JavaExecResult(
    val result: ExecResult? = null,
    val exception: Throwable? = null,
    val errorMsgLog: String? = null
) {

    val isSuccess: Boolean
        get() = exception == null && result?.exitValue == 0

    val errorMsg: String?
        get() = errorMsgLog ?: exception?.let { ExceptionUtils.getRootCauseMessage(it) }

}
