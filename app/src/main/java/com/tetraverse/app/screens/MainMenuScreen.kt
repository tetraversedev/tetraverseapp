package com.tetraverse.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetraverse.app.ui.theme.*

val avatarList = listOf(
    Icons.Default.Face,
    Icons.Default.SentimentSatisfied,
    Icons.Default.Psychology,
    Icons.Default.RocketLaunch,
    Icons.Default.Bolt,
    Icons.Default.AutoAwesome,
    Icons.Default.VpnKey,
    Icons.Default.Star,
    Icons.Default.Terrain,
    Icons.Default.BrightnessHigh,
    Icons.Default.Casino,
    Icons.Default.Celebration
)

@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenProfile: () -> Unit,
    onConnectWallet: () -> Unit,
    isWalletConnected: Boolean,
    walletAddress: String,
    latestSignature: String,
    selectedAvatarId: Int,
    equippedColor: Color? = null,
    isLoading: Boolean = false,
    appVersion: String = ""
) {
    val charScale = rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0.97f, 
        targetValue = 1.03f, 
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing), RepeatMode.Reverse), 
        label = ""
    )
    val uriHandler = LocalUriHandler.current

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Box(modifier = Modifier.size(400.dp).align(Alignment.TopEnd).offset(100.dp, (-100).dp).blur(100.dp).background(NeonBlue.copy(0.1f), CircleShape))
        Box(modifier = Modifier.size(400.dp).align(Alignment.BottomStart).offset((-100).dp, 100.dp).blur(100.dp).background(NeonPink.copy(0.1f), CircleShape))
        
        CityScape()

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopHUD(onConnectWallet, isWalletConnected, walletAddress, latestSignature, onOpenProfile, selectedAvatarId)
            
            Spacer(modifier = Modifier.height(40.dp))

            // Robust Elegant Logo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "TETRAVERSE", 
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black, 
                        color = Color.White,
                        letterSpacing = 4.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Surface(
                    color = (equippedColor ?: NeonBlue).copy(0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.offset(y = (-2).dp)
                ) {
                    Text(
                        " BEYOND THE BLOCKS ", 
                        fontSize = 10.sp, 
                        color = equippedColor ?: NeonBlue, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 4.sp,
                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(modifier = Modifier.size(240.dp).scale(charScale.value), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(180.dp, 25.dp)
                        .blur(15.dp)
                        .background(Brush.radialGradient(listOf((equippedColor ?: NeonBlue).copy(0.3f), Color.Transparent)))
                )
                Icon(
                    Icons.Default.SmartToy, 
                    null, 
                    tint = equippedColor ?: Color.White, 
                    modifier = Modifier.size(160.dp).shadow(20.dp, ambientColor = equippedColor ?: NeonBlue, spotColor = equippedColor ?: NeonBlue)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("START UNIVERSE", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    MenuBtnSmall("MARKET", Icons.Default.Storefront, NeonPink, Modifier.weight(1f), onOpenShop)
                    Spacer(modifier = Modifier.width(16.dp))
                    MenuBtnSmall("RANK", Icons.Default.EmojiEvents, DeepPurple, Modifier.weight(1f), onOpenLeaderboard)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { uriHandler.openUri("https://tetraverse.vercel.app/privacy.html") },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.4f))
                ) {
                    Text("PRIVACY", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                
                Text("|", color = Color.White.copy(alpha = 0.2f), fontSize = 10.sp)

                TextButton(
                    onClick = { uriHandler.openUri("https://tetraverse.vercel.app/terms.html") },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.4f))
                ) {
                    Text("TERMS", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            if (appVersion.isNotEmpty()) {
                Text(
                    text = appVersion,
                    color = Color.White.copy(alpha = 0.15f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SYNCHRONIZING WITH SOLANA...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MenuBtnSmall(text: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, 
        modifier = modifier.height(56.dp), 
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.9f)), 
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(6.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
    }
}

@Composable
fun TopHUD(
    onWallet: () -> Unit, 
    isConnected: Boolean, 
    address: String, 
    latestSignature: String,
    onProfile: () -> Unit, 
    avatarId: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onProfile,
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(NeonBlue).border(1.dp, Color.White, CircleShape)) {
                        val avatarIcon = avatarList.getOrElse(avatarId) { avatarList[0] }
                        Icon(avatarIcon, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        if (isConnected) {
                            Text(address.take(4) + "..." + address.takeLast(4), color = NeonYellow, fontSize = 9.sp)
                        } else {
                            Text("GUEST_USER", color = NeonYellow, fontSize = 9.sp)
                        }
                    }
                }
            }

            Surface(
                onClick = onWallet, 
                color = if (isConnected) NeonGreen.copy(0.15f) else Color.White.copy(0.05f), 
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, if (isConnected) NeonGreen.copy(0.5f) else Color.White.copy(0.1f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isConnected) Icons.Default.Link else Icons.Default.AccountBalanceWallet, null, tint = if (isConnected) NeonGreen else Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isConnected) "MAINNET" else "CONNECT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        

    }
}

@Composable
fun CityScape() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val c = Color.White.copy(0.02f)
        drawRect(c, Offset(40f, size.height - 400f), Size(120f, 400f))
        drawRect(c, Offset(200f, size.height - 600f), Size(140f, 600f))
        drawRect(c, Offset(size.width - 240f, size.height - 500f), Size(130f, 500f))
        
        for (i in 0..10) {
            val y = size.height - (i * 40f)
            drawLine(Color.White.copy(0.03f), Offset(0f, y), Offset(size.width, y))
        }
    }
}
