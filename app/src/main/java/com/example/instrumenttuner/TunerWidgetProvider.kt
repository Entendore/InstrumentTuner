package com.example.instrumenttuner

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TunerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, mgr, it) }
    }

    companion object {
        fun refresh(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, TunerWidgetProvider::class.java))
            ids.forEach { update(context, mgr, it) }
        }

        private fun update(context: Context, mgr: AppWidgetManager, id: Int) {
            val prefs = context.getSharedPreferences("tuner_prefs", Context.MODE_PRIVATE)
            val name = prefs.getString("instrument", null) ?: "Guitar"
            val best = prefs.getFloat("practice_best_$name", 0f)
            val views = RemoteViews(context.packageName, R.layout.widget_tuner)
            views.setTextViewText(R.id.widget_instrument, name)
            views.setTextViewText(R.id.widget_sub,
                if (best > 0f) "best ${best.toInt()}s · tap to tune" else "tap to tune")
            val intent = Intent(context, MainActivity::class.java)
            val pi = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widget_root, pi)
            mgr.updateAppWidget(id, views)
        }
    }
}
