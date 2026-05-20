package alragar2.isi3.uv.flagflash.online

import alragar2.isi3.uv.flagflash.online.models.OnlinePlayer
import alragar2.isi3.uv.flagflash.online.models.OnlineQuestion
import alragar2.isi3.uv.flagflash.online.models.OnlineRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RoomRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://flag-flash-tfg-default-rtdb.europe-west1.firebasedatabase.app/"
    ).reference
    private val roomsRef = db.child("online_rooms")
    private val countriesRef = db.child("paises")

    private val auth = FirebaseAuth.getInstance()
    val myUid get() = auth.currentUser?.uid ?: ""

    // ── Room Creation ──────────────────────────────────────────────────────

    suspend fun createRoom(gameMode: String): String {
        val code = generateCode()
        val info = playerInfo()
        val me = OnlinePlayer(uid = myUid, name = info.first, avatar = info.second, frame = info.third)
        val room = mapOf(
            "roomCode" to code,
            "hostUid" to myUid,
            "gameMode" to gameMode,
            "status" to "WAITING",
            "questionIndex" to 0,
            "totalQuestions" to 15,
            "players" to mapOf(myUid to playerMap(me))
        )
        roomsRef.child(code).setValue(room).await()
        return code
    }

    suspend fun joinRoom(code: String): Boolean {
        val snap = roomsRef.child(code).get().await()
        if (!snap.exists()) return false
        val status = snap.child("status").getValue(String::class.java)
        if (status != "WAITING") return false
        val players = snap.child("players").childrenCount
        if (players >= 4) return false
        val info = playerInfo()
        val me = OnlinePlayer(uid = myUid, name = info.first, avatar = info.second, frame = info.third)
        roomsRef.child(code).child("players").child(myUid).setValue(playerMap(me)).await()
        return true
    }

    suspend fun leaveRoom(code: String) {
        roomsRef.child(code).child("players").child(myUid).removeValue().await()
    }

    // ── Game Control (host only) ──────────────────────────────────────────

    suspend fun startGame(code: String, allCountries: List<Map<String, Any>>, mode: String) {
        roomsRef.child(code).child("status").setValue("IN_PROGRESS").await()
        pushNextQuestion(code, allCountries, mode, 0)
    }

    private suspend fun pushNextQuestion(
        code: String,
        allCountries: List<Map<String, Any>>,
        mode: String,
        index: Int
    ) {
        if (allCountries.size < 4) return
        val chosen = allCountries.shuffled().take(4)
        val correct = chosen.random()
        val q = when (mode) {
            "BANDERA" -> mapOf(
                "promptImageUrl" to (correct["bandera"] ?: ""),
                "options" to chosen.map { it["nombre"] ?: "" }.shuffled(),
                "correctOption" to (correct["nombre"] ?: "")
            )
            "PAIS" -> mapOf(
                "promptText" to (correct["nombre"] ?: ""),
                "options" to chosen.map { it["bandera"] ?: "" }.shuffled(),
                "correctOption" to (correct["bandera"] ?: "")
            )
            "CAPITAL" -> mapOf(
                "promptImageUrl" to (correct["bandera"] ?: ""),
                "promptText" to (correct["nombre"] ?: ""),
                "options" to chosen.map { it["capital"] ?: "" }.shuffled(),
                "correctOption" to (correct["capital"] ?: "")
            )
            else -> mapOf()
        }
        roomsRef.child(code).updateChildren(
            mapOf(
                "currentQuestion" to q,
                "questionIndex" to index
            )
        ).await()
        // Reset answered flags for all players
        val playersSnap = roomsRef.child(code).child("players").get().await()
        val updates = mutableMapOf<String, Any>()
        for (child in playersSnap.children) {
            updates["players/${child.key}/answeredCurrentQuestion"] = false
        }
        roomsRef.child(code).updateChildren(updates).await()
    }

    suspend fun advanceQuestion(code: String, allCountries: List<Map<String, Any>>, mode: String, newIndex: Int, total: Int) {
        if (newIndex >= total) {
            roomsRef.child(code).child("status").setValue("FINISHED").await()
        } else {
            pushNextQuestion(code, allCountries, mode, newIndex)
        }
    }

    // ── Player actions ────────────────────────────────────────────────────

    suspend fun submitAnswer(code: String, questionIndex: Int, answer: String, isCorrect: Boolean) {
        val updates = mutableMapOf<String, Any>(
            "players/$myUid/answeredCurrentQuestion" to true,
            "answers/$questionIndex/$myUid" to answer
        )
        if (isCorrect) {
            val currentScore = roomsRef.child(code).child("players").child(myUid)
                .child("score").get().await().getValue(Int::class.java) ?: 0
            updates["players/$myUid/score"] = currentScore + 1
        }
        roomsRef.child(code).updateChildren(updates).await()
    }

    // ── Realtime listeners ────────────────────────────────────────────────

    fun observeRoom(code: String): Flow<OnlineRoom?> = callbackFlow {
        val ref = roomsRef.child(code)
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                trySend(parseRoom(snap))
            }
            override fun onCancelled(error: DatabaseError) { trySend(null) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeQuestion(code: String): Flow<OnlineQuestion?> = callbackFlow {
        val ref = roomsRef.child(code).child("currentQuestion")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val q = OnlineQuestion(
                    promptImageUrl = snap.child("promptImageUrl").getValue(String::class.java),
                    promptText = snap.child("promptText").getValue(String::class.java),
                    options = (snap.child("options").value as? List<String>) ?: emptyList(),
                    correctOption = snap.child("correctOption").getValue(String::class.java) ?: ""
                )
                trySend(q)
            }
            override fun onCancelled(error: DatabaseError) { trySend(null) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // ── Utility ───────────────────────────────────────────────────────────

    suspend fun loadCountries(): List<Map<String, Any>> {
        val snap = countriesRef.get().await()
        return snap.children.mapNotNull { it.value as? Map<String, Any> }
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private suspend fun playerInfo(): Triple<String, String, String> {
        val uid = myUid
        return try {
            val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid).get().await()
            val name = doc.getString("name") ?: "Jugador"
            val avatar = doc.getString("avatar") ?: "default"
            val frame = doc.getString("frame") ?: "none"
            Triple(name, avatar, frame)
        } catch (e: Exception) { Triple("Jugador", "default", "none") }
    }

    private fun playerMap(p: OnlinePlayer) = mapOf(
        "uid" to p.uid,
        "name" to p.name,
        "score" to p.score,
        "answeredCurrentQuestion" to false,
        "avatar" to p.avatar,
        "frame" to p.frame
    )

    private fun parseRoom(snap: DataSnapshot): OnlineRoom? {
        if (!snap.exists()) return null
        val players = mutableMapOf<String, OnlinePlayer>()
        snap.child("players").children.forEach { ps ->
            players[ps.key ?: ""] = OnlinePlayer(
                uid = ps.child("uid").getValue(String::class.java) ?: "",
                name = ps.child("name").getValue(String::class.java) ?: "",
                score = ps.child("score").getValue(Int::class.java) ?: 0,
                answeredCurrentQuestion = ps.child("answeredCurrentQuestion").getValue(Boolean::class.java) ?: false,
                avatar = ps.child("avatar").getValue(String::class.java) ?: "default",
                frame = ps.child("frame").getValue(String::class.java) ?: "none"
            )
        }
        return OnlineRoom(
            roomCode = snap.child("roomCode").getValue(String::class.java) ?: "",
            hostUid = snap.child("hostUid").getValue(String::class.java) ?: "",
            gameMode = snap.child("gameMode").getValue(String::class.java) ?: "BANDERA",
            status = snap.child("status").getValue(String::class.java) ?: "WAITING",
            questionIndex = snap.child("questionIndex").getValue(Int::class.java) ?: 0,
            totalQuestions = snap.child("totalQuestions").getValue(Int::class.java) ?: 15,
            players = players
        )
    }
}
