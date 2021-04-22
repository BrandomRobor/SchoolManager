package me.brandom.schoolmanager.ui

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
            acitvityNavView.setupWithNavController(navController)
            appBarConfiguration = AppBarConfiguration(
                acitvityNavView.menu,
                activityDrawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(binding.activityFragmentView.id)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun closeKeyboard() {
        manager?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}