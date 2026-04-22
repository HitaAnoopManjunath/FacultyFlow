package com.example.facultyflow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facultyflow.databinding.ActivityLoginBinding
import com.example.facultyflow.faculty.FacultyHomeActivity
import com.example.facultyflow.student.FacultyDirectoryActivity
import com.example.facultyflow.utils.Constants
import com.example.facultyflow.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.tilEmail.error = "Email is required"
            } else if (!email.endsWith(Constants.EMAIL_DOMAIN)) {
                binding.tilEmail.error = "Please use your @rvu.edu.in email"
            } else if (password.isEmpty()) {
                binding.tilPassword.error = "Password is required"
            } else {
                binding.tilEmail.error = null
                binding.tilPassword.error = null
                performFirebaseLogin(email, password)
            }
        }

        binding.tvSignupToggle.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun performFirebaseLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Signing in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    
                    // Fetch user details from Firestore
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val name = document.getString("name") ?: ""
                                val userType = document.getString("userType") ?: ""
                                
                                preferencesManager.userName = name
                                preferencesManager.userEmail = email
                                preferencesManager.userType = userType
                                preferencesManager.isLoggedIn = true
                                
                                navigateToDashboard()
                            } else {
                                binding.btnLogin.isEnabled = true
                                binding.btnLogin.text = "Sign In"
                                Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            binding.btnLogin.isEnabled = true
                            binding.btnLogin.text = "Sign In"
                            Toast.makeText(this, "Error fetching profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Sign In"
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToDashboard() {
        val intent = if (preferencesManager.userType == Constants.USER_TYPE_STUDENT) {
            Intent(this, FacultyDirectoryActivity::class.java)
        } else {
            Intent(this, FacultyHomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
