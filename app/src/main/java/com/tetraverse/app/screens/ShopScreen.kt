package com.tetraverse.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetraverse.app.game.Skin
import com.tetraverse.app.game.premiumSkins
import com.tetraverse.app.ui.theme.*

@Composable
fun ShopScreen(
    currentAvatarId: Int,
    ownedSkinIds: Set<Int>,
    equippedSkinId: Int,
    onAvatarSelected: (Int) -> Unit,
    onEquipSkin: (Int) -> Unit,
    onBack: () -> Unit, 
    onPurchase: (Skin) -> Unit,
    isLoading: Boolean = false
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Skins, 1: Avatars
    var selectedSkin by remember { mutableStateOf<Skin?>(premiumSkins[0]) }
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = ""
    )

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text("NEON MARKET", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(SurfaceDark)) {
                TabItem("SKINS", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                TabItem("AVATARS", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 0) {
                // Featured Deal
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp).border(2.dp, NeonPink.copy(alpha = glowAlpha), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).background(NeonPink, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("LIMITED OFFER", color = NeonPink, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                            Text("CYBER BUNDLE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                selectedSkin?.let { skin -> SkinShowcase(skin) }

                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(premiumSkins) { skin ->
                        SkinThumb(
                            skin, 
                            isSelected = selectedSkin == skin,
                            isOwned = ownedSkinIds.contains(skin.id),
                            isEquipped = equippedSkinId == skin.id
                        ) { selectedSkin = skin }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                selectedSkin?.let { skin ->
                    val isOwned = ownedSkinIds.contains(skin.id)
                    val isEquipped = equippedSkinId == skin.id
                    
                    Button(
                        onClick = { 
                            if (isOwned) {
                                if (!isEquipped) onEquipSkin(skin.id)
                            } else {
                                onPurchase(skin)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(60.dp).shadow(12.dp, RoundedCornerShape(20.dp), spotColor = if (skin.id == 5) NeonYellow else NeonBlue),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isEquipped -> Color.Gray
                                isOwned -> NeonGreen
                                skin.id == 5 -> DeepPurple
                                else -> NeonBlue
                            }
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isOwned) Icons.Default.CheckCircle else Icons.Default.ShoppingBag, null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when {
                                    isEquipped -> "EQUIPPED"
                                    isOwned -> "USE SKIN"
                                    else -> "GET ${skin.name} - ${skin.price}"
                                },
                                fontWeight = FontWeight.Black, 
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // Avatars Section
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(avatarList.size) { id ->
                        AvatarThumb(id, isSelected = currentAvatarId == id) { onAvatarSelected(id) }
                    }
                }
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
                    Text("WAITING FOR WALLET...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) NeonBlue else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else Color.White.copy(0.4f), fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
fun AvatarThumb(id: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) NeonBlue.copy(0.2f) else SurfaceDark)
            .border(2.dp, if (isSelected) NeonBlue else Color.White.copy(0.1f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(avatarList[id], null, tint = Color.White, modifier = Modifier.size(40.dp))
        if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = NeonBlue, modifier = Modifier.align(Alignment.TopEnd).size(20.dp).padding(4.dp))
    }
}

@Composable
fun SkinShowcase(skin: Skin) {
    Box(
        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(SurfaceDark, DarkBg)))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row { BlockUnit3D(skin.colors) }
            Row { BlockUnit3D(skin.colors); BlockUnit3D(skin.colors); BlockUnit3D(skin.colors) }
            Spacer(modifier = Modifier.height(16.dp))
            Text(skin.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun BlockUnit3D(colors: List<Color>) {
    Canvas(modifier = Modifier.size(40.dp).padding(4.dp)) {
        val rect = Size(size.width, size.height)
        
        // Base Gradient
        val brush = if (colors.size > 1) {
            Brush.linearGradient(colors = colors)
        } else {
            Brush.linearGradient(listOf(colors[0], colors[0]))
        }
        
        drawRoundRect(brush = brush, size = rect, cornerRadius = CornerRadius(6.dp.toPx()))

        // Glossy Overlay
        drawRoundRect(
            brush = Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = 0.4f),
                0.4f to Color.White.copy(alpha = 0.1f),
                0.5f to Color.Transparent,
                1.0f to Color.Black.copy(alpha = 0.2f)
            ),
            size = rect,
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Bevels
        drawPath(path = Path().apply {
            moveTo(0f, size.height); lineTo(0f, 0f); lineTo(size.width, 0f)
            lineTo(size.width - 6f, 6f); lineTo(6f, 6f); lineTo(6f, size.height - 6f); close()
        }, color = Color.White.copy(alpha = 0.3f))
        drawPath(path = Path().apply {
            moveTo(size.width, 0f); lineTo(size.width, size.height); lineTo(0f, size.height)
            lineTo(6f, size.height - 6f); lineTo(size.width - 6f, size.height - 6f); lineTo(size.width - 6f, 6f); close()
        }, color = Color.Black.copy(alpha = 0.3f))
    }
}

@Composable
fun SkinThumb(skin: Skin, isSelected: Boolean, isOwned: Boolean, isEquipped: Boolean, onClick: () -> Unit) {
    val mainColor = skin.colors[0]
    Box(
        modifier = Modifier.aspectRatio(0.9f).clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) mainColor.copy(0.15f) else SurfaceDark)
            .border(2.dp, if (isSelected) mainColor else if (isOwned) mainColor.copy(0.3f) else Color.White.copy(0.05f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))
                .background(if (skin.colors.size > 1) Brush.linearGradient(skin.colors) else Brush.linearGradient(listOf(mainColor, mainColor)))
                .border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(6.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Text(skin.name.split(" ").first(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(if (isOwned) "OWNED" else skin.price, color = if (isOwned) NeonGreen else NeonYellow, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        if (isEquipped) Icon(Icons.Default.CheckCircle, null, tint = NeonBlue, modifier = Modifier.align(Alignment.TopEnd).size(16.dp))
        else if (isOwned) Icon(Icons.Default.CheckCircle, null, tint = mainColor.copy(0.5f), modifier = Modifier.align(Alignment.TopEnd).size(16.dp))
    }
}
