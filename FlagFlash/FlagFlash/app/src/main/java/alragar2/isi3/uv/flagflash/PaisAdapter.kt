package alragar2.isi3.uv.flagflash

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PaisAdapter(
    private var paises: List<Map<String, String>>,
    private val itemClick: (Map<String, String>) -> Unit
) : RecyclerView.Adapter<PaisAdapter.PaisViewHolder>() {

    inner class PaisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(pais: Map<String, String>) {
            val banderaUrl = pais["bandera"]
            val flagImageView = itemView.findViewById<ImageView>(R.id.flagImageView)
            if (banderaUrl != null) {
                Glide.with(itemView.context)
                    .load(banderaUrl)
                    .into(flagImageView)
            }
            itemView.setOnClickListener { itemClick(pais) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaisViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pais_item, parent, false)
        return PaisViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaisViewHolder, position: Int) {
        holder.bind(paises[position])
        //holder.itemView.setOnClickListener { itemClick(paises[position]) }
    }

    fun updatePaises(newPaises: List<Map<String, String>>) {
        paises = newPaises
        Log.d("FirebaseData", "Paises actualizados: $paises")
        notifyDataSetChanged()
    }

    override fun getItemCount() = paises.size
}