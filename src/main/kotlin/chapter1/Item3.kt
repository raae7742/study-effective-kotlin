package kotlin.chapter1

/**
 * Item 3. Eliminate platform types as soon as possible
 */
fun statedType() {
    val value: String = JavaClassForItem3().value // NPE
    //...
    println(value.length)
}

fun platformType() {
    val value = JavaClassForItem3().value
    //...
    println(value.length) // NPE
}

interface UserRepo {
    fun getUserName() = JavaClassForItem3().value
}

class RepoImpl : UserRepo {
    override fun getUserName(): String? {
        return null
    }
}

fun main() {
    val repo: UserRepo = RepoImpl()
    val text: String = repo.getUserName() // NPE in runtime
    print("User name length is ${text.length}")
}