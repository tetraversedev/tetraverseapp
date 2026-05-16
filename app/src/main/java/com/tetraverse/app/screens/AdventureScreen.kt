package com.tetraverse.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetraverse.app.ui.theme.*

@Composable
fun AdventureScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Text("ADVENTURE MAP", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(30.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                MapNode(1, "Cyber City", NeonBlue, isUnlocked = true)
                MapNode(2, "Lava Core", Color.Red, isUnlocked = false)
                MapNode(3, "Ice Matrix", Color.Cyan, isUnlocked = false)
                MapNode(4, "Space Colony", NeonPink, isUnlocked = false)
            }
        }
    }
}

@Composable
fun MapNode(id: Int, name: String, color: Color, isUnlocked: Boolean) {
    Column(
        modifier = Modifier.padding(start = (id * 40).dp, top = (id * 80).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) color else Color.Gray)
                .border(2.dp, Color.White, CircleShape)
                .clickable(enabled = isUnlocked) { },
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                Text(id.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            } else {
                Icon(Icons.Default.Lock, null, tint = Color.White.copy(alpha = 0.5f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, color = if (isUnlocked) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
