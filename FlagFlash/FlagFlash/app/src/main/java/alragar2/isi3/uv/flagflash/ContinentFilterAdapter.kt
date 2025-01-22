package alragar2.isi3.uv.flagflash

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

// Create a new adapter for the filter options
class ContinentFilterAdapter(private val filters: List<String>, private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<ContinentFilterAdapter.FilterViewHolder>() {

    private var selectedFilter: String? = filters.firstOrNull()

    class FilterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.filterButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_continent_filter, parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = filters[position]
        holder.button.text = filter
        holder.button.setBackgroundColor(
            if (filter == selectedFilter) Color.GREEN else Color.parseColor("#2E8DFF")
        )
        holder.button.setOnClickListener {
            selectedFilter = filter
            notifyDataSetChanged()
            onClick(filter) }
    }

    override fun getItemCount() = filters.size
}