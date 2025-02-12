/*
 * MIT License
 *
 * Copyright (c) 2024 BreninSul
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.breninsul.synchronizationstarter.service

import java.util.concurrent.Callable

/**
 * Executes a provided task in synchronization with a specified id.
 *
 * @param T A type parameter that extends SynchronizationService
 * @param R The result type of the provided task
 * @param id An identifier used for synchronization
 * @param task A piece of code that is meant to be executed with synchronization
 * @return Returns the result of the task execution
 *
 * @throws Exception if any thrown by the task
 */
fun <T : SynchronizationService, R> T.sync(
    id: Any,
    task: Callable<R>,
): R {
    return sync(id,true,task)
}

/**
 * Executes a provided task with synchronization, ensuring thread-safe operations around the specified identifier.
 * If `ignoreLockInTheSameThread` is true and the calling thread already holds the lock, the task is executed directly.
 *
 * @param T A type parameter that extends SynchronizationService
 * @param R The result type of the provided task
 * @param id An identifier used for synchronization
 * @param ignoreLockInTheSameThread Indicates whether to skip synchronization if the calling thread is already locked, default is true
 * @param task A callable task to be executed within the synchronization mechanics
 * @return The result of the task execution
 *
 * @throws Exception if any exception is thrown by the callable task
 */
fun <T : SynchronizationService, R> T.sync(
    id: Any,
    ignoreLockInTheSameThread:Boolean =true,
    task: Callable<R>,
): R {
    if (ignoreLockInTheSameThread&& threadIsAlreadyLocked.get()){
        return task.call()
    }
    before(id)
    if (ignoreLockInTheSameThread){
        threadIsAlreadyLocked.set(true)
    }
    try {
        return task.call()
    } finally {
        try {
            after(id)
        }finally {
            if (ignoreLockInTheSameThread) {
                threadIsAlreadyLocked.set(false)
            }
        }
    }
}
private val threadIsAlreadyLocked = ThreadLocal.withInitial{false}



