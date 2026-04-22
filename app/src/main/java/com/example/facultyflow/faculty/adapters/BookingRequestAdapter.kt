package com.example.facultyflow.faculty.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.facultyflow.databinding.ItemBookingRequestBinding
import com.example.facultyflow.faculty.models.BookingRequest

class BookingRequestAdapter(
    private val onAccept: (BookingRequest) -> Unit,
    private val onDecline: (BookingRequest) -> Unit
) : ListAdapter<BookingRequest, BookingRequestAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(private val binding: ItemBookingRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private var isExpanded = false
        
        fun bind(bookingRequest: BookingRequest) {
            binding.tvStudentName.text = bookingRequest.studentName
            binding.tvRequestedTime.text = bookingRequest.requestedTime
            binding.tvNotePreview.text = bookingRequest.note.take(50) + if (bookingRequest.note.length > 50) "..." else ""
            binding.tvFullNote.text = bookingRequest.note

            // Set expand/collapse functionality
            binding.ivExpand.setOnClickListener {
                isExpanded = !isExpanded
                toggleExpandedState()
            }

            binding.root.setOnClickListener {
                isExpanded = !isExpanded
                toggleExpandedState()
            }

            binding.btnAccept.setOnClickListener {
                onAccept(bookingRequest)
            }

            binding.btnDecline.setOnClickListener {
                onDecline(bookingRequest)
            }
        }

        private fun toggleExpandedState() {
            if (isExpanded) {
                binding.expandedSection.visibility = View.VISIBLE
                binding.ivExpand.rotation = 180f
            } else {
                binding.expandedSection.visibility = View.GONE
                binding.ivExpand.rotation = 0f
            }
        }
    }
}

class BookingDiffCallback : DiffUtil.ItemCallback<BookingRequest>() {
    override fun areItemsTheSame(oldItem: BookingRequest, newItem: BookingRequest): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BookingRequest, newItem: BookingRequest): Boolean {
        return oldItem == newItem
    }
}
