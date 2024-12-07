import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningproject.R

class CommunityAdapter(
    private val communityList: List<String>,
    private val communityIds: List<String>,
    private val communityImages: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    class CommunityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val communityName: TextView = view.findViewById(R.id.community_name)
        val communityImage: ImageView = view.findViewById(R.id.community_image)
        val arrowRight: ImageView = view.findViewById(R.id.arrow_right)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)
        return CommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        holder.communityName.text = communityList[position]

        // Use Glide to load the image from URL
        Glide.with(holder.itemView.context)
            .load(communityImages[position])
            .into(holder.communityImage)

        // Set the arrow drawable
        holder.arrowRight.setImageResource(R.drawable.arrow_right)

        holder.itemView.setOnClickListener {
            onItemClick(communityIds[position])
        }
    }

    override fun getItemCount() = communityList.size
}