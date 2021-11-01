package me.brandom.schoolmanager.utils

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class CustomFABBehavior(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean = dependency is Snackbar.SnackbarLayout

    override fun onDependentViewRemoved(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ) {
        child.translationY = (-16 * Resources.getSystem().displayMetrics.density)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            val oldTranslation = child.translationY
            val newTranslation =
                dependency.translationY - dependency.height - (16 * Resources.getSystem().displayMetrics.density)
            child.translationY = newTranslation
            return oldTranslation != newTranslation
        }

        return false
    }
}