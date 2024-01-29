package com.github.breninsul.synchronizationsatrter.service

import com.github.breninsul.synchronizationsatrter.dto.ClientLock
import java.time.Duration

/**
 * The ClearableSynchronisationService interface extends the SynchronizationService interface.
 * It provides methods to manage and retrieve locks used in synchronized blocks.
 */
interface ClearableSynchronisationService:SynchronizationService {

    /**
     *  This method returns all the elements that have exceeded their specified lifetime.
     *
     *  @param lifetime A Duration object that specifies the lifetime limit for objects
     *  @return A List of pairs. Each pair includes an object and the associated ClientLock that have exceeded their lifetime
     */
    fun getAllLocks(lifetime: Duration):List<Pair<Any,ClientLock>>

    /**
     *  This method clears the provided lock object.
     *
     *  @param id The id of the lock to be cleared.
     */
    fun clear(id:Any)
    /**
     *  This method times out and unlocks the provided lock using its id.
     *
     *  @param id The id of the lock to be timed out and unlocked.
     *  @param lifetime The duration before the lock becomes timed out.
     */
    fun unlockTimeOuted(id:Any,lifetime: Duration)
}