package com.amaze.fileutilities.home_page.ui.files

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.amaze.fileutilities.R
import com.amaze.fileutilities.utilis.AbstractMediaFilesAdapter

class RecentMediaFilesAdapter(
    val context: Context,
    val preloader: MediaAdapterPreloader<MediaFileInfo>,
    private val mediaFileInfoList: MutableList<MediaFileInfo>,
    toggleCheckCallback: (Int, Int, String) -> Unit,
) :
    AbstractMediaFilesAdapter(
        context, preloader, false, null,
        toggleCheckCallback
    ) {

    private var mediaFileListItems: MutableList<ListItem> = mutableListOf()
        set(value) {
            value.clear()
            mediaFileInfoList.forEach {
                value.add(ListItem(mediaFileInfo = it))
                preloader.addItem(it)
            }
            if (mediaFileInfoList.size != 0) {
                preloader.addItem(null)
                value.add(ListItem(EMPTY_LAST_ITEM))
            }
            field = value
        }

    init {
        mediaFileListItems = mutableListOf()
    }

    override fun getItemCount(): Int {
        return mediaFileListItems.size
    }

    override fun getOnlyItemsCount(): Int {
        return mediaFileListItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return mediaFileListItems[position].listItemType
    }

    override fun getMediaFilesListItems(): MutableList<ListItem> {
        return mediaFileListItems
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is MediaInfoRecyclerViewHolder) {
            getMediaFilesListItems()[position].run {
                if (isChecked) {
                    holder.root.background = ResourcesCompat
                        .getDrawable(
                            context.resources,
                            R.drawable.background_curved_recents_selected,
                            context.theme
                        )
                } else {
                    holder.root.background = ResourcesCompat
                        .getDrawable(
                            context.resources,
                            R.drawable.background_curved_recents,
                            context.theme
                        )
                }
            }
        }
    }

    /**
     * Set list elements
     */
    fun setData(data: List<MediaFileInfo>) {
        mediaFileInfoList.run {
            clear()
            preloader.clear()
            addAll(data)
            // triggers set call
            mediaFileListItems = mutableListOf()
            notifyDataSetChanged()
        }
    }
}
