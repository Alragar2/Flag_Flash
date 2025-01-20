package alragar2.isi3.uv.flagflash

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserScoreManager {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun saveUserScore(userId:String, score: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userScore = hashMapOf(
            "score" to score
        )
        db.collection("users").document(userId)
            .set(userScore, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Esta función se encarga de obtener los 10 mejores jugadores de la base de datos de Firebase
    suspend fun getTopPlayers(limit: Int): List<Player> {
        return try {
            val result = db.collection("users")
                .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            result.documents.map { document ->
                Player(
                    name = document.getString("name") ?: "Unknown",
                    score = document.getLong("score")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveUserName(userId: String, name: String, onSuccess: () -> Unit, onFailure: Exception.() -> Unit) {
        val userName = hashMapOf(
            "name" to name
        )
        db.collection("users").document(userId)
            .set(userName, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}