package com.quickcleanpro.phonecleaner.presentation.common.state

/**
 * 閫氱敤閫夋嫨鐘舵€併€?
 *
 * 閫氳繃涓嶅彲鍙?Set 淇濆瓨宸查€?id锛屽苟鎻愪緵 toggle銆佸叏閫夈€佸彇娑堝拰娓呯┖绛夊父瑙佹搷浣滐紝
 * 鍚庣画 FileManager銆丏uplicate Files 绛夐〉闈㈠彲浠ュ鐢ㄥ悓涓€濂楅€夋嫨璇箟銆?
 */
data class SelectionState<Id>(
    val selectedIds: Set<Id> = emptySet()
) {
    /** 褰撳墠宸查€夋暟閲忋€?*/
    val selectedCount: Int get() = selectedIds.size

    /** 鍒ゆ柇鎸囧畾 id 鏄惁宸茶閫変腑銆?*/
    fun contains(id: Id): Boolean = id in selectedIds

    /** 鍒ゆ柇涓€缁?id 鏄惁鍏ㄩ儴宸茶閫変腑銆?*/
    fun containsAll(ids: Set<Id>): Boolean = ids.isNotEmpty() && selectedIds.containsAll(ids)

    /** 鍒囨崲鍗曚釜 id 鐨勯€変腑鐘舵€併€?*/
    fun toggle(id: Id): SelectionState<Id> =
        copy(selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id)

    /** 娣诲姞涓€缁?id 鍒板凡閫夐泦鍚堛€?*/
    fun select(ids: Iterable<Id>): SelectionState<Id> =
        copy(selectedIds = selectedIds + ids)

    /** 浠庡凡閫夐泦鍚堢Щ闄や竴缁?id銆?*/
    fun unselect(ids: Iterable<Id>): SelectionState<Id> =
        copy(selectedIds = selectedIds - ids.toSet())

    /** 鐢ㄦ柊闆嗗悎鏇挎崲褰撳墠閫夋嫨銆?*/
    fun replace(ids: Iterable<Id>): SelectionState<Id> =
        copy(selectedIds = ids.toSet())

    /** 娓呯┖鎵€鏈夐€夋嫨銆?*/
    fun clear(): SelectionState<Id> = copy(selectedIds = emptySet())
}
