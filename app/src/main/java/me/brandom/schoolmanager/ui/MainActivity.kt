package me.brandom.schoolmanager.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import me.brandom.schoolmanager.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        const val FORM_CREATE_OK_FLAG = Activity.RESULT_FIRST_USER
        const val FORM_EDIT_OK_FLAG = Activity.RESULT_FIRST_USER + 1
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var manager: InputMethodManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.apply {
            setContentView(root)
            setSupportActionBar(activityToolbar)

            val navController =
                (supportFragmentManager.findFragmentById(activityFragmentView.id) as NavHostFragment).navController
            activityNavView.setupWithNavController(navController)
            appBarConfiguration = AppBarConfiguration(
                activityNavView.menu,
                activityDrawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(binding.activityFragmentView.id)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}