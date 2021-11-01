package me.brandom.schoolmanager.utils

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class CustomBotNavViewBehavior(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<BottomNavigationView>(context, attrs) {
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: BottomNavigationView,
        dependency: View
    ): Boolean {
        if (dependency is Snackbar.SnackbarLayout && dependency.layoutParams is CoordinatorLayout.LayoutParams) {
            val newParams = dependency.layoutParams as CoordinatorLayout.LayoutParams
            newParams.anchorId = child.id
            newParams.anchorGravity = Gravity.TOP
            newParams.gravity = Gravity.TOP
            dependency.layoutParams = newParams
            dependency.translationY = (-8 * Resources.getSystem().displayMetrics.density)
        }

        return super.layoutDependsOn(parent, child, dependency)
    }
}