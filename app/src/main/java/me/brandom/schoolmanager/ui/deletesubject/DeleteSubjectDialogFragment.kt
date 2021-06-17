package me.brandom.schoolmanager.ui.deletesubject

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteSubjectDialogFragment : DialogFragment() {
    private val viewModel: DeleteSubjectViewModel by viewModels()
    private val args: DeleteSubjectDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(requireContext())
        .setTitle("Confirm deletion")
        .setMessage("Deleting a subject will delete all homework of the subject. Do you want to continue?")
        .setPositiveButton("No", null)
        .setNegativeButton("Yes") { _, _ ->
            viewModel.onConfirmDeleteClick(args.subjectId)
        }
        .create()
}