package com.example.runningproject

import PostAdapter
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningproject.databinding.ActivityCommunityDetailBinding
import com.example.runningproject.model.Event
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CommunityDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityDetailBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private var isAdmin: Boolean = false
    private lateinit var communityId: String
    private val PICK_IMAGE_REQUEST = 1
    private var posterUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Mendapatkan communityId dari intent
        communityId = intent.getStringExtra("communityId") ?: ""

        // Memuat detail komunitas dan cek apakah pengguna adalah owner
        loadCommunityDetails()

        // Setup tombol kembali
        binding.backButton.setOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }

        // Setup RecyclerView untuk menampilkan postingan
        setupRecyclerView()

        // Tombol tambah postingan hanya muncul jika user adalah owner
        binding.addPostButton.setOnClickListener {
            showAddEventDialog()
        }
    }

    // Fungsi untuk memunculkan dialog input event
    private fun showAddEventDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_event)

        val eventNameInput = dialog.findViewById<EditText>(R.id.event_name_input)
        val eventTimeInput = dialog.findViewById<EditText>(R.id.event_time_input)
        val eventDescriptionInput = dialog.findViewById<EditText>(R.id.event_description_input)
        val selectPosterButton = dialog.findViewById<Button>(R.id.select_poster_button)
        val saveEventButton = dialog.findViewById<Button>(R.id.save_event_button)
        val recyclerView: RecyclerView = findViewById(R.id.posts_recycler_view)
        val spaceHeight = resources.getDimensionPixelSize(R.dimen.recycler_view_item_margin)
        recyclerView.addItemDecoration(MarginItemEventDecoration(spaceHeight))

        // Pilih gambar poster
        selectPosterButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Simpan event ke Firestore
        saveEventButton.setOnClickListener {
            val eventName = eventNameInput.text.toString()
            val eventTime = eventTimeInput.text.toString()
            val eventDescription = eventDescriptionInput.text.toString()

            if (eventName.isNotEmpty() && eventTime.isNotEmpty() && eventDescription.isNotEmpty()) {
                if (posterUri != null) {
                    uploadPosterAndSaveEvent(eventName, eventTime, eventDescription)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Pilih poster untuk event", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Isi semua kolom", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()

        // Mengatur ukuran dialog
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt() // 85% dari lebar layar
        val height = ViewGroup.LayoutParams.WRAP_CONTENT // Tinggi otomatis
        dialog.window?.setLayout(width, height) // Set lebar dan tinggi dialog
    }

    // Fungsi untuk mengunggah poster ke Firebase Storage dan menyimpan event ke Firestore
    private fun uploadPosterAndSaveEvent(name: String, time: String, description: String) {
        val fileReference = storageRef.child("event_posters/${System.currentTimeMillis()}.jpg")
        posterUri?.let {
            fileReference.putFile(it).addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    saveEventData(name, time, description, uri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal mengunggah poster", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menyimpan data event ke Firestore
    private fun saveEventData(name: String, time: String, description: String, posterUrl: String) {
        val eventData = hashMapOf(
            "name" to name,
            "time" to time,
            "description" to description,
            "posterUrl" to posterUrl,
            "timestamp" to Timestamp(Date()) // Menyimpan waktu event dibuat
        )

        firestore.collection("communities").document(communityId).collection("posts").add(eventData)
            .addOnSuccessListener {
                Toast.makeText(this, "Event berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan event", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk menangani pemilihan gambar
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            posterUri = data.data
            Toast.makeText(this, "Poster terpilih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCommunityDetails() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            firestore.collection("communities").document(communityId).get()
                .addOnSuccessListener { document ->
                    val communityName = document.getString("name")
                    val communityLocation = document.getString("location")
                    val communityLogo = document.getString("logoUrl")  // Mendapatkan URL logo dari Firestore
                    val adminId = document.getString("adminId")  // Ambil ID owner dari Firestore

                    binding.communityNameDetail.text = communityName
                    binding.communityDescription.text = communityLocation

                    // Tampilkan logo komunitas jika URL logo ada
                    if (communityLogo != null) {
                        Glide.with(this)
                            .load(communityLogo) // Memuat logo dari URL
                            .into(binding.communityLogoImageView) // ImageView di layout yang menampilkan logo
                    }

                    // Cek apakah pengguna saat ini adalah owner
                    if (adminId == currentUser.uid) {
                        isAdmin = true
                        binding.addPostButton.visibility = View.VISIBLE // Tampilkan tombol untuk owner
                    } else {
                        binding.addPostButton.visibility = View.GONE // Sembunyikan tombol jika bukan owner
                    }
                }
                .addOnFailureListener {
                    // Tangani error jika ada
                    Toast.makeText(this, "Gagal memuat detail komunitas", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun checkIfAdmin() {
        // Asumsikan bahwa Firestore menyimpan ID pengguna admin di field "adminId"
        val currentUserId = "CURRENT_USER_ID" // Ganti dengan ID pengguna saat ini dari auth
        firestore.collection("communities").document(communityId).get()
            .addOnSuccessListener { document ->
                val adminId = document.getString("adminId")
                if (adminId == currentUserId) {
                    isAdmin = true
                    binding.addPostButton.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                // Tangani error jika ada
            }
    }

        private fun setupRecyclerView() {
            val eventList = mutableListOf<Event>()
            val currentUser = auth.currentUser
            val spaceHeight = resources.getDimensionPixelSize(R.dimen.community_post_item_margin)
            binding.postsRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.postsRecyclerView.addItemDecoration(MarginItemEventDecoration(spaceHeight))

            // Pastikan currentUser tidak null
            if (currentUser != null) {
                // Mendapatkan adminId dari Firestore
                firestore.collection("communities").document(communityId).get()
                    .addOnSuccessListener { document ->
                        val adminId = document.getString("adminId")

                        // Memuat postingan dari Firestore
                        firestore.collection("communities").document(communityId).collection("posts").get()
                            .addOnSuccessListener { result ->
                                for (document in result) {
                                    val event = document.toObject(Event::class.java).copy(postId = document.id)
                                    eventList.add(event)
                                }

                                // Inisialisasi adapter setelah mendapatkan data
                                val adapter = PostAdapter(eventList, currentUser.uid, adminId ?: "", communityId)
                                binding.postsRecyclerView.layoutManager = LinearLayoutManager(this)
                                binding.postsRecyclerView.adapter = adapter

                                adapter.notifyDataSetChanged()
                            }

                    }
                    .addOnFailureListener {
                        // Tangani error jika adminId tidak bisa didapatkan
                    }
            }
        }


}
