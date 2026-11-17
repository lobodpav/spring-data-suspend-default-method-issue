# Cancelling a call to a `suspend` default repository method fails with `CoroutinesInternalError`

Run `./gradlew test`: [`ItemRepositoryTest`](src/test/kotlin/repro/ItemRepositoryTest.kt) fails within a few seconds.
The same test calling `findById` directly instead of the default method [`findByIdOrThrow`](src/main/kotlin/repro/ReproApp.kt) passes.

---

### Versions

Spring Boot `4.1.2` (Spring Data Commons `4.1.2`, Spring Framework `7.0.10`), kotlinx.coroutines `1.10.2`, Kotlin `2.4.10`.

### Describe the bug

A repository has a Kotlin `suspend` default method:

```kotlin
interface ItemRepository : CoroutineCrudRepository<Item, Long> {

    suspend fun findByIdOrThrow(id: Long): Item = findById(id) ?: throw NoSuchElementException("No item '$id'")
}
```

When a coroutine calling it gets cancelled, kotlinx.coroutines occasionally reports an internal error:

```
kotlinx.coroutines.CoroutinesInternalError: Fatal exception in coroutines machinery for CancellableContinuation(DispatchedContinuation[Dispatchers.Default, Continuation at repro.ItemRepository.findByIdOrThrow$suspendImpl(ReproApp.kt:16)]){Completed}. Please read KDoc to 'handleFatalException' method and report this incident to maintainers
Caused by: java.lang.ClassCastException: class kotlin.coroutines.jvm.internal.CompletedContinuation cannot be cast to class kotlinx.coroutines.internal.DispatchedContinuation
	at kotlinx.coroutines.CoroutineDispatcher.releaseInterceptedContinuation(CoroutineDispatcher.kt:248)
	at kotlin.coroutines.jvm.internal.ContinuationImpl.releaseIntercepted(ContinuationImpl.kt:119)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:40)
```

In production this hits WebFlux `suspend` controllers calling such methods when clients abort requests.
The test does the same: it launches lookups and cancels them straight away.

Calling `findById` directly, or declaring `findByIdOrThrow` as an extension function on the repository instead, does not fail.
