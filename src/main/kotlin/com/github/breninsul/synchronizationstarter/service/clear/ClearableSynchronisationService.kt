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

package com.github.breninsul.synchronizationstarter.service.clear

import com.github.breninsul.synchronizationstarter.dto.ClientLock
import com.github.breninsul.synchronizationstarter.service.SynchronizationService
import java.time.Duration

/**
 * The ClearableSynchronisationService interface extends the SynchronizationService interface.
 * It provides methods to manage and retrieve locks used in synchronized blocks.
 */
interface ClearableSynchronisationService<T : ClientLock> : SynchronizationService {
    /**
     *  This method returns all the elements that have exceeded their specified lifetime.
     *
     *  @param lifetime A Duration object that specifies the lifetime limit for objects
     *  @return A List of pairs. Each pair includes an object and the associated ClientLock that have exceeded their lifetime
     */
    fun getAllLocksAfter(lifetime: Duration): List<Pair<Any, T>>

    /**
     *  This method clears the provided lock object.
     *
     *  @param id The id of the lock to be cleared.
     */
    fun clear(id: Any)

    /**
     *  This method times out and unlocks the provided lock using its id.
     *
     *  @param id The id of the lock to be timed out and unlocked.
     *  @param lifetime The duration before the lock becomes timed out.
     */
    fun unlockTimeOuted(
        id: Any,
        lifetime: Duration,
    )

    fun getLock(id:Any):T?
}
