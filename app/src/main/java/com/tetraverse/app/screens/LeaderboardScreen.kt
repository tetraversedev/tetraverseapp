package com.tetraverse.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetraverse.app.game.LeaderboardManager
import com.tetraverse.app.ui.theme.*
import java.util.*

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val leaderboardData by LeaderboardManager.getLeaderboardFlow().collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text("GLOBAL RANKING", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (leaderboardData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EmojiEvents, 
                            contentDescription = null, 
                            tint = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "NO RANKINGS YET", 
                            color = Color.White.copy(0.4f), 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Be the first to claim the throne!", 
                            color = Color.White.copy(0.2f), 
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Text("TOP 50 PLAYERS", color = NeonBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Ranking List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(leaderboardData) { index, entry ->
                        RankingRow(index + 1, entry.address, entry.score)
                    }
                }
            }
        }
    }
}

@Composable
fun RankingRow(rank: Int, address: String, score: Int) {
    val displayAddress = if (address.length > 10) "${address.take(6)}...${address.takeLast(4)}" else address
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            color = if (rank <= 3) NeonYellow else Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            modifier = Modifier.width(30.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(displayAddress, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("SOLANA_ID", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        }
        Text(
            text = String.format(Locale.US, "%,d", score),
            color = NeonPink,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
