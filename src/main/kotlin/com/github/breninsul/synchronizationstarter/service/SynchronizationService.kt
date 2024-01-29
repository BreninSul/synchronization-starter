package com.github.breninsul.synchronizationsatrter.service

/**
 * A service interface providing methods for synchronizing any process.
 */
interface SynchronizationService {

    /**
     * This method is invoked before the synchronization process starts.
     * @param id An identifier for the process that will be synchronized.
     * @return A Boolean indication of the process status (true if id was locked).
     */
    fun before(id: Any):Boolean

    /**
     * This method is invoked after the synchronization process ends.
     * @param id An identifier for the process that was synchronized.
     */
    fun after(id: Any)
}