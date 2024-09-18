

package com.amaze.fileutilities.home_page.ui.options

import android.view.View
import androidx.mediarouter.app.MediaRouteButton
import com.amaze.fileutilities.PermissionsActivity
import com.amaze.fileutilities.home_page.ui.files.MediaFileInfo

abstract class CastActivity : PermissionsActivity() {

    fun refactorCastButton(mediaRouteButton: MediaRouteButton) {
        mediaRouteButton.visibility = View.GONE
    }

    fun showCastFileDialog(
        mediaFileInfo: MediaFileInfo,
        mediaType: Int,
        inbuiltCallback: () -> Unit
    ) {
        inbuiltCallback.invoke()
    }
}
