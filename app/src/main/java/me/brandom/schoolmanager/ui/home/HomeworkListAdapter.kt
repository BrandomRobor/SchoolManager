package me.brandom.schoolmanager.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.database.entities.SubjectWithHomeworks
import me.brandom.schoolmanager.databinding.ItemHomeworkBinding

class HomeworkListAdapter :
    ListAdapter<SubjectWithHomeworks, HomeworkListAdapter.HomeworkListViewHolder>(differ) {
    companion object {
        val differ = object : DiffUtil.ItemCallback<SubjectWithHomeworks>() {
            override fun areItemsTheSame(
                oldItem: SubjectWithHomeworks,
                newItem: SubjectWithHomeworks
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: SubjectWithHomeworks,
                newItem: SubjectWithHomeworks
            ): Boolean =
                oldItem.homework == newItem.homework
        }
    }

    class HomeworkListViewHolder(private val binding: ItemHomeworkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(homework: Homework, subject: Subject) {
            binding.apply {
                itemHomeworkName.text = homework.name

                itemHomeworkDescription.isVisible = homework.description.isNullOrEmpty()
                itemHomeworkDescription.text = homework.description

                itemHomeworkDeadline.text = homework.deadline.toString()

                itemHomeworkSubject.text = subject.name
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
        currentItem.homework.forEach {
            holder.bind(it, currentItem.subject)
        }
    }
}