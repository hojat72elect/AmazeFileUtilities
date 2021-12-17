package com.amaze.fileutilities.video_player

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.amaze.fileutilities.PermissionActivity
import com.amaze.fileutilities.R
import com.amaze.fileutilities.databinding.VideoPlayerDialogActivityBinding

class VideoPlayerDialogActivity: PermissionActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        VideoPlayerDialogActivityBinding.inflate(layoutInflater)
    }
    private lateinit var videoModel: LocalVideoModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        if (savedInstanceState == null) {
            val mimeType = intent.type
            val videoUri = intent.data
            Log.i(javaClass.simpleName, "Loading video from path ${videoUri?.path} " +
                    "and mimetype $mimeType")
            videoModel = LocalVideoModel(uri = videoUri!!, mimeType = mimeType!!)
            val bundle = bundleOf(
                VideoPlayerFragment.VIEW_TYPE_ARGUMENT to videoModel
            )

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<VideoPlayerFragment>(R.id.fragment_container_view, args = bundle)
            }
        }
    }
}