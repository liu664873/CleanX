package com.quickcleanpro.phonecleaner.domain.model


/**
 * 娓呯悊椤归鍩熸ā鍨? */
data class CleanItem(
    val junkFile: JunkFile,
    var isChecked: Boolean = true
) {
    val category: JunkCategory get() = junkFile.category
    val fileSize: Long get() = junkFile.fileSize
    val formattedSize: String get() = junkFile.formattedSize
    val fileName: String get() = junkFile.fileName
}
