package alragar2.isi3.uv.flagflash

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("score_prefs", Context.MODE_PRIVATE)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get the score from the Firestore database
    fun getScore(onComplete: (Int) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val score = documentSnapshot.getLong("score")?.toInt() ?: 0
                    onComplete(score)
                    Log.d("ScorePreferences", "Score get: $score")
                } else {
                    onComplete(0)
                }
            }.addOnFailureListener {
                onComplete(0)
            }
        } else {
            onComplete(0)
        }

    }

    fun setScore(score: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userScore = hashMapOf("score" to score)
            firestore.collection("users").document(userId).set(userScore, SetOptions.merge())
            Log.d("ScorePreferences", "Score set: $score")
        }
    }

    fun getInitialScore(): Int {
        return sharedPreferences.getInt("initial_score", 0)
    }

    fun setInitialScore(score: Int) {
        sharedPreferences.edit().putInt("initial_score", score).apply()
    }

    fun getUserName(onComplete: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name") ?: "Unknown"
                    onComplete(name)
                    Log.d("ScorePreferences", "Name get: $name")
                } else {
                    onComplete("Unknown")
                }
            }.addOnFailureListener {
                onComplete("Unknown")
            }
        } else {
            onComplete("Unknown")
        }
    }

    fun setUserName(name: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userName = hashMapOf("name" to name)
            firestore.collection("users").document(userId).set(userName, SetOptions.merge())
            Log.d("ScorePreferences", "Name set: $name")
        }
    }
}