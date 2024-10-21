import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningproject.R
import com.example.runningproject.model.Event

class PostAdapter(private val eventList: List<Event>) :
    RecyclerView.Adapter<PostAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.event_name)
        val eventTime: TextView = view.findViewById(R.id.event_time)
        val eventDescription: TextView = view.findViewById(R.id.event_description)
        val eventPoster: ImageView = view.findViewById(R.id.event_poster)
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

        // Menggunakan Glide untuk memuat poster event dari URL
        Glide.with(holder.itemView.context)
            .load(event.posterUrl)
//            .placeholder(R.drawable.placeholder_image) // Placeholder saat gambar dimuat
//            .error(R.drawable.error_image) // Gambar jika URL poster tidak valid
            .into(holder.eventPoster)
    }

    override fun getItemCount() = eventList.size
}
