package chapter1

import java.util.SortedSet
import java.util.TreeSet
import kotlin.concurrent.thread
import kotlin.jvm.Throws
import kotlin.properties.Delegates.observable

/**
 * Item 1: Limit mutability
 * limiting mutability features in Kotlin
 * 1. Read-only properties `val`
 * 2. Separation between mutable and read-only collections
 * 3. `copy` in data classes
 */
private fun main() {
    val account = BankAccount()
    println(account.balance)    // 0.0
    account.deposit(100.0)
    println(account.balance)    // 100.0
    account.withdraw(50.0)
    println(account.balance)    // 50.0
}
class BankAccount {
    var balance = 0.0
        private set

    fun deposit(depositAmount: Double) {
        balance += depositAmount
    }

    @Throws(InsufficientFunds::class)
    fun withdraw(withdrawAmount: Double) {
        if (balance < withdrawAmount) {
            throw InsufficientFunds()
        }
        balance -= withdrawAmount
    }
}

class InsufficientFunds : Exception()

// 1. Read-only properties `val`
// `val` returned different value because we custom it's getter.
var name: String = "Marcin"
var surname: String = "Moskata"
val fullName
    get() = "$name $surname"

private fun main1() {
    println(fullName)   // Marcin Maskata
    name = "Maja"
    println(fullName)   // Maja Moskata
}

// 'val' is only a getter, but 'var' is both a getter and setter.
private fun calculate(): Int {
    println("Calculating... ")
    return 42
}

val fizz = calculate()  // Calculating...
val buzz
    get() = calculate()

private fun main2() {
    print(fizz) // 42
    print(fizz) // 42
    print(buzz) // Calculating... 42
    print(buzz) // Calculating... 42
}

interface Element {
    val active: Boolean
}

class ActualElement : Element {
    override var active: Boolean = false
}

// Final 'val' can be smart-casted.
val name2: String? = "Marton"
val surname2: String = "Braun"

val fullName2: String?
    get() = name?.let { "$it $surname" }
val fullName3: String? = name?.let { "$it $surname" }

private fun main3() {
    if (fullName2 != null) {
        //println(fullName2.length)   // ERROR
    }

    if (fullName3 != null) {
        println(fullName3.length)   // 12
    }
}

// 2. Separation between mutable and read-only collections
// Down-casting read-only collections to mutable ones should never happen in Kotlin.
private fun main4() {
    val list = listOf(1, 2, 3)
    val mutableList = list.toMutableList()  // It's okay. (creating a copy of the list)
    mutableList.add(4)
}

// 3. Copy in data classes
data class FullName(
    var firstName: String,
    var secondName: String
)

// Because the person is at an incorrect position, it couldn't be found.
private fun main5() {
    val names: SortedSet<FullName> = TreeSet()
    val person = FullName("AAA", "AAA")

    names.add(person)
    names.add(FullName("Jordan", "Hansen"))
    names.add(FullName("David", "Blanc"))

    print(names)    // [AAA AAA, David Blanc, Jordan Hansen]
    print(person in names)  // true

    person.firstName = "ZZZ"
    print(names)    // [ZZZ AAA, David Blanc, Jordan Hansen]
    print(person in names)  // false
}

// `withSurname` method produces a copy with a surname property changed.
class User(
    val name: String,
    val surname: String
) {
    fun withSurname(surname: String) = User(name, surname)
}

private fun main6() {
    var user = User("Maja", "Markiewicz")
    user = user.withSurname("Moskata")
    print(user) // User(name=Maja, surname=Moskata)
}

// it is better to use `data` modifier to copy.
data class UserData(
    val name: String,
    val surname: String
)

private fun main7() {
    var user = UserData("Maja", "Markiewicz")
    user = user.copy(surname = "Moskata")
    print(user) // User(name=Maja, surname=Moskata)
}

// + Different kinds of mutation points
private fun main8() {
    val list1: MutableList<Int> = mutableListOf()
    var list2: List<Int> = listOf()

    list1.add(1)
    list2 = list2 + 1

    list1 += 1  // Translates to list1.plusAssign(1)
    list2 += 1  // Translates to list2 = list2.plus(1)

    for (i in 1..1000) {
        thread {
            list2 = list2 + i
        }
    }
    Thread.sleep(1000)
    print(list2.size)   // Very unlikely to be 1000,
    // every time a different number, like for instance 911

    var names by observable(listOf<String>()) { _, old, new ->
        println("Names changed from $old to $new")
    }

    names += "Fabio"
    // Names changed from [] to [Fabio]
    names += "Bill"
    // Names changed from [Fabio] to [Fabio, Bill]

    // Read-only collections in mutable properties
    var announcements = listOf<Announcement>()
        //private set

    // Don't do that. It has both a mutating property and a mutable collection.
    var list3 = mutableListOf<Int>()
}

data class Announcement(
    val content: String = ""
)

/** [Summary]
 *  - Prefer `val` over `var`.
 *  - Prefer an immutable property over a mutable one.
 *  - Prefer objects and classes that are immutable over mutable ones.
 *  - If you need immutable objects to change, consider making them data classes and using `copy`.
 *  - When you hold a state, prefer read-only over mutable collections.
 *  - Design your mutation points wisely and do not produce unnecessary ones.
 */