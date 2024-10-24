package com.example.runningproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runningproject.databinding.ActivityAddCommunityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddCommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCommunityBinding
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference.child("community_logos")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi binding
        binding = ActivityAddCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Klik listener untuk tombol upload logo
        binding.uploadLogoButton.setOnClickListener {
            selectImage()
        }

        // Klik listener untuk tombol simpan komunitas
        binding.saveCommunityButton.setOnClickListener {
            val name = binding.communityNameInput.text.toString()
            val location = binding.communityLocationInput.text.toString()

            if (name.isNotEmpty() && location.isNotEmpty()) {
                if (imageUri != null) {
                    uploadLogoAndSaveCommunity(name, location)
                } else {
                    saveCommunityData(name, location, null)
                }
            } else {
                Toast.makeText(this, "Isi semua kolom", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backButton.setOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
            finish() // Tutup AddCommunityActivity setelah pindah ke CommunityActivity
        }
    }

    // Fungsi untuk memilih gambar dari galeri
    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Fungsi untuk mengunggah logo dan menyimpan data komunitas ke Firestore
    private fun uploadLogoAndSaveCommunity(name: String, location: String) {
        val fileReference = storageRef.child(System.currentTimeMillis().toString() + ".jpg")
        imageUri?.let {
            fileReference.putFile(it).addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    saveCommunityData(name, location, uri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal mengunggah logo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menyimpan data komunitas ke Firestore
    private fun saveCommunityData(name: String, location: String, logoUrl: String?) {
        // Mendapatkan ID pengguna saat ini
        val currentUser = auth.currentUser
        val adminId = currentUser?.uid // Dapatkan UID pengguna saat ini

        if (adminId == null) {
            Toast.makeText(this, "Pengguna tidak terotentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        // Data komunitas yang akan disimpan
        val communityData = hashMapOf(
            "name" to name,
            "location" to location,
            "logoUrl" to logoUrl,
            "adminId" to adminId // Simpan adminId dari pengguna saat ini
        )

        // Simpan data komunitas ke Firestore
        firestore.collection("communities").add(communityData)
            .addOnSuccessListener {
                Toast.makeText(this, "Komunitas berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish() // Tutup activity setelah data disimpan
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }

    // Mendapatkan hasil pemilihan gambar
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Toast.makeText(this, "Logo terpilih", Toast.LENGTH_SHORT).show()
        }
    }
}
