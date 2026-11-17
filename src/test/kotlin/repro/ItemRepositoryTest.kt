package repro

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CopyOnWriteArrayList

@SpringBootTest
class ItemRepositoryTest(@Autowired private val itemRepository: ItemRepository) {

    @Test
    fun `cancelling calls to a suspend default method causes no internal coroutines error`() {
        val failures = CopyOnWriteArrayList<Throwable>()
        val scope = CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { _, failure -> failures += failure })

        // Cancels lookups straight away, as WebFlux does when clients abort requests
        runBlocking {
            repeat(1_000) {
                val lookups = List(100) { scope.launch { itemRepository.findByIdOrThrow(1) } }
                lookups.forEach { it.cancel() }
                lookups.joinAll()
            }
        }

        // Fails with CoroutinesInternalError caused by ClassCastException: CompletedContinuation cannot be cast to DispatchedContinuation
        assertEquals(emptyList<Throwable>(), failures)
    }
}
