package me.brandom.schoolmanager.utils

enum class SortOrder {
    BY_NAME,
    BY_DEADLINE
}

enum class HomeworkOptions(val query: String) {
    TODAY("WHERE datetime(homework.deadline / 1000, 'unixepoch', 'localtime') BETWEEN date('now') AND date('now', '+1 day')"),
    TOMORROW(""),
    LATE(""),
    ALL("")
}