package com.example.facultyflow.student

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.facultyflow.databinding.ActivityFacultyDirectoryBinding
import com.example.facultyflow.student.adapters.FacultyAdapter
import com.example.facultyflow.student.models.Faculty
import com.example.facultyflow.R
import com.google.firebase.firestore.FirebaseFirestore

class FacultyDirectoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFacultyDirectoryBinding
    private lateinit var facultyAdapter: FacultyAdapter
    private var allFacultyList = mutableListOf<Faculty>()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacultyDirectoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupUI()
        setupFacultyList()
        setupSearch()
        setupFilters()
        fetchFaculty()
    }

    private fun setupUI() {
        binding.chipAll.isChecked = true
        binding.chipAll.setChipBackgroundColorResource(R.color.ios_blue)
        binding.chipAll.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun setupFacultyList() {
        facultyAdapter = FacultyAdapter { faculty ->
            val intent = Intent(this, FacultyProfileActivity::class.java)
            intent.putExtra("faculty_id", faculty.id)
            intent.putExtra("faculty_name", faculty.name)
            intent.putExtra("faculty_designation", faculty.designation)
            intent.putExtra("faculty_dept", faculty.department)
            startActivity(intent)
        }

        binding.rvFaculty.apply {
            layoutManager = LinearLayoutManager(this@FacultyDirectoryActivity)
            adapter = facultyAdapter
        }
    }

    private fun fetchFaculty() {
        db.collection("users")
            .whereEqualTo("userType", "faculty")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allFacultyList.clear()
                value?.forEach { doc ->
                    val faculty = Faculty(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        designation = doc.getString("designation") ?: "Professor",
                        department = doc.getString("department") ?: "General",
                        availability = doc.getString("availability") ?: "green"
                    )
                    allFacultyList.add(faculty)
                }
                
                filterFaculty(binding.etSearch.text.toString())
            }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFaculty(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener {
            resetChips()
            binding.chipAll.isChecked = true
            binding.chipAll.setChipBackgroundColorResource(R.color.ios_blue)
            binding.chipAll.setTextColor(ContextCompat.getColor(this, R.color.white))
            filterFaculty(binding.etSearch.text.toString())
        }

        binding.chipCS.setOnClickListener { filterByDepartment("Computer Science", binding.chipCS) }
        binding.chipElectronics.setOnClickListener { filterByDepartment("Electronics", binding.chipElectronics) }
        binding.chipMechanical.setOnClickListener { filterByDepartment("Mechanical", binding.chipMechanical) }
        binding.chipCivil.setOnClickListener { filterByDepartment("Civil", binding.chipCivil) }
    }

    private fun resetChips() {
        listOf(binding.chipAll, binding.chipCS, binding.chipElectronics, binding.chipMechanical, binding.chipCivil).forEach { chip ->
            chip.isChecked = false
            chip.setChipBackgroundColorResource(R.color.ios_gray6)
            chip.setTextColor(ContextCompat.getColor(this, R.color.apple_secondary_label))
        }
    }

    private fun filterByDepartment(department: String, selectedChip: com.google.android.material.chip.Chip) {
        resetChips()
        selectedChip.isChecked = true
        selectedChip.setChipBackgroundColorResource(R.color.ios_blue)
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.white))
        
        val filteredList = allFacultyList.filter { it.department == department }
        updateRecyclerView(filteredList)
    }

    private fun filterFaculty(searchQuery: String) {
        val filteredList = if (searchQuery.isEmpty()) {
            allFacultyList.toList()
        } else {
            allFacultyList.filter { faculty ->
                faculty.name.contains(searchQuery, ignoreCase = true) ||
                faculty.department.contains(searchQuery, ignoreCase = true)
            }
        }
        updateRecyclerView(filteredList)
    }

    private fun updateRecyclerView(list: List<Faculty>) {
        facultyAdapter.submitList(list)
        if (list.isEmpty()) {
            binding.rvFaculty.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.rvFaculty.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }
}
