package com.fedup.user

import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*

@SpringBootApplication
class UserService(private val userRepository: UserRepository) {
    fun register(user: User) {
        userRepository.save(user)
    }
}

fun main(args: Array<String>) {
    runApplication<UserService>(*args)
}
