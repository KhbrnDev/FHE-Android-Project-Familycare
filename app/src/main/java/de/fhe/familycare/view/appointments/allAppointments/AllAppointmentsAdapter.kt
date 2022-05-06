package de.fhe.familycare.view.appointments.allAppointments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.fhe.familycare.R
import de.fhe.familycare.databinding.RvitemAllAppointmentsBinding
import de.fhe.familycare.storage.model.Appointment

/**
 * Adapter for AllAppointments RecyclerView
 */
class AllAppointmentsAdapter(private val onAppointmentClicked: (Appointment) -> Unit) :
    ListAdapter<Appointment, AllAppointmentsAdapter.AllAppointmentsViewHolder>(DiffCallback) {

    class AllAppointmentsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        val binding = RvitemAllAppointmentsBinding.bind(view)

        /**
         * Binds Appointment Object to View and displays its data
         */
        fun bind(appointment: Appointment) {
            binding.tvTitle.text = appointment.title
            val splits = appointment.formatStartDate().split(" ", ignoreCase = true)
            binding.tvDate.text =
                view.resources.getString(R.string.appointment_clock_rvitem, splits[0], splits[1])
            binding.tvFamilyMember.text =
                appointment.familyMemberName ?: "Kein Familien Miglied"
        }
    }

    /**
     * inflates Layout
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAppointmentsViewHolder {

        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.rvitem_all_appointments, parent, false)

        return AllAppointmentsViewHolder(layout)
    }

    /**
     * Sets onClickListener to ListItems
     */
    override fun onBindViewHolder(holder: AllAppointmentsViewHolder, position: Int) {

        val current = getItem(position)

        holder.itemView.setOnClickListener {
            onAppointmentClicked(current)
        }
        holder.bind(current)
    }

    /**
     * Checks if item is already in List
     */
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}