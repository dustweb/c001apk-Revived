package com.example.c001apk.compose.logic.repository

import androidx.datastore.core.DataStore
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AccountRepositoryTest {
    private class MemoryStore(initial: UserPreferencesCompat = UserPreferencesCompat.default()) : DataStore<UserPreferencesCompat> {
        override val data = MutableStateFlow(initial)
        override suspend fun updateData(transform: suspend (UserPreferencesCompat) -> UserPreferencesCompat): UserPreferencesCompat =
            transform(data.value).also { data.value = it }
    }
    private val cipher = object : AccountTokenCipher() {
        override fun encrypt(token: String) = "protected:${token.reversed()}"
        override fun decrypt(value: String): String {
            check(value.startsWith("protected:"))
            return value.removePrefix("protected:").reversed()
        }
    }

    @Test fun switchingRestoresTheWholeProfileAndKeepsDevicePreferences() = runBlocking {
        val initial = UserPreferencesCompat.default().copy(szlmId = "device", xAppDevice = "encoded", fontScale = 1.2f)
        val store = MemoryStore(initial)
        val repo = AccountRepository(store, cipher)
        repo.signIn("1", "Alice", "token-a")
        repo.updateProfile(store.data.value.sessionGeneration, "1", "Alice", "avatar-a", "8", "10", "20")
        repo.signIn("2", "Bob", "token-b")
        repo.switchTo("1")
        val current = store.data.value
        assertEquals("Alice", current.username)
        assertEquals("token-a", current.token)
        assertEquals("avatar-a", current.userAvatar)
        assertEquals("8", current.level)
        assertEquals("device", current.szlmId)
        assertEquals("encoded", current.xAppDevice)
        assertEquals(1.2f, current.fontScale)
        assertEquals(setOf("1", "2"), current.accounts.map { it.uid }.toSet())
        assertTrue(current.accounts.none { it.encryptedToken == "token-a" || it.encryptedToken == "token-b" })
        assertEquals(current, UserPreferencesCompat(current.toProto()))
    }

    @Test fun staleResponsesCannotOverwriteOrLogOutTheNewAccount() = runBlocking {
        val store = MemoryStore()
        val repo = AccountRepository(store, cipher)
        repo.signIn("1", "Alice", "token-a")
        val oldGeneration = store.data.value.sessionGeneration
        repo.signIn("2", "Bob", "token-b")
        val before = store.data.value
        repo.updateProfile(oldGeneration, "1", "Old reply", "old-avatar")
        repo.expire(oldGeneration)
        assertEquals(before, store.data.value)
    }

    @Test fun legacyLoginIsArchivedAndRemovingAnotherAccountKeepsTheSession() = runBlocking {
        val store = MemoryStore(UserPreferencesCompat.default().copy(uid = "1", username = "Alice", token = "legacy", isLogin = true))
        val repo = AccountRepository(store, cipher)
        repo.signIn("2", "Bob", "token-b")
        repo.switchTo("1")
        assertEquals("legacy", store.data.value.token)
        val generation = store.data.value.sessionGeneration
        repo.remove("2")
        assertEquals(generation, store.data.value.sessionGeneration)
        assertTrue(store.data.value.isLogin)
        repo.remove("1")
        assertFalse(store.data.value.isLogin)
        assertTrue(store.data.value.token.isEmpty())
        assertTrue(store.data.value.accounts.isEmpty())
    }

    @Test fun unreadableSavedCredentialsLeaveCurrentSessionUntouched() = runBlocking {
        val store = MemoryStore()
        val repo = AccountRepository(store, cipher)
        repo.signIn("1", "Alice", "token-a")
        repo.expire(store.data.value.sessionGeneration)
        repo.signIn("2", "Bob", "token-b")
        val before = store.data.value
        assertTrue(runCatching { repo.switchTo("1") }.isFailure)
        assertEquals(before, store.data.value)
    }
}
