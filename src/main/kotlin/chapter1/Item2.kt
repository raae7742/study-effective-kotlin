package chapter1

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Item 2. Eliminate critical sections
 * When multiple threads modify a shared state, it can lead to unexpected results.
 */
fun print1000WithThread() {
   var num = 0
   for (i in 1..1000) {
       thread {
           Thread.sleep(10)
           num += 1
       }
   }
    Thread.sleep(5000)
    print(num)  // Very unlikely to be 1000
    // Every time a different number
}

// The default collections do not support their elements being modified when they are iterated over.
fun getConcurrentModificationException() {
    var numbers = mutableListOf<Int>()
    for (i in 1..1000) {
        thread {
            Thread.sleep(1)
            numbers.add(i)
        }
        thread {
            Thread.sleep(1)
            print(numbers.sum())    // sum iterates over the list
            // often ConcurrentModificationException
        }
    }
}

// Synchronization in Kotlin/JVM
fun useSynchronization() {
    val lock = Any()
    var num = 0
    for (i in 1..1000) {
        thread {
            Thread.sleep(10)
            synchronized(lock) {
                num += 1
            }
        }
    }
    Thread.sleep(1000)
    print(num)
}

class Counter {
    private val lock = Any()
    private var num = 0

    fun inc() = synchronized(lock) {
        num += 1
    }

    fun dec() = synchronized(lock) {
        num -= 1
    }

    // Synchronization is not necessary, however,
    // without it, getter might serve stale value
    fun get(): Int = num
}

// Atomic objects
fun useAtomicObjects() {
    val num = AtomicInteger(0)
    for (i in 1..1000) {
        thread {
            Thread.sleep(10)
            num.incrementAndGet()
        }
    }
    Thread.sleep(5000)
    print(num.get())
}

// Concurrent collections
fun useConcurrentCollection() {
    val map = ConcurrentHashMap<Int, String>()
    for (i in 1..1000) {
        thread {
            Thread.sleep(1)
            map.put(i, "E$i")
        }
        thread {
            Thread.sleep(1)
            print(map.toList().sumOf { it.first })
        }
    }
}

fun makeConCurrentSet() {
    val set = ConcurrentHashMap.newKeySet<Int>()
    for (i in 1..1000) {
        thread {
            Thread.sleep(1)
            set += i
        }
    }
    Thread.sleep(5000)
    println(set.size)
}

// Do not leak mutation points
data class User2(val name: String)

class UserRepository {
    private val users: MutableList<User2> = mutableListOf()

    //fun loadAll(): MutableList<User2> = users
    fun loadAll(): List<User2> = users

    //...
}

fun useLoadAllToModify() {
    val userRepository = UserRepository()
    val users = userRepository.loadAll()
    //users.add(User2("Kirill"))

    print(userRepository.loadAll()) // [User(name=Kirill)]
}

class UserRepositorySafer {
    private val users: MutableList<User2> = mutableListOf()

    fun loadAll(): List<User2> = users

    fun add(user: User2) {
        users += user
    }
}

class UserRepositoryTest {
    fun `should add elements`() {
        val repo = UserRepositorySafer()
        val oldElements = repo.loadAll()

        repo.add(User2("B"))

        val newElements = repo.loadAll()
        assert(oldElements != newElements)
        // This assertion will fail, because both references
        // point to the same object, and they are equal
    }
}

fun modifyMutableCollectionWhenIterating() {
    val repo = UserRepositorySafer()
    thread {
        for (i in 1..10000) repo.add(User2("User$i"))
    }
    thread {
        for (i in 1..10000) {
            val list = repo.loadAll()
            for (e in list) {
                /* no-op */
            }
        }
    }
    // ConcurrentModificationException
}

/**
 * Defensive copying
 * - Synchronization is needed when supporting multithreaded access to our object.
 * - Collections can be copied with transformation functions like `toList`.
 */
class UserRepositoryWithCopy {
    private val users: MutableList<User2> = mutableListOf()
    private val LOCK = Any()

    fun loadAll(): List<User2> = synchronized(LOCK) {
        users.toList()
    }

    fun add(user: User2) = synchronized(LOCK) {
        users += user
    }
}

/**
 * Simpler option: use a read-only list
 * - only need to synchronize the operations that modify our list.
 * - makes adding elements slower, but accessing the list is faster.
 * => good for more reads than writes.
 */
class UserRepositoryWithReadOnlyList {
    private var users: List<User2> = listOf()
    private val LOCK = Any()

    fun loadAll(): List<User2> = users
    fun add(user: User2) = synchronized(LOCK) {
        users = users + user
    }
}

/**
 * [Summary]
 * - Multiple threads modifying the same state can lead to conflicts.
 * - To protect a state from concurrent modification, we can use synchronization such as `synchronized` block.
 * - There are also default or external libraries that provide atomic objects.
 * - Classes should protect their internal state and not expose it.
 * - To protect a state from concurrent modification, we can also operate on read-only objects or use defensive copying.
 */