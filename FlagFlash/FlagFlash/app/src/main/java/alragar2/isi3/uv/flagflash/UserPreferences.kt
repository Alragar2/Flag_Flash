package alragar2.isi3.uv.flagflash

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue

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
            val data = mapOf("ownedPets" to FieldValue.arrayUnion(petId))
            firestore.collection("users").document(userId).set(data, SetOptions.merge())
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
                val fedStates = documentSnapshot.get("petFedStates") as? Map<String, Any> ?: emptyMap()
                val fed = fedStates[petId] as? Boolean ?: false
                onComplete(fed)
            }.addOnFailureListener { onComplete(false) }
        } else { onComplete(false) }
    }

    fun getPetFedStates(onComplete: (Map<String, Boolean>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val fedStates = documentSnapshot.get("petFedStates") as? Map<String, Any> ?: emptyMap()
                val result = fedStates.mapValues { it.value as? Boolean ?: false }
                onComplete(result)
            }.addOnFailureListener { onComplete(emptyMap()) }
        } else { onComplete(emptyMap()) }
    }

    fun setPetFed(petId: String, fed: Boolean) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val data = mapOf("petFedStates" to mapOf(petId to fed))
            firestore.collection("users").document(userId).set(data, SetOptions.merge())
                .addOnFailureListener { e -> Log.e("UserPreferences", "Error setting pet fed state", e) }
        }
    }

    fun getDiscoveredCountries(onComplete: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val countries = documentSnapshot.get("discoveredCountries") as? List<String> ?: emptyList()
                onComplete(countries)
            }.addOnFailureListener { onComplete(emptyList()) }
        } else { onComplete(emptyList()) }
    }

    fun addDiscoveredCountry(countryName: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val data = mapOf("discoveredCountries" to FieldValue.arrayUnion(countryName))
            firestore.collection("users").document(userId).set(data, SetOptions.merge())
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

    fun getAvatar(onComplete: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                onComplete(documentSnapshot.getString("avatar") ?: "default")
            }.addOnFailureListener { onComplete("default") }
        } else { onComplete("default") }
    }

    fun setAvatar(avatar: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val data = hashMapOf("avatar" to avatar)
            firestore.collection("users").document(userId).set(data, SetOptions.merge())
        }
    }

    fun getFrame(onComplete: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                onComplete(documentSnapshot.getString("frame") ?: "none")
            }.addOnFailureListener { onComplete("none") }
        } else { onComplete("none") }
    }

    fun setFrame(frame: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val data = hashMapOf("frame" to frame)
            firestore.collection("users").document(userId).set(data, SetOptions.merge())
        }
    }
    
    fun getOwnedCosmetics(onComplete: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val cosmetics = documentSnapshot.get("ownedCosmetics") as? List<String> ?: emptyList()
                onComplete(cosmetics)
            }.addOnFailureListener { onComplete(emptyList()) }
        } else { onComplete(emptyList()) }
    }

    fun addOwnedCosmetic(cosmeticId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val data = mapOf("ownedCosmetics" to FieldValue.arrayUnion(cosmeticId))
            firestore.collection("users").document(userId).set(data, SetOptions.merge())
        }
    }

    fun incrementTotalGames() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val totalGames = documentSnapshot.getLong("total_games") ?: 0L
                firestore.collection("users").document(userId).set(hashMapOf("total_games" to totalGames + 1), SetOptions.merge())
            }
        }
    }

    fun incrementPerfectGames() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val perfectGames = documentSnapshot.getLong("perfect_games") ?: 0L
                firestore.collection("users").document(userId).set(hashMapOf("perfect_games" to perfectGames + 1), SetOptions.merge())
            }
        }
    }

    fun getStats(onComplete: (totalGames: Long, perfectGames: Long) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val totalGames = documentSnapshot.getLong("total_games") ?: 0L
                val perfectGames = documentSnapshot.getLong("perfect_games") ?: 0L
                onComplete(totalGames, perfectGames)
            }.addOnFailureListener { onComplete(0L, 0L) }
        } else { onComplete(0L, 0L) }
    }

    fun getUnlockedAchievements(onComplete: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val achievements = documentSnapshot.get("unlocked_achievements") as? List<String> ?: emptyList()
                onComplete(achievements)
            }.addOnFailureListener { onComplete(emptyList()) }
        } else { onComplete(emptyList()) }
    }

    fun unlockAchievement(achievementId: String, onUnlocked: () -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            getUnlockedAchievements { current ->
                if (!current.contains(achievementId)) {
                    val newAchievements = current.toMutableList()
                    newAchievements.add(achievementId)
                    val data = hashMapOf("unlocked_achievements" to newAchievements)
                    firestore.collection("users").document(userId).set(data, SetOptions.merge()).addOnSuccessListener {
                        onUnlocked()
                    }
                }
            }
        }
    }

    fun addCorrectAnswers(count: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val total = documentSnapshot.getLong("total_correct_answers") ?: 0L
                firestore.collection("users").document(userId).set(hashMapOf("total_correct_answers" to total + count), SetOptions.merge())
            }
        }
    }

    fun updateSurvivalMaxScore(score: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val currentMax = documentSnapshot.getLong("survival_max_score") ?: 0L
                if (score > currentMax) {
                    firestore.collection("users").document(userId).set(hashMapOf("survival_max_score" to score), SetOptions.merge())
                }
            }
        }
    }

    fun updateTimeAttackMaxScore(score: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val currentMax = documentSnapshot.getLong("time_attack_max_score") ?: 0L
                if (score > currentMax) {
                    firestore.collection("users").document(userId).set(hashMapOf("time_attack_max_score" to score), SetOptions.merge())
                }
            }
        }
    }

    fun getAdvancedStats(onComplete: (correctAnswers: Long, survivalMax: Long, timeAttackMax: Long) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                val cAnswers = documentSnapshot.getLong("total_correct_answers") ?: 0L
                val sMax = documentSnapshot.getLong("survival_max_score") ?: 0L
                val tMax = documentSnapshot.getLong("time_attack_max_score") ?: 0L
                onComplete(cAnswers, sMax, tMax)
            }.addOnFailureListener { onComplete(0L, 0L, 0L) }
        } else { onComplete(0L, 0L, 0L) }
    }

    fun isMusicEnabled(): Boolean {
        return sharedPreferences.getBoolean("music_enabled", true)
    }

    fun setMusicEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("music_enabled", enabled).apply()
    }

    fun isSoundEffectsEnabled(): Boolean {
        return sharedPreferences.getBoolean("sound_effects_enabled", true)
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("sound_effects_enabled", enabled).apply()
    }
}
