package com.tetraverse.app.game

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class LeaderboardEntry(
    val address: String = "",
    val score: Int = 0,
    val timestamp: Long = 0
)

object LeaderboardManager {
    private const val TAG = "LeaderboardManager"
    
    private val database by lazy { 
        try {
            Firebase.database.reference.child("leaderboard")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Database initialization failed", e)
            null
        }
    }

    fun getLeaderboardFlow(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val db = database
        if (db == null) {
            trySend(emptyList()) 
            awaitClose { }
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val entries = snapshot.children.mapNotNull { 
                        // Manual mapping to be safe with Firebase numbers
                        val addr = it.child("address").getValue(String::class.java) ?: ""
                        val sc = it.child("score").getValue(Long::class.java)?.toInt() ?: 0
                        val ts = it.child("timestamp").getValue(Long::class.java) ?: 0L
                        if (addr.isNotEmpty()) LeaderboardEntry(addr, sc, ts) else null
                    }.sortedByDescending { it.score }.take(50)
                    
                    trySend(entries)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing leaderboard data", e)
                    trySend(emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Leaderboard flow cancelled: ${error.message}")
                trySend(emptyList())
            }
        }
        
        db.addValueEventListener(listener)
        awaitClose { db.removeEventListener(listener) }
    }

    suspend fun submitScore(address: String, score: Int) {
        if (address.isEmpty()) return
        val db = database ?: return
        try {
            Log.d(TAG, "Submitting score: $score for $address")
            
            // 1. Get current score with timeout
            val currentSnapshot = withTimeoutOrNull(5000L) {
                db.child(address).get().await()
            }
            
            val currentScore = currentSnapshot?.child("score")?.getValue(Long::class.java)?.toInt() ?: 0
            Log.d(TAG, "Current score in DB: $currentScore")

            // 2. Only update if current score is better
            if (score > currentScore) {
                val data = mapOf(
                    "address" to address,
                    "score" to score,
                    "timestamp" to System.currentTimeMillis()
                )
                db.child(address).setValue(data).await()
                Log.d(TAG, "Score submitted successfully!")
            } else {
                Log.d(TAG, "Score not submitted (lower than existing)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit score", e)
        }
    }
}
