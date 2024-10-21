package com.example.runningproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningproject.databinding.ActivityCommunityBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class CommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding
        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Back Button
        binding.backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Tutup CommunityActivity setelah pindah ke MainActivity
        }

        // Setup tombol tambah komunitas
        binding.addCommunity.setOnClickListener {
            val intent = Intent(this, AddCommunityActivity::class.java)
            startActivity(intent)
        }

        // Setup RecyclerView dengan data dari Firestore
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        firestore.collection("communities").get()
            .addOnSuccessListener { result ->
                val communityList = mutableListOf<String>()
                val communityIds = mutableListOf<String>()

                for (document in result) {
                    val community = document.getString("name")
                    community?.let {
                        communityList.add(it)
                        communityIds.add(document.id) // Simpan ID komunitas
                    }
                }

                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = CommunityAdapter(communityList, communityIds) { communityId ->
                    val intent = Intent(this, CommunityDetailActivity::class.java)
                    intent.putExtra("communityId", communityId)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                // Tangani error jika ada
            }
    }


    class CommunityAdapter(private val communityList: List<String>, private val communityIds: List<String>, private val onItemClick: (String) -> Unit) :
        RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

        class CommunityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val communityName: TextView = view.findViewById(R.id.community_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_community, parent, false)
            return CommunityViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
            holder.communityName.text = communityList[position]
            holder.itemView.setOnClickListener {
                onItemClick(communityIds[position])
            }
        }

        override fun getItemCount() = communityList.size
    }

}
