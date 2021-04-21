package me.brandom.schoolmanager.ui

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.apply {
            setContentView(root)
            setSupportActionBar(activityToolbar)

            val navController =
                (supportFragmentManager.findFragmentById(activityFragmentView.id) as NavHostFragment).navController
            appBarConfiguration = AppBarConfiguration(navController.graph, activityDrawerLayout)
            acitvityNavView.setupWithNavController(navController)
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(binding.activityFragmentView.id)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}