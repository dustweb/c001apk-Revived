package com.example.c001apk.compose.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.repository.AccountRepository
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.ui.base.PrefsViewModel
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.encode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val networkRepo: NetworkRepo,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val accounts: AccountRepository,
) : PrefsViewModel(userPreferencesRepository) {
    var badge by mutableIntStateOf(0)
        private set
    private var navigationGeneration = -1L
    private var navigationStore = androidx.lifecycle.ViewModelStore()
    fun navigationOwner(generation: Long): androidx.lifecycle.ViewModelStoreOwner {
        if (navigationGeneration != generation) {
            navigationStore.clear()
            navigationStore = androidx.lifecycle.ViewModelStore()
            navigationGeneration = generation
        }
        val store = navigationStore
        return object : androidx.lifecycle.ViewModelStoreOwner { override val viewModelStore = store }
    }

    override fun onCleared() {
        navigationStore.clear()
        super.onCleared()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.data.map { it.sessionGeneration }.distinctUntilChanged().collectLatest { generation ->
                badge = 0
                CookieUtil.SESSID = ""
                CookieUtil.atme = null; CookieUtil.atcommentme = null; CookieUtil.feedlike = null
                CookieUtil.contacts_follow = null; CookieUtil.message = null
                val session = userPreferencesRepository.data.first()
                networkRepo.checkLoginInfo().collect { result ->
                    if (userPreferencesRepository.data.first().sessionGeneration != generation) return@collect
                    val response = result.getOrNull() ?: return@collect
                    response.body()?.data?.takeIf { session.isLogin && it.uid == session.uid }?.let { login ->
                        badge = login.notifyCount.badge
                        CookieUtil.atme = login.notifyCount.atme
                        CookieUtil.atcommentme = login.notifyCount.atcommentme
                        CookieUtil.feedlike = login.notifyCount.feedlike
                        CookieUtil.contacts_follow = login.notifyCount.contactsFollow
                        CookieUtil.message = login.notifyCount.message
                        try { accounts.updateProfile(generation, login.uid, login.username.encode, login.userAvatar, token = login.token) }
                        catch (e: kotlinx.coroutines.CancellationException) { throw e }
                        catch (_: Exception) { /* Keep the last stored profile if secure storage is unavailable. */ }
                    }
                    if (session.isLogin && response.body()?.message == "登录信息有误") accounts.expire(generation)
                    if (userPreferencesRepository.data.first().sessionGeneration != generation) return@collect
                    response.headers().values("Set-Cookie").firstOrNull { it.startsWith("SESSID=") }
                        ?.let { CookieUtil.SESSID = it.substringBefore(';') }
                }
            }
        }
    }

    fun resetBadge() { badge = 0 }
}
