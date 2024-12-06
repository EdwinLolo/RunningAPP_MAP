import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningproject.HistoryAdapter
import com.example.runningproject.HistoryItem
import com.example.runningproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Bind the TextViews for username and user level
        val userNameTextView = view.findViewById<TextView>(R.id.user_name)
        val userLevelTextView = view.findViewById<TextView>(R.id.user_level)

        // Observe the SharedViewModel and update username and level
        sharedViewModel.username.observe(viewLifecycleOwner, { username ->
            userNameTextView.text = username
        })

        // Fetch history data from Firestore
        fetchHistoryData()

        return view
    }

    private fun fetchHistoryData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId).collection("routes")
            .get()
            .addOnSuccessListener { documents ->
                val historyList = mutableListOf<HistoryItem>()
                for (document in documents) {
                    val date = document.getString("date") ?: ""
                    val distance = document.getDouble("totalDistance")?.toString() ?: "0.0 km"
                    val calories = document.getDouble("totalCalories")?.toString() ?: "0.0 kcal"
                    val pace = document.getDouble("pace")?.toString() ?: "0.0 min/km"
                    historyList.add(HistoryItem(date, distance, calories, pace))
                }
                historyAdapter = HistoryAdapter(historyList)
                recyclerView.adapter = historyAdapter
            }
            .addOnFailureListener { exception ->
                Log.w("HistoryFragment", "Error getting documents: ", exception)
            }
    }
}
