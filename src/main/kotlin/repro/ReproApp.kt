package repro

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@SpringBootApplication
class ReproApp

@Table
data class Item(@Id val id: Long)

interface ItemRepository : CoroutineCrudRepository<Item, Long> {

    suspend fun findByIdOrThrow(id: Long): Item = findById(id) ?: throw NoSuchElementException("No item found by ID '$id'")
}
