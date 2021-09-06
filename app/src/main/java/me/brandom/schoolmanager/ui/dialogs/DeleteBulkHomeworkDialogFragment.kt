package me.brandom.schoolmanager.ui.dialogs

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import me.brandom.schoolmanager.R

@AndroidEntryPoint
class DeleteBulkHomeworkDialogFragment : DialogFragment() {
    private val viewModel: DialogActionsViewModel by viewModels()
    private val navArgs: DeleteBulkHomeworkDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.bulk_homework_deletion_title)
            .setMessage(R.string.bulk_homework_deletion_message)
            .setPositiveButton(R.string.bulk_homework_deletion_cancel, null)
            .setNegativeButton(R.string.bulk_homework_deletion_confirm) { _, _ ->
                viewModel.onConfirmDeleteBulk(navArgs.idArray)
            }
            .create()
}