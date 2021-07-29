package me.brandom.schoolmanager.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.brandom.schoolmanager.R
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

    inner class HomeworkListViewHolder(private val binding: ItemHomeworkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                val menu = PopupMenu(root.context, itemHomeworkMenu)
                menu.inflate(R.menu.homework_menu)

                itemHomeworkMenu.setOnClickListener {
                    menu.show()
                }

                menu.setOnMenuItemClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val homework = getItem(adapterPosition).homework
                        when (it.itemId) {
                            R.id.homework_edit_item -> {
                                homeworkManager.editHomework(homework)
                                true
                            }
                            R.id.homework_delete_item -> {
                                homeworkManager.deleteHomework(homework)
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }

                itemHomeworkCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    val homework = getItem(adapterPosition).homework
                    homeworkManager.markAsCompleted(homework, isChecked)
                }
            }
        }

        fun bind(homework: Homework, subject: Subject) {
            binding.apply {
                itemHomeworkName.text = homework.hwName

                itemHomeworkDescription.isVisible = homework.description != null
                itemHomeworkDescription.text = homework.description

                itemHomeworkDeadline.text = homework.formattedDateTime

                itemHomeworkSubject.text = subject.name

                itemHomeworkCheckBox.isChecked = homework.isComplete
            }
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
        holder.bind(currentItem.homework, currentItem.subject)
    }

    interface HomeworkManager {
        fun markAsCompleted(homework: Homework, checkBoxState: Boolean)
        fun deleteHomework(homework: Homework)
        fun editHomework(homework: Homework)
    }
}