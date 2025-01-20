package alragar2.isi3.uv.flagflash
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import alragar2.isi3.uv.flagflash.R

class GameModeAdapter(private val gameModes: List<String>, private val onGameModeClick: (String) -> Unit) : RecyclerView.Adapter<GameModeAdapter.GameModeViewHolder>() {

    inner class GameModeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.gameModeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameModeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_mode, parent, false)
        return GameModeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameModeViewHolder, position: Int) {
        val gameMode = gameModes[position]
        holder.button.text = gameMode
        holder.button.setOnClickListener { onGameModeClick(gameMode) }
    }

    override fun getItemCount() = gameModes.size
}