package com.example.runningproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.runningproject.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inisialisasi View Binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Tombol Logout
        binding.logout.setOnClickListener {
            // Logika untuk logout
            auth.signOut()

            // Arahkan kembali ke halaman login atau main activity setelah logout
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Membersihkan binding saat fragment dihancurkan
        _binding = null
    }
}
