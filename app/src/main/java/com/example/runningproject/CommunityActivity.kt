package com.example.runningproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningproject.databinding.ActivityCommunityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // FirebaseAuth untuk mendapatkan currentUser
    private val db = Firebase.firestore // Inisialisasi Firestore

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

        // Ambil data pengguna dan tampilkan
        loadUserData()

        // Setup RecyclerView dengan data dari Firestore
        setupRecyclerView()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Ambil data dari Firestore
                        val username = document.getString("username") ?: "User"
                        val email = document.getString("email") ?: "No email"

                        // Set data ke UI
                        binding.userName.text = username
                        binding.userLevel.text = "Beginner" // Contoh level pengguna
                    } else {
                        binding.userName.text = "No user found"
                    }
                }
                .addOnFailureListener { e ->
                    binding.userName.text = "Failed to load user data"
                }
        } else {
            binding.userName.text = "User not logged in"
        }
    }



    private fun setupRecyclerView() {
        firestore.collection("communities").get()
            .addOnSuccessListener { result ->
                val communityList = mutableListOf<String>()
                val communityIds = mutableListOf<String>()
                val communityImages = mutableListOf<String>() // Daftar untuk URL gambar

                for (document in result) {
                    val community = document.getString("name")
                    val imageUrl = document.getString("logoUrl") // Ambil URL gambar komunitas

                    community?.let {
                        communityList.add(it)
                        communityIds.add(document.id)
                        communityImages.add(imageUrl ?: "") // Tambahkan URL gambar atau string kosong jika tidak ada
                    }
                }

                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = CommunityAdapter(communityList, communityIds, communityImages) { communityId ->
                    val intent = Intent(this, CommunityDetailActivity::class.java)
                    intent.putExtra("communityId", communityId)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                // Tangani error jika ada
            }
    }


    class CommunityAdapter(
        private val communityList: List<String>,
        private val communityIds: List<String>,
        private val communityImages: List<String>,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

        class CommunityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val communityName: TextView = view.findViewById(R.id.community_name)
            val communityImage: ImageView = view.findViewById(R.id.community_image) // ImageView untuk foto komunitas
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_community, parent, false)
            return CommunityViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
            holder.communityName.text = communityList[position]

            // Gunakan Glide untuk memuat gambar dari URL
            Glide.with(holder.itemView.context)
                .load(communityImages[position]) // URL gambar komunitas
//                .placeholder(R.drawable.ic_placeholder) // Gambar placeholder saat memuat
//                .error(R.drawable.ic_error) // Gambar error jika URL tidak valid
                .into(holder.communityImage)

            holder.itemView.setOnClickListener {
                onItemClick(communityIds[position])
            }
        }

        override fun getItemCount() = communityList.size
    }
}
