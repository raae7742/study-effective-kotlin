package kotlin.chapter1

import java.awt.Color

/**
 * Item 4. Minimize the scope of variables
 */
val a = 1
fun fizz() {
    val b = 2
    print(a + b)
}
val buzz = {
    val c = 3
    print(a + c)
}
// Here we can see a, but not b nor c

var users = listOf("Kim", "Jang")
fun badExample1() {
    lateinit var user: String

    for (i in users.indices) {
        user = users[i]
        print("User at $i is $user")
    }
}

fun betterExample1() {
    for (i in users.indices) {
        val user = users[i]
        print("User t $i is $user")
    }
}

fun sameButNicerSyntaxExample() {
    for ((i, user) in users.withIndex()) {
        print("User at $i is $user")
    }
}

//fun badExample2() {
//    val user: User
//    if (hasValue) {
//        user = getValue()
//    } else {
//        user = User()
//    }
//}

//fun betterExample2() {
//    val user: User = if (hasValue) {
//        getValue()
//    } else {
//        User()
//    }
//}

// Bad
fun updateWeatherWithBadMethod(degrees: Int) {
    val description: String
    val color: Color
    if (degrees < 5) {
        description = "cold"
        color = Color.BLUE
    } else if (degrees < 23) {
        description = "mild"
        color = Color.YELLOW
    } else {
        description = "hot"
        color = Color.RED
    }
}

fun updateWeatherWithBetterMethod(degrees: Int) {
    val (description, color) = when {
        degrees < 5 -> "cold" to Color.BLUE
        degrees < 23 -> "mild" to Color.YELLOW
        else -> "hot" to Color.RED
    }
}

// Capturing
fun getPrimes() {
    var numbers = (2..100).toList()
    val primes = mutableListOf<Int>()

    while (numbers.isNotEmpty()) {
        val prime = numbers.first()
        primes.add(prime)
        numbers = numbers.filter { it % prime != 0 }
    }

    print(primes)
}

// the solution which is able to produce a infinite sequence of prime numbers
fun getInfiniteSequenceOfPrimeNumbers() {
    val primes: Sequence<Int> = sequence {
        var numbers = generateSequence(2) { it + 1 }

        while (true) {
            val prime = numbers.first()     // 최적화를 위해 prime 변수를 while 문 밖으로 빼면 잘못된 결과 출력
            yield(prime)
            numbers = numbers.drop(1)
                .filter { it % prime != 0 }
        }
    }

    print(primes.take(10).toList())
}

