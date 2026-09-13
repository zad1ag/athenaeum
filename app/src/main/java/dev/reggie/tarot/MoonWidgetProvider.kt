package dev.reggie.tarot

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MoonWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildViews(context, appWidgetManager, id))
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        // Re-layout when the user resizes the widget.
        appWidgetManager.updateAppWidget(appWidgetId, buildViews(context, appWidgetManager, appWidgetId))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Refresh all widgets whenever the phase changes (day rollover alarms
        // come through as ACTION_TIME_CHANGED / our own update action).
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MoonWidgetProvider::class.java))
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        fun pushUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MoonWidgetProvider::class.java))
            for (id in ids) {
                manager.updateAppWidget(id, buildViews(context, manager, id))
            }
        }

        fun moonPhaseForDateForWidget(): MoonPhase {
            val knownNewMoon = LocalDate.of(2000, 1, 6)
            val cycle = 29.53059
            val days = ChronoUnit.DAYS.between(knownNewMoon, LocalDate.now()).toDouble()
            val phase = (days % cycle) / cycle
            return when (phase) {
                in 0.0..0.025, in 0.975..1.0 -> MoonPhase.NewMoon
                in 0.025..0.225 -> MoonPhase.WaxingCrescent
                in 0.225..0.275 -> MoonPhase.FirstQuarter
                in 0.275..0.475 -> MoonPhase.WaxingGibbous
                in 0.475..0.525 -> MoonPhase.FullMoon
                in 0.525..0.725 -> MoonPhase.WaningGibbous
                in 0.725..0.775 -> MoonPhase.LastQuarter
                else -> MoonPhase.WaningCrescent
            }
        }

        fun buildViews(context: Context, manager: AppWidgetManager, appWidgetId: Int): RemoteViews {
            val phase = moonPhaseForDateForWidget()
            val opts = manager.getAppWidgetOptions(appWidgetId)
            val minWidthDp = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
            val minHeightDp = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 130)
            val compact = minWidthDp < 140 || minHeightDp < 130

            val views = if (compact) {
                RemoteViews(context.packageName, R.layout.widget_moon_small)
            } else {
                RemoteViews(context.packageName, R.layout.widget_moon)
            }

            views.setImageViewResource(R.id.widget_moon_icon, widgetIconRes(phase))
            views.setImageViewResource(R.id.widget_moon_icon_shadow, widgetIconRes(phase))
            views.setTextViewText(R.id.widget_moon_label, readablePhase(phase))
            if (!compact) {
                views.setTextViewText(R.id.widget_moon_illumination, "${illuminationPercent()}% illuminated")
            }

            // Optional card background behind the content (user toggle).
            if (FeatureSettings.isWidgetBackgroundEnabled(context)) {
                views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_moon_bg)
            } else {
                views.setInt(R.id.widget_root, "setBackgroundResource", android.R.color.transparent)
            }

            // Material You: on Android 12+ tint the moon and text with the
            // system dynamic palette so the widget matches the wallpaper theme.
            // Note: only set views that exist in the chosen layout — applying
            // actions to missing views breaks the whole RemoteViews update.
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                val accent = runCatching {
                    context.getColor(android.R.color.system_accent1_100)
                }.getOrNull() ?: defaultAccent()
                views.setColorStateList(
                    R.id.widget_moon_label,
                    "setTextColor",
                    android.content.res.ColorStateList.valueOf(accent)
                )
                if (!compact) {
                    views.setColorStateList(
                        R.id.widget_moon_illumination,
                        "setTextColor",
                        android.content.res.ColorStateList.valueOf(accent)
                    )
                }
                // Moon icon follows the accent like the text does.
                views.setColorStateList(
                    R.id.widget_moon_icon,
                    "setImageTintList",
                    android.content.res.ColorStateList.valueOf(accent)
                )
            }

            if (FeatureSettings.isWidgetBackgroundEnabled(context) &&
                android.os.Build.VERSION.SDK_INT >= 31
            ) {
                val container = runCatching {
                    context.resources.getColor(android.R.color.system_neutral1_800, context.theme)
                }.getOrDefault(0xFF1A1A1E.toInt())
                views.setColorStateList(
                    R.id.widget_root,
                    "setBackgroundTintList",
                    android.content.res.ColorStateList.valueOf(container)
                )
            }

            // Tap opens MainActivity with the moon calendar flag.
            val open = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_moon_calendar", true)
            }
            val pi = PendingIntent.getActivity(
                context,
                0,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)
            return views
        }

        fun widgetIconRes(phase: MoonPhase): Int = when (phase) {
            MoonPhase.NewMoon -> R.drawable.ic_moon_new
            MoonPhase.WaxingCrescent -> R.drawable.ic_moon_waxing_crescent
            MoonPhase.FirstQuarter -> R.drawable.ic_moon_first_quarter
            MoonPhase.WaxingGibbous -> R.drawable.ic_moon_waxing_gibbous
            MoonPhase.FullMoon -> R.drawable.ic_moon_full
            MoonPhase.WaningGibbous -> R.drawable.ic_moon_waning_gibbous
            MoonPhase.LastQuarter -> R.drawable.ic_moon_last_quarter
            MoonPhase.WaningCrescent -> R.drawable.ic_moon_waning_crescent
        }

        fun readablePhase(phase: MoonPhase): String =
            phase.name.replace(Regex("([a-z])([A-Z])"), "$1 $2")

        /** Pale gold matching the app theme, used when dynamic color is unavailable. */
        fun defaultAccent(): Int = 0xFFC9A959.toInt()

        fun illuminationPercent(): Int {
            val knownNewMoon = LocalDate.of(2000, 1, 6)
            val cycle = 29.53059
            val age = ChronoUnit.DAYS.between(knownNewMoon, LocalDate.now()).toDouble() % cycle
            return ((1 - kotlin.math.cos(2 * Math.PI * age / cycle)) / 2 * 100).toInt()
        }
    }
}