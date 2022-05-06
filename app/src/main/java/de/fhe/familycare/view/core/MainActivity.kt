package de.fhe.familycare.view.core

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.fhe.familycare.R
import de.fhe.familycare.databinding.ActivityMainBinding

/**
 * This is the starting point for the application. Everything is controlled from here. 
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // find ViewBinding and inflate layout
        val binding =ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i("FM", resources.getString(R.string.app_name))

        // setting NavController and names in TopAppBar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.miAllFamilyMembers, R.id.miAllAppointments, R.id.miAllContacts))
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)

    }


    fun setActionBarTitle(title: String){
        supportActionBar?.title = title
    }

    // activates back button in top left corner if available
    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp() || navController.navigateUp()
    }
}