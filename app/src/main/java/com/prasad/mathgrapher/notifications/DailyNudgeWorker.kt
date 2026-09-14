package com.prasad.mathgrapher.notifications

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "daily_nudge"

class DailyNudgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        showNudgeNotification(applicationContext)
        scheduleNextNudge(applicationContext, ExistingWorkPolicy.REPLACE) // chain: pick tomorrow's random hour now
        return Result.success()
    }
}

// windowStartHour/EndHour: the range of hours it's allowed to fire in, 24h format.
// 10..20 means "somewhere between 10am and 8pm" — adjust to taste.
private fun computeRandomDelayMillis(windowStartHour: Int = 10, windowEndHour: Int = 20): Long {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, (windowStartHour until windowEndHour).random())
        set(Calendar.MINUTE, (0..59).random())
        set(Calendar.SECOND, 0)
    }
    return (target.timeInMillis - now.timeInMillis).coerceAtLeast(60_000L)
}

fun scheduleNextNudge(context: Context, policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
    val request = OneTimeWorkRequestBuilder<DailyNudgeWorker>()
        .setInitialDelay(computeRandomDelayMillis(), TimeUnit.MILLISECONDS)
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
}
