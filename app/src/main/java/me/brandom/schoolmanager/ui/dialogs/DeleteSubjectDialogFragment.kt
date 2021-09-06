package me.brandom.schoolmanager.ui.dialogs

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import me.brandom.schoolmanager.R

@AndroidEntryPoint
class DeleteSubjectDialogFragment : DialogFragment() {
    private val viewModel: DialogActionsViewModel by viewModels()
    private val args: DeleteSubjectDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(requireContext())
        .setTitle(R.string.confirm_subject_deletion_title)
        .setMessage(R.string.confirm_subject_deletion_message)
        .setPositiveButton(R.string.confirm_subject_deletion_negative, null)
        .setNegativeButton(R.string.confirm_subject_deletion_positive) { _, _ ->
            viewModel.onConfirmDeleteClick(args.subjectId)
        }
        .create()
}