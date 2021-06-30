package me.brandom.schoolmanager.ui.subject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.brandom.schoolmanager.R
import me.brandom.schoolmanager.database.entities.Subject
import me.brandom.schoolmanager.databinding.ItemSubjectBinding

class SubjectListAdapter(private val subjectManager: SubjectManager) :
    ListAdapter<Subject, SubjectListAdapter.SubjectListViewHolder>(differ) {
    companion object {
        val differ = object : DiffUtil.ItemCallback<Subject>() {
            override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean =
                oldItem == newItem
        }
    }

    inner class SubjectListViewHolder(private val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                val menu = PopupMenu(root.context, itemSubjectMenu)
                menu.inflate(R.menu.homework_menu)

                itemSubjectMenu.setOnClickListener {
                    menu.show()
                }

                menu.setOnMenuItemClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val subject = getItem(adapterPosition)
                        when (it.itemId) {
                            R.id.homework_edit_item -> {
                                subjectManager.editSubject(subject)
                                true
                            }
                            R.id.homework_delete_item -> {
                                subjectManager.deleteSubject(subject)
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
            }
        }

        fun bind(subject: Subject) {
            binding.apply {
                itemSubjectName.text = subject.name

                if (subject.location.isNullOrEmpty()) {
                    itemSubjectLocation.isVisible = false
                } else {
                    itemSubjectLocation.isVisible = true
                    itemSubjectLocation.text = subject.location
                }

                if (subject.teacherName.isNullOrEmpty()) {
                    itemSubjectTeacher.isVisible = false
                } else {
                    itemSubjectTeacher.isVisible = true
                    itemSubjectTeacher.text = subject.teacherName
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectListViewHolder =
        SubjectListViewHolder(
            ItemSubjectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: SubjectListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface SubjectManager {
        fun deleteSubject(subject: Subject)
        fun editSubject(subject: Subject)
    }
}