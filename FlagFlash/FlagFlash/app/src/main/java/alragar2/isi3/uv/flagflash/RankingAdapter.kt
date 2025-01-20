package alragar2.isi3.uv.flagflash

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class Player(val name: String, val score: Int)

class RankingAdapter : ListAdapter<Player, RankingAdapter.PlayerViewHolder>(PlayerDiffCallback()) {

    // Esta función se encarga de actualizar la lista de jugadores
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    // Esta función se encarga de mostrar los datos de cada jugador en la lista
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Esta clase se encarga de gestionar la vista de cada jugador
    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val positionTextView: TextView = itemView.findViewById(R.id.positionTextView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)

        @SuppressLint("SetTextI18n")
        fun bind(player: Player) {
            positionTextView.text = (adapterPosition + 1).toString()
            nameTextView.text = player.name
            scoreTextView.text = player.score.toString()

//            if (positionTextView.text == "1") {
//                positionTextView.setBackgroundResource(R.color.gold)
//            } else if (positionTextView.text == "2") {
//                positionTextView.setBackgroundResource(R.color.silver)
//            } else if (positionTextView.text == "3") {
//                positionTextView.setBackgroundResource(R.color.bronze)
//            }
            val background = itemView.background as GradientDrawable
            when (adapterPosition){
                0 -> background.setColor(itemView.resources.getColor(R.color.gold))
                1 -> background.setColor(itemView.resources.getColor(R.color.silver))
                2 -> background.setColor(itemView.resources.getColor(R.color.bronze))
                else -> background.setColor(itemView.resources.getColor(R.color.white))
            }
        }
    }

    // Esta clase se encarga de comparar los elementos de la lista
    class PlayerDiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem == newItem
        }
    }
}