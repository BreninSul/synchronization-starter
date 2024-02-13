This project provides several synchronisation services to sync by any java/kotlin object (with equals/hashcode implementation).
Synchronisation can be processed local,using PostgresSQL and Zookeeper.
Implementation can be set by property synchronisation.mode
````
synchronisation.mode=LOCAL
synchronisation.mode=POSTGRES
synchronisation.mode=ZOOKEEPER
````
For POSTGRES DataSource should be registered as spring bean
For ZOOKEEPER ZooKeeper should be registered as spring bean
Configure with properties

| Parameter                                | Type     | Description                                                                                                                                                     |
|------------------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `synchronisation.enabled`                | boolean  | Enable autoconfig for this starter                                                                                                                              |
| `synchronisation.lock-timeout`           | Duration | Max lifetime for lock. `io.github.breninsul.synchronizationstarter.exception.SyncTimeoutException` will be thrown otherwise                                     |
| `synchronisation.lock-lifetime`          | Duration | Lifetime of lock objects in map. For each synchronisation id, a lock object is created; this parameter specifies after what time interval it should be deleted. |
| `synchronisation.normal-lock-time`       | Duration | Provides normal lock time. If lock took more time, log message will be written                                                                                  |
| `synchronisation.zoo-keeper-path-prefix` | String   | ZooKeeper path prefix                                                                                                                                           |


If set lock-timeout or lock-lifetime to disable timeout/lock clearing

io.github.breninsul.synchronizationstarter.exception.SyncTimeoutException can be thrown if timeout is reached

Beans in Spring Boot will be automatically registered in SynchronisationAutoconfiguration with defined properties SynchronisationProperties (prefix synchronisation).

add the following dependency:

````kotlin
dependencies {
//Other dependencies
    implementation("io.github.breninsul:synchronization-starter:${version}")
//Other dependencies
}

````
# Example of usage

````kotlin
syncService.sync(anyIdYouWant) {
    //your work
}
````

or

````kotlin
syncService.before(anyIdYouWant) {
try {
        //your work
} finally {
syncService.after(anyIdYouWant)
}
}
````