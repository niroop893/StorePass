package com.example.storepass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.storepass.data.Credential

class CredentialAdapter(
    private var credentials: List<Credential>,
    private val onItemClick: (Credential) -> Unit
) : RecyclerView.Adapter<CredentialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serviceText: TextView = view.findViewById(R.id.serviceNameText)
        val usernameText: TextView = view.findViewById(R.id.usernameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_credential, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val credential = credentials[position]
        holder.serviceText.text = credential.service
        holder.usernameText.text = credential.username
        holder.itemView.setOnClickListener { onItemClick(credential) }
    }

    override fun getItemCount() = credentials.size

    fun updateCredentials(newCredentials: List<Credential>) {
        credentials = newCredentials
        notifyDataSetChanged()
    }
} 