package com.example.c001apk.compose.ui.component.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*

/** Resolves shared action semantics to the active design system's actual glyphs. */
internal fun miuixIcon(vector: ImageVector): ImageVector {
    val name = vector.name
    if (name == "Swatch") return MiuixIcons.Medium.Theme
    if (!name.startsWith("Filled.") && !name.startsWith("Outlined.") &&
        !name.startsWith("Rounded.") && !name.startsWith("AutoMirrored.")) return vector
    return when (name.substringAfterLast('.')) {
        "ArrowBack" -> MiuixIcons.Medium.Back
        "ArrowForwardIos", "ArrowRight", "KeyboardArrowRight", "ChevronRight" -> MiuixIcons.Medium.ChevronForward
        "Search" -> MiuixIcons.Medium.Search
        "Close", "Clear", "Cancel" -> MiuixIcons.Medium.Close
        "Check" -> MiuixIcons.Medium.Ok
        "Add" -> MiuixIcons.Medium.Add
        "AddCircleOutline" -> MiuixIcons.Medium.AddCircle
        "MoreVert" -> MiuixIcons.Medium.More
        "ExpandMore" -> MiuixIcons.Medium.ExpandMore
        "DragHandle" -> MiuixIcons.Medium.Sort
        "Home" -> if (name.startsWith("Filled.")) MiuixIcons.Demibold.Home else MiuixIcons.Medium.Home
        "Settings" -> if (name.startsWith("Filled.")) MiuixIcons.Demibold.Settings else MiuixIcons.Medium.Settings
        "Message", "ChatBubbleOutline" -> if (name.contains("Outlined")) MiuixIcons.Medium.Reply else MiuixIcons.Medium.Messages
        "Reply" -> MiuixIcons.Medium.Reply
        "Send" -> MiuixIcons.Medium.Send
        "Backspace" -> MiuixIcons.Medium.Clear
        "Feed" -> MiuixIcons.Medium.Notes
        "Download" -> MiuixIcons.Medium.Download
        "Upload" -> MiuixIcons.Medium.UploadCloud
        "Image", "AddPhotoAlternate", "ImageAspectRatio" -> MiuixIcons.Medium.Image
        "PhotoLibrary" -> MiuixIcons.Medium.Photos
        "Mail" -> MiuixIcons.Medium.Email
        "SupervisorAccount", "PersonAdd" -> MiuixIcons.Medium.Contacts
        "History", "Schedule" -> MiuixIcons.Medium.Recent
        "Update", "SystemUpdate" -> MiuixIcons.Medium.Update
        "Link" -> MiuixIcons.Medium.Link
        "Share" -> MiuixIcons.Medium.Share
        "Block" -> MiuixIcons.Medium.Blocklist
        "Info", "ErrorOutline" -> MiuixIcons.Medium.Info
        "HelpOutline" -> MiuixIcons.Medium.Help
        "Source" -> MiuixIcons.Medium.Folder
        "Archive" -> MiuixIcons.Medium.Backup
        "ClearAll", "CleaningServices" -> MiuixIcons.Medium.Clear
        "Palette", "Swatch", "FormatColorFill", "InvertColors" -> MiuixIcons.Medium.Theme
        "Tune" -> MiuixIcons.Medium.Tune
        "Visibility" -> MiuixIcons.Medium.Show
        "VisibilityOff" -> MiuixIcons.Medium.Hide
        "MyLocation" -> MiuixIcons.Medium.Location
        "FavoriteBorder" -> MiuixIcons.Medium.Favorites
        "ThumbUp", "ThumbUpOffAlt" -> AppGlyphs.Like
        "ThumbUpAlt" -> AppGlyphs.LikeFilled
        "EmojiEmotions" -> AppGlyphs.Emoji
        "Smartphone", "DeveloperMode" -> AppGlyphs.Device
        "AllInclusive" -> AppGlyphs.Infinity
        "Code" -> AppGlyphs.Code
        "ForkRight" -> AppGlyphs.Fork
        "DarkMode" -> AppGlyphs.Moon
        "TextFields" -> AppGlyphs.TextSize
        "TouchApp" -> AppGlyphs.Touch
        "CardGiftcard" -> AppGlyphs.Gift
        "Notifications" -> AppGlyphs.Bell
        "StarOutline" -> AppGlyphs.Star
        "AlternateEmail" -> AppGlyphs.Mention
        "TravelExplore" -> AppGlyphs.Globe
        "ArrowUpward" -> AppGlyphs.BackToTop
        "Logout" -> AppGlyphs.Logout
        else -> vector
    }
}

