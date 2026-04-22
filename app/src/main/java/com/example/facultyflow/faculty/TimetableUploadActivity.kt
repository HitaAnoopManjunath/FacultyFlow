package com.example.facultyflow.faculty

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.facultyflow.R // <--- Add this line
import com.example.facultyflow.databinding.ActivityTimetableUploadBinding


class TimetableUploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimetableUploadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimetableUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.uploadArea.setOnClickListener {
            simulateFileUpload()
        }

        binding.cameraOption.setOnClickListener {
            simulateCameraCapture()
        }
    }

    private fun simulateFileUpload() {
        // Show progress
        binding.uploadArea.visibility = View.GONE
        binding.cameraOption.visibility = View.GONE
        binding.progressArea.visibility = View.VISIBLE
        binding.tvProgressText.text = getString(R.string.uploading)

        // Simulate upload progress
        binding.progressIndicator.progress = 0
        val progressUpdate = object : Runnable {
            override fun run() {
                val currentProgress = binding.progressIndicator.progress + 20
                binding.progressIndicator.progress = currentProgress

                if (currentProgress < 100) {
                    binding.progressIndicator.postDelayed(this, 500)
                } else {
                    // Upload complete, show scanning
                    binding.tvProgressText.text = getString(R.string.scanning)
                    binding.progressIndicator.postDelayed({
                        showSuccessState()
                    }, 2000)
                }
            }
        }
        binding.progressIndicator.postDelayed(progressUpdate, 500)
    }

    private fun simulateCameraCapture() {
        // In a real app, this would open the camera
        simulateFileUpload()
    }

    private fun showSuccessState() {
        binding.progressArea.visibility = View.GONE
        binding.successState.visibility = View.VISIBLE

        // Auto navigate back after 2 seconds
        binding.successState.postDelayed({
            finish()
        }, 2000)
    }
}
