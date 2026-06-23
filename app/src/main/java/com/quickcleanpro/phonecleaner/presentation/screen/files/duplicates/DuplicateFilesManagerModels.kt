package com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates

import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileOperationPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.formatManagedFileDate

internal data class DuplicateFileEntry(
    val id: Int,
    val path: String,
    val date: String,
    val sizeLabel: String,
    val selected: Boolean,
    val note: String? = null,
    val realFile: ManagedFileItem? = null
)

internal data class DuplicateGroupItem(
    val id: Int,
    val name: String,
    val sizeLabel: String,
    val duplicateCount: Int,
    val files: List<DuplicateFileEntry>
)

internal fun duplicateFileKey(file: DuplicateFileEntry): String =
    file.realFile?.uri?.toString() ?: "${file.path}#${file.date}#${file.sizeLabel}"

internal fun mapDuplicateGroups(groups: List<List<ManagedFileItem>>): List<DuplicateGroupItem> =
    groups.filter { it.isNotEmpty() }
        .mapIndexed { groupIndex, files ->
            val first = files.first()
            DuplicateGroupItem(
                id = groupIndex + 1,
                name = first.name,
                sizeLabel = first.formattedSize,
                duplicateCount = files.size,
                files = files.mapIndexed { index, file ->
                    DuplicateFileEntry(
                        id = index + 1,
                        path = file.path ?: file.name,
                        date = formatManagedFileDate(file.modifiedSeconds),
                        sizeLabel = file.formattedSize,
                        selected = index > 0,
                        note = if (index == 0) "Removal not recommended" else null,
                        realFile = file
                    )
                }
            )
        }