/** Product actions without an equivalent in the pinned Miuix icon set. */
object AppGlyphs {
    private fun glyph(name: String, path: String, filled: Boolean = false): ImageVector =
        ImageVector.Builder("App.$name", 24.dp, 24.dp, 24f, 24f).addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            fill = if (filled) SolidColor(Color.Black) else null,
            stroke = SolidColor(Color.Black), strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        ).build()

    private const val LikePath = "M8 10 L12 4 Q13 2 14 4 Q15 6 13 10 H19 Q21 10 20.5 12 L19 19 Q18.8 20 17 20 H8 Z M4 10 H8 V20 H4 Z"
    val Like by lazy { glyph("Like", LikePath) }
    val LikeFilled by lazy { glyph("LikeFilled", LikePath, true) }
    val Emoji by lazy { glyph("Emoji", "M21 12 A9 9 0 1 1 3 12 A9 9 0 1 1 21 12 M8 9 H8.1 M16 9 H16.1 M8 14 Q12 19 16 14") }
    val Device by lazy { glyph("Device", "M8 3 H16 Q18 3 18 5 V19 Q18 21 16 21 H8 Q6 21 6 19 V5 Q6 3 8 3 Z M10 5 H14 M11 18.5 H13") }
    val Infinity by lazy { glyph("Infinity", "M12 12 C9 8 8 7 6 7 C0 7 0 17 6 17 C8 17 9 16 12 12 C15 8 16 7 18 7 C24 7 24 17 18 17 C16 17 15 16 12 12") }
    val Code by lazy { glyph("Code", "M8 6 L3 12 L8 18 M16 6 L21 12 L16 18 M14 4 L10 20") }
    val Fork by lazy { glyph("Fork", "M7 21 V4 M4 7 L7 4 L10 7 M7 15 C7 11 16 16 17 8 M14 10 L17 7 L20 10") }
    val Moon by lazy { glyph("Moon", "M18 16 A9 9 0 0 1 8 4 A9 9 0 1 0 20 15 Z") }
    val TextSize by lazy { glyph("TextSize", "M3 6 H15 M9 6 V20 M17 11 H23 M20 11 V20") }
    val Touch by lazy { glyph("Touch", "M10 12 V6 C10 3 14 3 14 6 V11 C16 9 17 12 18 12 C21 11 22 14 21 17 L19 21 H11 L6 15 C4 12 6 11 8 13 L10 15 M5 7 C4 2 10 0 14 2 M18 4 L19 7") }
    val Gift by lazy { glyph("Gift", "M4 10 H20 V21 H4 Z M3 7 H21 V11 H3 Z M12 7 V21 M12 7 C5 8 4 3 7 3 C9 3 11 5 12 7 C13 5 15 3 17 3 C20 3 19 8 12 7") }
    val Bell by lazy { glyph("Bell", "M5 17 Q7 14 7 9 C7 2 17 2 17 9 Q17 14 19 17 Z M10 20 Q12 23 14 20 M12 2 V4") }
    val Star by lazy { glyph("Star", "M12 3 L15 9 L22 10 L17 15 L18 22 L12 18.5 L6 22 L7 15 L2 10 L9 9 Z") }
    val Mention by lazy { glyph("Mention", "M16 8 V15 C16 19 22 16 21 11 C20 0 3 1 3 12 C3 21 13 23 18 19 M16 12 C16 5 8 7 8 12 C8 19 16 17 16 12") }
    val Globe by lazy { glyph("Globe", "M21 12 A9 9 0 1 1 3 12 A9 9 0 1 1 21 12 M3 12 H21 M12 3 C6 8 6 16 12 21 C18 16 18 8 12 3") }
    val BackToTop by lazy { glyph("BackToTop", "M5 4 H19 M6 13 L12 7 L18 13 M12 7 V21") }
    val Topic by lazy { glyph("Topic", "M9 3 L7 21 M17 3 L15 21 M4 9 H21 M3 16 H20") }
    val Logout by lazy { glyph("Logout", "M10 4 H5 V20 H10 M9 12 H21 M17 8 L21 12 L17 16") }
}
