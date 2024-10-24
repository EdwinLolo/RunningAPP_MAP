import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningproject.R
import com.example.runningproject.model.Event
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    private val eventList: List<Event>,
    private val currentUserId: String,
    private val adminId: String,
    private val communityId: String // Tambahkan communityId sebagai parameter
) : RecyclerView.Adapter<PostAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.event_name)
        val eventTime: TextView = view.findViewById(R.id.event_time)
        val eventDescription: TextView = view.findViewById(R.id.event_description)
        val eventPoster: ImageView = view.findViewById(R.id.event_poster)
        val deletePostButton: ImageButton = view.findViewById(R.id.delete_post_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.eventName.text = event.name
        holder.eventTime.text = event.time
        holder.eventDescription.text = event.description

        // Memuat poster menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(event.posterUrl)
            .into(holder.eventPoster)

        // Logika untuk menampilkan atau menyembunyikan deletePostButton
        if (currentUserId == adminId) {
            holder.deletePostButton.visibility = View.VISIBLE
        } else {
            holder.deletePostButton.visibility = View.GONE
        }

        // Menghapus post ketika deletePostButton diklik
        holder.deletePostButton.setOnClickListener {
            // Panggil fungsi untuk menghapus event
            deleteEvent(event.postId, position, holder.itemView.context)
        }
    }

    private fun deleteEvent(postId: String, position: Int, context: android.content.Context) {
        // Referensi ke koleksi posts di dalam komunitas
        val postRef = FirebaseFirestore.getInstance()
            .collection("communities")
            .document(communityId) // Gunakan communityId yang diterima dari konstruktor
            .collection("posts")
            .document(postId)

        // Menghapus dokumen di Firestore
        postRef.delete()
            .addOnSuccessListener {
                // Jika berhasil, hapus item dari list di adapter dan beri tahu RecyclerView
                (eventList as MutableList).removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, eventList.size)
                Toast.makeText(context, "Postingan berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal menghapus postingan", Toast.LENGTH_SHORT).show()
            }
    }


    override fun getItemCount() = eventList.size
}

