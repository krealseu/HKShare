package org.kreal.hkshare

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat

/**
 * Helper class for showing and canceling hkshare
 * notifications.
 *
 *
 * This class makes heavy use of the [NotificationCompat.Builder] helper
 * class to create notifications in a backward-compatible way.
 */
object HKShareNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private val NOTIFICATION_TAG = "HKShare"
    private val NOTIFICATION_ID = 233

    fun notiff(context: Context, block: NotificationCompat.Builder.() -> Unit) {
        val builder = NotificationCompat.Builder(context)
        builder.block()
        notify(context, builder.build())
    }

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     *
     *
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     *
     *
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of hkshare notifications. Make
     * sure to follow the
     * [
 * Notification design guidelines](https://developer.android.com/design/patterns/notifications.html) when doing so.
     *
     * @see .cancel
     */
    fun notify(context: Context,
               ipString: String, number: Int = 0) {
        val res = context.resources

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        val picture = BitmapFactory.decodeResource(res, R.drawable.example_picture)

        val title = res.getString(
                R.string.hkshare_notification_title_template, "Servicing  > > > >")
        val text = res.getString(
                R.string.hkshare_notification_placeholder_text_template, ipString)

        val builder = NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
//                .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_stat_hkshare)
                .setContentTitle(title)
                .setContentText(text)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
//                .setLargeIcon(picture)

                // Set ticker text (preview) information for this notification.
                .setTicker(ipString)

                // Show a number. This is useful when stacking notifications of
                // a single type.
                .setNumber(number)

                // If this notification relates to a past or upcoming event, you
                // should set the relevant time information using the setWhen
                // method below. If this call is omitted, the notification's
                // timestamp will by set to the time at which it was shown.
                // TODO: Call setWhen if this notification relates to a past or
                // upcoming event. The sole argument to this method should be
                // the notification timestamp in milliseconds.
                //.setWhen(...)

                // Set the pending intent to be initiated when the user touches
                // the notification.
//                .setContentIntent(
//                        PendingIntent.getActivity(
//                                context,
//                                0,
//                                QRActivity.intent(context,text), PendingIntent.FLAG_UPDATE_CURRENT))

                // Example additional actions for this notification. These will
                // only show on devices running Android 4.1 or later, so you
                // should ensure that the activity in this notification's
                // content intent provides access to the same actions in
                // another way.
                .addAction(
                        R.drawable.ic_action_stat_reply,
                        res.getString(R.string.action_stop),
                        PendingIntent.getService(context, 0, HKService.stopIntent(context), PendingIntent.FLAG_CANCEL_CURRENT))

                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(false)
                .setOngoing(true)
        notify(context, builder.build())
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private fun notify(context: Context, notification: Notification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification)
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification)
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * [.notify].
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    infix fun cancel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, NOTIFICATION_ID)
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode())
        }
    }

}
