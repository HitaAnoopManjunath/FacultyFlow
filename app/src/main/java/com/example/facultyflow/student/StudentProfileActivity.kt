package com.example.facultyflow.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.facultyflow.LoginActivity
import com.example.facultyflow.databinding.ActivityStudentProfileBinding
import com.example.facultyflow.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        preferencesManager = PreferencesManager(this)

        setupUI()
    }

    private fun setupUI() {
        binding.tvName.text = preferencesManager.userName
        binding.tvEmail.text = preferencesManager.userEmail

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        auth.signOut()
        preferencesManager.clearAll()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
