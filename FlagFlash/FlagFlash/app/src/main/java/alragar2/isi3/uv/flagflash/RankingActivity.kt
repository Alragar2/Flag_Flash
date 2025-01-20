package alragar2.isi3.uv.flagflash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RankingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var rankingAdapter: RankingAdapter
    private lateinit var userScoreManager: UserScoreManager

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        rankingAdapter = RankingAdapter()
        recyclerView.adapter = rankingAdapter

        userScoreManager = UserScoreManager()

        fetchTopPlayers()
    }

    private fun fetchTopPlayers(){
        CoroutineScope(Dispatchers.IO).launch {
            val topPlayers = userScoreManager.getTopPlayers(10)
            withContext(Dispatchers.Main){
                rankingAdapter.submitList(topPlayers)
            }
        }
    }
}