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

    fun getScore(onComplete: (Int) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val score = documentSnapshot.getLong("score")?.toInt() ?: 0
                onComplete(score)
            }.addOnFailureListener { onComplete(0) }
        } else { onComplete(0) }
    }

    fun setScore(score: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userScore = hashMapOf("score" to score)
            firestore.collection("users").document(userId).set(userScore, SetOptions.merge())
        }
    }

    fun getCoins(onComplete: (Int) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("coins")) {
                        val coins = documentSnapshot.getLong("coins")?.toInt() ?: 0
                        onComplete(coins)
                    } else {
                        val initialCoins = 100
                        setCoins(initialCoins)
                        onComplete(initialCoins)
                    }
                } else { onComplete(0) }
            }.addOnFailureListener { onComplete(0) }
        } else { onComplete(0) }
    }

    fun setCoins(coins: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userCoins = hashMapOf("coins" to coins)
            firestore.collection("users").document(userId).set(userCoins, SetOptions.merge())
        }
    }

    fun getSelectedPet(onComplete: (String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                onComplete(documentSnapshot.getString("selectedPet"))
            }.addOnFailureListener { onComplete(null) }
        } else { onComplete(null) }
    }

    fun setSelectedPet(petId: String?) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val data = hashMapOf("selectedPet" to petId)
            firestore.collection("users").document(userId).set(data, SetOptions.merge())
        }
    }

    fun getOwnedPets(onComplete: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val pets = documentSnapshot.get("ownedPets") as? List<String> ?: emptyList()
                onComplete(pets)
            }.addOnFailureListener { onComplete(emptyList()) }
        } else { onComplete(emptyList()) }
    }

    fun addOwnedPet(petId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            getOwnedPets { currentPets ->
                if (!currentPets.contains(petId)) {
                    val newPets = currentPets.toMutableList()
                    newPets.add(petId)
                    val data = hashMapOf("ownedPets" to newPets)
                    firestore.collection("users").document(userId).set(data, SetOptions.merge())
                }
            }
        }
    }

    fun getFoodCount(onComplete: (Int) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                onComplete(documentSnapshot.getLong("foodCount")?.toInt() ?: 0)
            }.addOnFailureListener { onComplete(0) }
        } else { onComplete(0) }
    }

    fun setFoodCount(count: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val data = hashMapOf("foodCount" to count)
            firestore.collection("users").document(userId).set(data, SetOptions.merge())
        }
    }

    fun isPetFed(petId: String, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val fedStates = documentSnapshot.get("petFedStates") as? Map<String, Boolean> ?: emptyMap()
                onComplete(fedStates[petId] ?: false)
            }.addOnFailureListener { onComplete(false) }
        } else { onComplete(false) }
    }

    fun setPetFed(petId: String, fed: Boolean) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val fedStates = (documentSnapshot.get("petFedStates") as? Map<String, Boolean> ?: emptyMap()).toMutableMap()
                fedStates[petId] = fed
                val data = hashMapOf("petFedStates" to fedStates)
                firestore.collection("users").document(userId).set(data, SetOptions.merge())
            }
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
                onComplete(documentSnapshot.getString("name") ?: "Unknown")
            }.addOnFailureListener { onComplete("Unknown") }
        } else { onComplete("Unknown") }
    }

    fun setUserName(name: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userName = hashMapOf("name" to name)
            firestore.collection("users").document(userId).set(userName, SetOptions.merge())
        }
    }
}