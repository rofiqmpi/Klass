package com.mpi.klass

data class UserSession(
    val role: String,
    val email: String,
    val name: String,
    val photoUrl: String? = null,
)

expect fun saveUserSession(session: UserSession)
expect fun loadUserSession(): UserSession?
expect fun clearUserSession()
