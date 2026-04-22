package com.example.facultyflow

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.facultyflow.databinding.ActivityMainBinding
import com.example.facultyflow.faculty.FacultyHomeActivity
import com.example.facultyflow.student.FacultyDirectoryActivity
import com.example.facultyflow.utils.Constants
import com.example.facultyflow.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        val preferencesManager = PreferencesManager(this)

        // Fade in animation
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        binding.mainContent.startAnimation(fadeIn)

        binding.root.postDelayed({
            binding.mainContent.startAnimation(fadeOut)
            
            binding.root.postDelayed({
                val currentUser = auth.currentUser
                
                // CRITICAL FIX: Verify Firebase user exists and preferences match
                if (currentUser != null && preferencesManager.isLoggedIn) {
                    // Check email verification status on app launch
                    if (currentUser.isEmailVerified) {
                        val intent = if (preferencesManager.userType == Constants.USER_TYPE_STUDENT) {
                            Intent(this, FacultyDirectoryActivity::class.java)
                        } else {
                            Intent(this, FacultyHomeActivity::class.java)
                        }
                        startActivity(intent)
                    } else {
                        // Not verified, force login
                        auth.signOut()
                        preferencesManager.clearAll()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                } else {
                    // No user or local session cleared
                    auth.signOut()
                    preferencesManager.clearAll()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }, 1000)
        }, 2000)
    }
}
