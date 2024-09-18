package com.amaze.fileutilities.home_page.ui.options

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.amaze.fileutilities.utilis.PreferencesConstants
import com.amaze.fileutilities.utilis.Utils
import com.amaze.fileutilities.utilis.getAppCommonSharedPreferences
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Billing(val context: Context, private var uniqueId: String) {

    var log: Logger = LoggerFactory.getLogger(Billing::class.java)

    companion object {
        private const val URL_AUTHOR_2_PAYPAL = "https://www.paypal.me/vishalnehra"
        private const val URL_LIBERAPAY = "https://liberapay.com/Team-Amaze/donate"

        fun getInstance(activity: AppCompatActivity): Billing? {
            var billing: Billing? = null
            val deviceId = activity.getAppCommonSharedPreferences()
                .getString(PreferencesConstants.KEY_DEVICE_UNIQUE_ID, null)
            deviceId?.let {
                billing = Billing(activity, deviceId)
            }
            return billing
        }

        fun getInstance(context: Context): Billing? {
            var billing: Billing? = null
            val deviceId = context.getAppCommonSharedPreferences()
                .getString(PreferencesConstants.KEY_DEVICE_UNIQUE_ID, null)
            deviceId?.let {
                billing = Billing(context, deviceId)
            }
            return billing
        }
    }

    fun getSubscriptions(resultCallback: () -> Unit) {
        resultCallback.invoke()
    }

    /** Start a purchase flow  */
    fun initiatePurchaseFlow() {
        Utils.buildPurchaseFdroidDialog(context, {
            Utils.openURL(URL_AUTHOR_2_PAYPAL, context)
        }, {
            Utils.openURL(URL_LIBERAPAY, context)
        }).show()
    }
}
