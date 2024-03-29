package me.brandom.schoolmanager.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.HomeworkWithSubject
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.databinding.ItemHomeworkBinding

class HomeworkListAdapter(val homeworkManager: HomeworkManager) :
    ListAdapter<HomeworkWithSubject, HomeworkListAdapter.HomeworkListViewHolder>(differ) {
    companion object {
        val differ = object : DiffUtil.ItemCallback<HomeworkWithSubject>() {
            override fun areItemsTheSame(
                oldItem: HomeworkWithSubject,
                newItem: HomeworkWithSubject
            ): Boolean =
                oldItem.homework.hwId == newItem.homework.hwId

            override fun areContentsTheSame(
                oldItem: HomeworkWithSubject,
                newItem: HomeworkWithSubject
            ): Boolean =
                oldItem.homework == newItem.homework
        }
    }

    var tracker: SelectionTracker<Long>? = null

    inner class HomeworkListViewHolder(private val binding: ItemHomeworkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                itemHomeworkCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    val homework = getItem(adapterPosition).homework
                    homeworkManager.markAsCompleted(homework, isChecked)
                }

                root.setOnClickListener {
                    homeworkManager.onHomeworkClick(it, getItem(adapterPosition))
                }
            }
        }

        fun bind(homework: Homework, subject: Subject, isSelected: Boolean) {
            binding.apply {
                ViewCompat.setTransitionName(
                    root,
                    "homework_to_details_transition_${homework.hwId}"
                )
                itemHomeworkName.text = homework.hwName
                itemHomeworkDeadline.text = homework.formattedDateTime
                itemHomeworkSubject.text = subject.name
                itemHomeworkCheckBox.isChecked = homework.isComplete
                root.isSelected = isSelected
            }
        }

        fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition() = adapterPosition
            override fun getSelectionKey() = getItem(adapterPosition).homework.hwId.toLong()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkListViewHolder =
        HomeworkListViewHolder(
            ItemHomeworkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: HomeworkListViewHolder, position: Int) {
        val currentItem = getItem(position)
        tracker?.let {
            holder.bind(
                currentItem.homework,
                currentItem.subject,
                it.isSelected(currentItem.homework.hwId.toLong())
            )
        }
    }

    interface HomeworkManager {
        fun onHomeworkClick(rootView: View, hwWthSubject: HomeworkWithSubject)
        fun markAsCompleted(homework: Homework, checkBoxState: Boolean)
        fun deleteHomework(homework: Homework)
        fun editHomework(homework: Homework)
    }
}