package com.example.c001apk.compose.ui.component.design

import com.example.c001apk.compose.ui.component.icons.Swatch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import org.junit.Assert.assertNotSame
import org.junit.Test

class IconResolutionTest {
    @Test fun sharedMaterialActionsHaveNativeOrProductGlyphs() {
        listOf(
            Icons.AutoMirrored.Default.Logout,
            Icons.AutoMirrored.Default.Send,
            Icons.AutoMirrored.Filled.ArrowBack,
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            Icons.AutoMirrored.Filled.ArrowRight,
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            Icons.AutoMirrored.Filled.Message,
            Icons.AutoMirrored.Filled.Reply,
            Icons.AutoMirrored.Outlined.ArrowBack,
            Icons.AutoMirrored.Outlined.Backspace,
            Icons.AutoMirrored.Outlined.Feed,
            Icons.AutoMirrored.Outlined.HelpOutline,
            Icons.AutoMirrored.Outlined.Message,
            Icons.Default.Add,
            Icons.Default.AlternateEmail,
            Icons.Default.Check,
            Icons.Default.ChevronRight,
            Icons.Default.Clear,
            Icons.Default.ClearAll,
            Icons.Default.Close,
            Icons.Default.Download,
            Icons.Default.DragHandle,
            Icons.Default.ErrorOutline,
            Icons.Default.ExpandMore,
            Icons.Default.Home,
            Icons.Default.Link,
            Icons.Default.MoreVert,
            Icons.Default.Search,
            Icons.Default.Settings,
            Icons.Default.Share,
            Icons.Default.Smartphone,
            Icons.Default.ThumbUpOffAlt,
            Icons.Default.Tune,
            Icons.Default.Update,
            Icons.Default.Upload,
            Icons.Filled.ThumbUpAlt,
            Icons.Filled.Visibility,
            Icons.Filled.VisibilityOff,
            Icons.Outlined.AddCircleOutline,
            Icons.Outlined.AddPhotoAlternate,
            Icons.Outlined.AllInclusive,
            Icons.Outlined.Archive,
            Icons.Outlined.Block,
            Icons.Outlined.CardGiftcard,
            Icons.Outlined.ChatBubbleOutline,
            Icons.Outlined.CleaningServices,
            Icons.Outlined.Code,
            Icons.Outlined.DarkMode,
            Icons.Outlined.DeveloperMode,
            Icons.Outlined.EmojiEmotions,
            Icons.Outlined.FavoriteBorder,
            Icons.Outlined.ForkRight,
            Icons.Outlined.FormatColorFill,
            Icons.Outlined.History,
            Icons.Outlined.Home,
            Icons.Outlined.Image,
            Icons.Outlined.ImageAspectRatio,
            Icons.Outlined.Info,
            Icons.Outlined.InvertColors,
            Icons.Outlined.Mail,
            Icons.Outlined.MyLocation,
            Icons.Outlined.Notifications,
            Icons.Outlined.Palette,
            Icons.Outlined.PersonAdd,
            Icons.Outlined.PhotoLibrary,
            Icons.Outlined.Schedule,
            Icons.Outlined.Settings,
            Icons.Outlined.Smartphone,
            Icons.Outlined.Source,
            Icons.Outlined.StarOutline,
            Icons.Outlined.SupervisorAccount,
            Icons.Outlined.SystemUpdate,
            Icons.Outlined.TextFields,
            Icons.Outlined.ThumbUp,
            Icons.Outlined.TouchApp,
            Icons.Outlined.TravelExplore,
            Icons.Rounded.ArrowUpward,
            Icons.Rounded.Cancel,
            Icons.Rounded.Swatch,
        ).forEach { original ->
            assertNotSame("Missing Miuix glyph: ${original.name}", original, miuixIcon(original))
        }
    }
}
