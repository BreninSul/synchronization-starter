This project provides several synchronisation services to sync by any java/kotlin object (with equals/hashcode implementation).
Synchronisation can be processed local,using PostgresSQL and Zookeeper.
Beans in Spring Boot will be automatically registered in SynchronisationAutoconfiguration with defined properties SynchronisationProperties (prefix synchronisation).

````kotlin

repositories {
//Other repositories
    maven {
        name = "GitHub"
        url = uri("https://maven.pkg.github.com/BreninSul/synchronization-starter")
    }
//Other repositories
}
````

 Next, add the following dependency:

````kotlin
dependencies {
//Other dependencies
    implementation("com.github.breninsul:synchronization-starter:1.0.0")
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