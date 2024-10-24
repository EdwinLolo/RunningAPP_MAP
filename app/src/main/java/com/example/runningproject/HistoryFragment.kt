import android.os.Bundle
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

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Create sample data for history
        val historyList = listOf(
            HistoryItem("November 26", "10,12 km", "701 kcal   11,2 km/hr"),
            HistoryItem("November 21", "9,89 km", "669 kcal   10,8 km/hr"),
            HistoryItem("November 16", "9,12 km", "608 kcal   10 km/hr")
        )

        // Set the adapter
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        // Bind the TextViews for username and user level
        val userNameTextView = view.findViewById<TextView>(R.id.user_name)
        val userLevelTextView = view.findViewById<TextView>(R.id.user_level)

        // Observe the SharedViewModel and update username and level
        sharedViewModel.username.observe(viewLifecycleOwner, { username ->
            userNameTextView.text = username
        })

        return view
    }
}
