package com.quickcleanpro.phonecleaner.domain.model


/**
 * 鎸夌被鍒垎缁勭殑娓呯悊椤归泦鍚? */
data class CategoryCleanGroup(
    val category: JunkCategory,
    val items: List<CleanItem>
) {
    val totalSize: Long
        get() = items.sumOf { it.fileSize }

    val checkedSize: Long
        get() = items.filter { it.isChecked }.sumOf { it.fileSize }

    val checkedCount: Int
        get() = items.count { it.isChecked }

    val totalCount: Int
        get() = items.size

    val formattedTotalSize: String
        get() = JunkFile.formatFileSize(totalSize)

    val formattedCheckedSize: String
        get() = JunkFile.formatFileSize(checkedSize)
}
