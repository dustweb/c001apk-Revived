package com.example.c001apk.compose.ui.main

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigation.*
import androidx.navigation.compose.LocalOwnersProvider
import androidx.navigation.compose.rememberNavController
import com.example.c001apk.compose.ui.theme.isMiuix
import kotlinx.coroutines.flow.collectLatest
import top.yukonga.miuix.kmp.nav.core.*
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.nav.transition.navGraphicsTransition
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** AndroidX owns routes, arguments and saved state; Miuix owns the interactive presentation. */
@Navigator.Name("app-page")
class AppPageNavigator : Navigator<AppPageNavigator.Destination>() {
    val entries get() = state.backStack

    override fun createDestination() = Destination(this)

    override fun navigate(entries: List<NavBackStackEntry>, navOptions: NavOptions?, navigatorExtras: Extras?) {
        entries.forEach(state::pushWithTransition)
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        state.popWithTransition(popUpTo, savedState)
    }

    fun prepare() = entries.value.forEach(state::prepareForTransition)
    fun complete(entry: NavBackStackEntry) = state.markTransitionComplete(entry)
    fun completeTransitions() = state.transitionsInProgress.value.toList().forEach(state::markTransitionComplete)

    class Destination(navigator: AppPageNavigator) : NavDestination(navigator) {
        var content: @Composable (NavBackStackEntry) -> Unit = {}
    }
}

@Composable
fun rememberAppNavController(): NavHostController {
    val navigator = remember { AppPageNavigator() }
    return rememberNavController(navigator)
}

fun NavGraphBuilder.appComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    val navigator = provider.getNavigator<AppPageNavigator>("app-page")
    addDestination(AppPageNavigator.Destination(navigator).apply {
        this.route = route
        arguments.forEach { addArgument(it.name, it.argument) }
        this.content = content
    })
}

private class PageKey(val entry: NavBackStackEntry) : NavKey {
    override fun equals(other: Any?) = other is PageKey && entry.id == other.entry.id
    override fun hashCode() = entry.id.hashCode()
    override fun toString() = entry.id
}

private val MaterialPageTransition = navGraphicsTransition(opaqueDepth = 2f) { scope ->
    val depth = scope.relativeDepth.coerceIn(-1f, 1f)
    translationX = -depth * scope.layoutSize.width * 0.15f
    alpha = 1f - kotlin.math.abs(depth)
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    isPane: Boolean = false,
    backEnabled: Boolean = true,
    builder: NavGraphBuilder.() -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val storeOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val graph = remember(navController, startDestination, builder) {
        navController.createGraph(startDestination = startDestination, builder = builder)
    }
    navController.setViewModelStore(storeOwner.viewModelStore)
    navController.graph = graph
    val navigator = navController.navigatorProvider.getNavigator<AppPageNavigator>("app-page")
    DisposableEffect(navController, lifecycleOwner) {
        navController.setLifecycleOwner(lifecycleOwner)
        onDispose { navigator.completeTransitions() }
    }

    val entries by navigator.entries.collectAsState()
    // This list is a presentation mirror. AndroidX persists the authoritative stack and entry IDs.
    val stack = remember(navController) { navBackStackOf() }
    val keys = entries.map(::PageKey)
    SideEffect {
        if (stack.toList() != keys) Snapshot.withMutableSnapshot {
            stack.clear()
            stack.addAll(keys)
        }
    }
    val stateHolder = rememberSaveableStateHolder()
    val miuix = isMiuix()
    val cornerRadius = rememberNavSystemCornerRadius()
    val parentDispatcher = checkNotNull(LocalNavigationEventDispatcherOwner.current)
    val backOwner = remember(parentDispatcher) {
        object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = NavigationEventDispatcher(parent = parentDispatcher.navigationEventDispatcher)
        }
    }
    SideEffect {
        try { backOwner.navigationEventDispatcher.isEnabled = backEnabled }
        catch (_: IllegalStateException) { /* The Activity has already disposed the dispatcher tree. */ }
    }
    DisposableEffect(backOwner) {
        onDispose {
            try { backOwner.navigationEventDispatcher.dispose() }
            catch (_: IllegalStateException) { /* Parent disposal also disposes its descendants. */ }
        }
    }
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides backOwner) {
        if (stack.isNotEmpty()) NavDisplay(
            backStack = stack,
            modifier = modifier,
            onBack = { navController.popBackStack() },
            transition = if (miuix) NavTransitions.MiuixDefault else MaterialPageTransition,
            effects = if (miuix) NavDisplayEffects(
                cornerClipRadius = if (isPane) 0.dp else cornerRadius,
                backdropColor = MiuixTheme.colorScheme.surface,
            ) else NavDisplayEffects.None,
        ) {
            entry<PageKey>(contentKey = { it.entry.id }) { page ->
                val motion = LocalNavTransitionScope.current
                LaunchedEffect(motion, navigator) {
                    snapshotFlow { motion.isRunning }.collectLatest { running ->
                        if (running) navigator.prepare() else {
                            // Reconciliation begins after composition; do not complete a just-pushed entry early.
                            withFrameNanos { }
                            if (!motion.isRunning) navigator.completeTransitions()
                        }
                    }
                }
                DisposableEffect(page.entry) {
                    onDispose { navigator.complete(page.entry) }
                }
                page.entry.LocalOwnersProvider(stateHolder) {
                    val isTop = entries.lastOrNull()?.id == page.entry.id
                    Box(Modifier.fillMaxSize().then(if (isTop) Modifier else Modifier.clearAndSetSemantics {})) {
                        (page.entry.destination as AppPageNavigator.Destination).content(page.entry)
                    }
                }
            }
        }
    }
}
