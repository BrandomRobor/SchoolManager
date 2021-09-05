package me.brandom.schoolmanager.ui.home

import androidx.recyclerview.selection.ItemKeyProvider

class HomeworkKeyProvider(private val adapter: HomeworkListAdapter) : ItemKeyProvider<Long>(
    SCOPE_CACHED
) {
    override fun getKey(position: Int) = adapter.currentList[position].homework.hwId.toLong()
    override fun getPosition(key: Long) =
        adapter.currentList.indexOfFirst { it.homework.hwId.toLong() == key }
}