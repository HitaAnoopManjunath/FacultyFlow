package com.example.facultyflow.faculty.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.facultyflow.R
import com.example.facultyflow.databinding.ItemScheduleBinding
import com.example.facultyflow.faculty.models.ScheduleItem

class ScheduleAdapter : ListAdapter<ScheduleItem, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(scheduleItem: ScheduleItem) {
            binding.tvStartTime.text = scheduleItem.startTime
            binding.tvEndTime.text = scheduleItem.endTime
            binding.tvClassName.text = scheduleItem.className
            binding.tvClassRoom.text = scheduleItem.classRoom
            binding.tvStatus.text = scheduleItem.status

            // Set status indicator color
            val statusColorRes = when (scheduleItem.statusColor) {
                "green" -> R.drawable.availability_green
                "amber" -> R.drawable.availability_amber
                "grey" -> R.drawable.availability_grey
                else -> R.drawable.availability_green
            }
            binding.statusIndicator.setBackgroundResource(statusColorRes)

            // Set status text color
            val statusTextColor = when (scheduleItem.statusColor) {
                "green" -> binding.root.context.getColor(R.color.green_500)
                "amber" -> binding.root.context.getColor(R.color.amber_500)
                "grey" -> binding.root.context.getColor(R.color.grey_500)
                else -> binding.root.context.getColor(R.color.green_500)
            }
            binding.tvStatus.setTextColor(statusTextColor)
        }
    }
}

class ScheduleDiffCallback : DiffUtil.ItemCallback<ScheduleItem>() {
    override fun areItemsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean {
        return oldItem.startTime == newItem.startTime
    }

    override fun areContentsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean {
        return oldItem == newItem
    }
}
