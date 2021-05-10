package me.brandom.schoolmanager.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import java.text.DateFormat
import java.text.SimpleDateFormat

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

        val formatter: DateFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT)
    }

    inner class HomeworkListViewHolder(private val binding: ItemHomeworkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(homework: Homework, subject: Subject) {
            binding.apply {
                itemHomeworkName.text = homework.hwName

                itemHomeworkDescription.isVisible = homework.description != null
                itemHomeworkDescription.text = homework.description

                itemHomeworkDeadline.text = formatter.format(homework.deadline)

                itemHomeworkSubject.text = subject.name

                val menu = PopupMenu(root.context, itemHomeworkMenu)
                menu.inflate(R.menu.homework_menu)

                itemHomeworkMenu.setOnClickListener {
                    menu.show()
                }

                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.homework_edit_item -> {
                            true
                        }
                        R.id.homework_delete_item -> {
                            AlertDialog.Builder(root.context)
                                .setMessage(R.string.dialog_delete_message)
                                .setPositiveButton(R.string.dialog_cancel_button) { _, _ -> }
                                .setNegativeButton(R.string.fragment_home_delete_item) { _, _ ->
                                    homeworkManager.deleteHomework(homework)
                                    Toast.makeText(
                                        root.context,
                                        R.string.fragment_home_delete_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .create().show()
                            true
                        }
                        else -> false
                    }
                }
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
        fun deleteHomework(homework: Homework)
    }
}