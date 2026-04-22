package com.example.facultyflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facultyflow.databinding.ActivitySignupBinding
import com.example.facultyflow.faculty.FacultyHomeActivity
import com.example.facultyflow.student.FacultyDirectoryActivity
import com.example.facultyflow.utils.Constants
import com.example.facultyflow.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isStudent = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize state
        updateUserTypeUI()
        
        setupUserTypeSelection()
        setupFormFields()
        setupSignupButton()
    }

    private fun setupUserTypeSelection() {
        binding.btnStudent.setOnClickListener {
            if (!isStudent) {
                isStudent = true
                updateUserTypeUI()
            }
        }

        binding.btnFaculty.setOnClickListener {
            if (isStudent) {
                isStudent = false
                updateUserTypeUI()
            }
        }
    }

    private fun updateUserTypeUI() {
        if (isStudent) {
            binding.btnStudent.apply {
                setBackgroundResource(R.drawable.segmented_control_selected)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            binding.btnFaculty.apply {
                background = null
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            binding.studentFieldsContainer.visibility = View.VISIBLE
        } else {
            binding.btnFaculty.apply {
                setBackgroundResource(R.drawable.segmented_control_selected)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            binding.btnStudent.apply {
                background = null
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            binding.studentFieldsContainer.visibility = View.GONE
        }
    }

    private fun setupFormFields() {
        val degrees = arrayOf("B.Tech Computer Science", "B.Tech Electronics", "B.Tech Mechanical", "B.Tech Civil", "M.Tech Computer Science", "M.Tech Electronics")
        val degreeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, degrees)
        binding.etDegree.setAdapter(degreeAdapter)

        val semesters = arrayOf("1st Semester", "2nd Semester", "3rd Semester", "4th Semester", "5th Semester", "6th Semester", "7th Semester", "8th Semester")
        val semesterAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, semesters)
        binding.etSemester.setAdapter(semesterAdapter)
    }

    private fun setupSignupButton() {
        binding.btnSignup.setOnClickListener {
            if (validateForm()) {
                performFirebaseSignup()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            binding.tilEmail.error = "Please use your @rvu.edu.in email"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (isStudent) {
            val degree = binding.etDegree.text.toString().trim()
            val semester = binding.etSemester.text.toString().trim()

            if (degree.isEmpty()) {
                binding.tilDegree.error = "Degree is required"
                isValid = false
            } else {
                binding.tilDegree.error = null
            }

            if (semester.isEmpty()) {
                binding.tilSemester.error = "Semester is required"
                isValid = false
            } else {
                binding.tilSemester.error = null
            }
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return email.endsWith("@rvu.edu.in") && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun performFirebaseSignup() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val userType = if (isStudent) Constants.USER_TYPE_STUDENT else Constants.USER_TYPE_FACULTY

        binding.btnSignup.isEnabled = false
        binding.btnSignup.text = "Creating account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val userData = mutableMapOf<String, Any>(
                        "uid" to userId,
                        "name" to name,
                        "email" to email,
                        "userType" to userType
                    )
                    
                    if (isStudent) {
                        userData["degree"] = binding.etDegree.text.toString()
                        userData["semester"] = binding.etSemester.text.toString()
                    }

                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            // Save to local prefs too
                            preferencesManager.userName = name
                            preferencesManager.userEmail = email
                            preferencesManager.userType = userType
                            preferencesManager.isLoggedIn = true

                            navigateToDashboard()
                        }
                        .addOnFailureListener { e ->
                            binding.btnSignup.isEnabled = true
                            binding.btnSignup.text = "Create Account"
                            Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    binding.btnSignup.isEnabled = true
                    binding.btnSignup.text = "Create Account"
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToDashboard() {
        if (isStudent) {
            startActivity(Intent(this, FacultyDirectoryActivity::class.java))
        } else {
            startActivity(Intent(this, FacultyHomeActivity::class.java))
        }
        finish()
    }
}
