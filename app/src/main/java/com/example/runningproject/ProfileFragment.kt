package com.example.runningproject

import SharedViewModel
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.runningproject.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore // Inisialisasi Firestore
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    // Inisialisasi SharedViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        // Tampilkan informasi pengguna dari Firestore
        displayUserInfoFromFirestore()

        // Tombol Logout
        binding.logout.setOnClickListener {
            auth.signOut()

            // Arahkan ke LoginActivity setelah logout
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Menambahkan listener untuk addPhoto
        binding.addPhoto.setOnClickListener {
            openGallery()
        }

        return view
    }

    // Fungsi untuk membuka galeri
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Menangani hasil dari aktivitas galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.profileImage.setImageURI(imageUri) // Menampilkan gambar yang dipilih di profileImage
        }
    }

    // Fungsi untuk mengambil dan menampilkan informasi pengguna dari Firestore
    private fun displayUserInfoFromFirestore() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username") ?: "User"
                        val email = document.getString("email") ?: "No email"

                        // Set data ke UI
                        binding.userName.text = username
                        binding.userLevel.text = "Beginner" // Contoh level pengguna

                        // Set username ke SharedViewModel agar fragmen lain bisa mengaksesnya
                        sharedViewModel.setUsername(username)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
