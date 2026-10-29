package com.example.c001apk.compose.logic.repository

import androidx.datastore.core.DataStore
import com.example.c001apk.compose.SavedAccount
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.util.CookieUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val store: DataStore<UserPreferencesCompat>,
    private val cipher: AccountTokenCipher,
) {
    val data get() = store.data

    private fun UserPreferencesCompat.archiveCurrent(): UserPreferencesCompat {
        if (!isLogin || uid.isBlank() || token.isBlank()) return this
        val saved = SavedAccount.newBuilder().setUid(uid).setUsername(username)
            .setEncryptedToken(cipher.encrypt(token)).setAvatar(userAvatar).setLevel(level)
            .setExperience(experience).setNextLevelExperience(nextLevelExperience).build()
        return copy(accounts = accounts.filterNot { it.uid == uid } + saved)
    }

    suspend fun rememberCurrent() { store.updateData { it.archiveCurrent() } }

    suspend fun finishLoginInitialization(generation: Long) {
        store.updateData { if (it.pendingDeviceIdLogin == generation) it.copy(pendingDeviceIdLogin = 0) else it }
    }

    suspend fun signIn(uid: String, username: String, token: String) {
        require(uid.isNotBlank() && username.isNotBlank() && token.isNotBlank())
        store.updateData { old ->
            val archived = old.archiveCurrent()
            val saved = archived.accounts.find { it.uid == uid }
            archived.copy(uid = uid, username = username, token = token, isLogin = true,
                userAvatar = saved?.avatar.orEmpty(), level = saved?.level.orEmpty(),
                experience = saved?.experience.orEmpty(), nextLevelExperience = saved?.nextLevelExperience.orEmpty(),
                sessionGeneration = old.sessionGeneration + 1, pendingDeviceIdLogin = old.sessionGeneration + 1).archiveCurrent()
        }
        clearTransientSession()
    }

    suspend fun switchTo(uid: String) {
        var changed = false
        store.updateData { old ->
            if (old.isLogin && old.uid == uid) return@updateData old
            val archived = old.archiveCurrent()
            val saved = archived.accounts.find { it.uid == uid } ?: error("账号已被移除")
            val token = runCatching { cipher.decrypt(saved.encryptedToken) }.getOrNull()
                ?.takeIf { it.isNotBlank() } ?: error("登录信息已失效，请重新添加账号")
            changed = true
            archived.copy(uid = saved.uid, username = saved.username, token = token, isLogin = true,
                userAvatar = saved.avatar, level = saved.level, experience = saved.experience,
                nextLevelExperience = saved.nextLevelExperience, sessionGeneration = old.sessionGeneration + 1, pendingDeviceIdLogin = 0)
        }
        if (changed) clearTransientSession()
    }

    suspend fun remove(uid: String) {
        var changed = false
        store.updateData { old ->
            changed = old.uid == uid
            val remaining = old.copy(accounts = old.accounts.filterNot { it.uid == uid })
            if (old.uid == uid) remaining.loggedOut() else remaining
        }
        if (changed) clearTransientSession()
    }

    suspend fun signOut() {
        store.updateData { it.copy(accounts = it.accounts.filterNot { account -> account.uid == it.uid }).loggedOut() }
        clearTransientSession()
    }

    suspend fun expire(generation: Long) {
        store.updateData { old ->
            if (old.sessionGeneration != generation) old else {
                val expired = SavedAccount.newBuilder().setUid(old.uid).setUsername(old.username).setAvatar(old.userAvatar).build()
                old.copy(accounts = old.accounts.filterNot { it.uid == old.uid } + expired).loggedOut()
            }
        }
    }

    suspend fun updateProfile(generation: Long, uid: String, username: String, avatar: String,
        level: String? = null, experience: String? = null, nextLevel: String? = null, token: String? = null) {
        store.updateData { old ->
            if (!old.isLogin || old.uid != uid || old.sessionGeneration != generation) old else
                old.copy(username = username, userAvatar = avatar, level = level ?: old.level,
                    experience = experience ?: old.experience, nextLevelExperience = nextLevel ?: old.nextLevelExperience,
                    token = token?.takeIf { it.isNotBlank() } ?: old.token).archiveCurrent()
        }
    }

    private fun UserPreferencesCompat.loggedOut() = copy(isLogin = false, uid = "", username = "", token = "",
        userAvatar = "", level = "", experience = "", nextLevelExperience = "", sessionGeneration = sessionGeneration + 1, pendingDeviceIdLogin = 0)

    private fun clearTransientSession() {
        CookieUtil.SESSID = ""
        CookieUtil.atme = null; CookieUtil.atcommentme = null; CookieUtil.feedlike = null
        CookieUtil.contacts_follow = null; CookieUtil.message = null; CookieUtil.notification = 0
    }
}
