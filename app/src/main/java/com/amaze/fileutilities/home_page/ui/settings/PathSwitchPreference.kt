/*
 * Copyright (C) 2021-2022 Team Amaze - Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com>. All Rights reserved.
 *
 * This file is part of Amaze File Utilities.
 *
 * 'Amaze File Utilities' is a registered trademark of Team Amaze. All other product
 * and company names mentioned are trademarks or registered trademarks of their respective owners.
 */

package com.amaze.fileutilities.home_page.ui.settings

import android.content.Context
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.amaze.fileutilities.R

class PathSwitchPreference(
    context: Context,
    private val onEdit: ((PathSwitchPreference) -> Unit)?,
    private val onDelete: (PathSwitchPreference) -> Unit
) : Preference(context) {
    var lastItemClicked = -1
        private set

    init {
        widgetLayoutResource = R.layout.namepathswitch_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        holder.itemView.let { view ->
            view.findViewById<View>(R.id.edit).setOnClickListener {
                onEdit?.let { it1 ->
                    it1(this)
                }
            }
            view.findViewById<View>(R.id.delete).setOnClickListener { onDelete(this) }
            view.setOnClickListener(null)
        }

        // Keep this before things that need changing what's on screen
        super.onBindViewHolder(holder)
    }
}
