package com.amaze.fileutilities.docx_viewer

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebViewFeature
import com.amaze.fileutilities.PermissionsActivity
import com.amaze.fileutilities.R
import com.amaze.fileutilities.databinding.DocxViewerActivityBinding
import com.amaze.fileutilities.utilis.showToastInCenter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zwobble.mammoth.DocumentConverter
import org.zwobble.mammoth.Result

class DocxViewerActivity : PermissionsActivity() {
    var log: Logger = LoggerFactory.getLogger(DocxViewerActivity::class.java)

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        DocxViewerActivityBinding.inflate(layoutInflater)
    }
    private lateinit var docxModel: LocalDocxModel
    private lateinit var viewModel: DocxViewerActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                resources
                    .getColor(R.color.navy_blue)
            )
        )
        viewModel = ViewModelProvider(this).get(DocxViewerActivityViewModel::class.java)
        if (viewModel.getDocxModel(intent) == null) {
            showToastInCenter(resources.getString(R.string.unsupported_content))
            finish()
        }
        docxModel = viewModel.getDocxModel(intent)!!
        log.info(
            "Loading docx from path ${docxModel.getUri().path} " +
                    "and mimetype ${docxModel.mimeType}"
        )
        try {
            val converter = DocumentConverter()
            val result: Result<String>? = converter.convertToHtml(docxModel.getInputStream(this))
            title = docxModel.getName(this)
            result?.let {
                val html: String = result.value // The generated HTML
                val base64 = Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING)
                viewBinding.webview.also {
                    it.loadData(base64, "text/html", "base64")
                    it.settings.setSupportZoom(true)
                    it.settings.builtInZoomControls = true
                    it.settings.displayZoomControls = false
                    it.isVerticalScrollBarEnabled = true
                    it.isHorizontalScrollBarEnabled = true
                }
            }
        } catch (e: Exception) {
            log.error("Failed to load document", e)
            showToastInCenter(getString(R.string.failed_to_load_document))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.docx_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.also {
            it.findItem(R.id.invert_colors).isChecked = viewModel.nightMode
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.invert_colors -> {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    viewModel.nightMode = !viewModel.nightMode
                    WebSettingsCompat.setForceDark(
                        viewBinding.webview.settings,
                        if (viewModel.nightMode) FORCE_DARK_ON else FORCE_DARK_OFF
                    )
                    viewBinding.webview.reload()
                }
            }

            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
