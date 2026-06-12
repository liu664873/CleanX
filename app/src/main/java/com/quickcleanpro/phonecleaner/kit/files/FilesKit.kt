package com.quickcleanpro.phonecleaner.kit.files

import android.content.Context
import com.quickcleanpro.phonecleaner.data.repository.FileRepositoryImpl
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository

object FilesKit {
    fun create(context: Context): FileRepository =
        FileRepositoryImpl(context.applicationContext)
}
