package com.example.runningproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.runningproject.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observasi username dari SharedViewModel dan tampilkan di UI
        sharedViewModel.username.observe(viewLifecycleOwner, { username ->
            binding.userName.text = "Hello, $username"
        })

        // Observasi total distance, pace, dan calories dari SharedViewModel dan tampilkan di UI
        sharedViewModel.totalDistance.observe(viewLifecycleOwner, { totalDistance ->
            binding.totalDistance.text = String.format("%.2f km", totalDistance.toDouble() / 1000)
        })

        sharedViewModel.totalCalories.observe(viewLifecycleOwner, { totalCalories ->
            binding.totalCalories.text = String.format("%.0f kcal", totalCalories)
        })

        sharedViewModel.totalPace.observe(viewLifecycleOwner, { totalPace ->
            binding.totalPace.text = String.format("%.1f min/km", totalPace)
        })

        // Mengatur klik listener untuk LinearLayout join_community
        binding.joinCommunity.setOnClickListener {
            val intent = Intent(requireContext(), CommunityActivity::class.java)
            startActivity(intent)
        }

        binding.tracking.setOnClickListener {
            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivity(intent)
        }

        // Fetch history data
        fetchHistoryData()
    }

    private fun fetchHistoryData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId).collection("routes")
            .get()
            .addOnSuccessListener { documents ->
                var totalDistance = 0.0
                var totalCalories = 0.0
                var totalPace = 0.0
                var paceCount = 0

                for (document in documents) {
                    val distance = document.getDouble("totalDistance") ?: 0.0
                    val calories = document.getDouble("totalCalories") ?: 0.0
                    val pace = document.getDouble("pace") ?: 0.0

                    totalDistance += distance
                    totalCalories += calories
                    if (pace > 0) {
                        totalPace += pace
                        paceCount++
                    }
                }

                // Update the total TextViews
                binding.totalDistance.text = String.format("%.2f km", totalDistance / 1000)
                binding.totalCalories.text = String.format("%.0f kcal", totalCalories)
                binding.totalPace.text = if (paceCount > 0) String.format("%.1f min/km", totalPace / paceCount) else "0.0 min/km"
            }
            .addOnFailureListener { exception ->
                Log.w("HomeFragment", "Error getting documents: ", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}