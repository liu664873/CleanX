package com.quickcleanpro.phonecleaner.presentation.common.permission

import androidx.annotation.StringRes
import com.quickcleanpro.phonecleaner.R

data class CleanXPermissionCopy(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val hint1Res: Int,
    @StringRes val hint2Res: Int,
    @StringRes val allowRes: Int = R.string.allow,
    @StringRes val cancelRes: Int = R.string.cancel
)