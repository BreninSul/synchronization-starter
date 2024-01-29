package com.github.breninsul.synchronizationsatrter.service

import java.util.concurrent.Callable

 fun <T : SynchronizationService, R> T.sync(id: Any, task: Callable<R>): R {
    before(id)
    try {
        return task.call()
    } finally {
        after(id)
    }
}