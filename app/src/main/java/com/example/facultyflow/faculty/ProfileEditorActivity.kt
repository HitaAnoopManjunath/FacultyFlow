package com.example.facultyflow.faculty

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facultyflow.R
import com.example.facultyflow.databinding.ActivityProfileEditorBinding
import com.example.facultyflow.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditorBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        preferencesManager = PreferencesManager(this)

        setupUI()
        setupFormFields()
        setupClickListeners()
        loadProfileData()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupFormFields() {
        val departments = arrayOf("Computer Science", "Electronics", "Mechanical", "Civil", "Electrical")
        val departmentAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departments)
        binding.etDepartment.setAdapter(departmentAdapter)
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.btnSaveSticky.setOnClickListener { saveProfile() }
    }

    private fun loadProfileData() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.etName.setText(doc.getString("name"))
                    binding.etDepartment.setText(doc.getString("department"), false)
                    binding.etRoomBlock.setText(doc.getString("roomBlock"))
                    binding.etOfficeHours.setText(doc.getString("officeHours"))
                    binding.etDesignation.setText(doc.getString("designation"))
                }
            }
    }

    private fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return
        val name = binding.etName.text.toString().trim()
        val department = binding.etDepartment.text.toString().trim()
        val roomBlock = binding.etRoomBlock.text.toString().trim()
        val officeHours = binding.etOfficeHours.text.toString().trim()
        val designation = binding.etDesignation.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return
        }

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."

        val updates = hashMapOf(
            "name" to name,
            "department" to department,
            "roomBlock" to roomBlock,
            "officeHours" to officeHours,
            "designation" to designation
        )

        db.collection("users").document(userId)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                preferencesManager.userName = name
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Changes"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
