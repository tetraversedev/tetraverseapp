package com.tetraverse.app.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetraverse.app.game.AudioManager
import com.tetraverse.app.game.TetrisEngine
import com.tetraverse.app.game.PieceType
import com.tetraverse.app.game.LeaderboardManager
import com.tetraverse.app.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun GameScreen(
    audioManager: AudioManager,
    overrideColors: List<Color>? = null,
    walletAddress: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("tetra_prefs", android.content.Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    
    val engine = remember { 
        TetrisEngine { finalScore ->
            // Always try to submit to leaderboard on save, comparison happens in Manager
            if (walletAddress.isNotEmpty()) {
                scope.launch {
                    LeaderboardManager.submitScore(walletAddress, finalScore)
                }
            }
            // Still update local prefs for offline display
            val localHigh = prefs.getInt("high_score", 0)
            if (finalScore > localHigh) {
                prefs.edit().putInt("high_score", finalScore).apply()
            }
        }.apply {
            highScore = prefs.getInt("high_score", 0)
        }
    }

    LaunchedEffect(Unit) {
        // audioManager.playInGameMusic()
        engine.onLineCleared = { 
            audioManager.playClear()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        engine.onGameOver = { 
            // audioManager.playGameOverMusic()
            audioManager.playGameOverSfx()
        }
    }

    LaunchedEffect(engine.isGameOver, engine.isPaused) {
        while (!engine.isGameOver && !engine.isPaused) {
            val tickTime = (800 - (engine.level * 70L)).coerceAtLeast(80L)
            delay(tickTime)
            engine.tick()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        val isLandscape = maxWidth > maxHeight
        
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.size(300.dp).align(Alignment.TopStart).offset((-100).dp, (-100).dp).blur(80.dp).background(NeonBlue.copy(0.15f), CircleShape))
            Box(modifier = Modifier.size(300.dp).align(Alignment.BottomEnd).offset(100.dp, 100.dp).blur(80.dp).background(NeonPink.copy(0.15f), CircleShape))
        }

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(modifier = Modifier.weight(0.3f), horizontalAlignment = Alignment.CenterHorizontally) {
                    GameHeader(engine)
                    Spacer(modifier = Modifier.height(20.dp))
                    SidePanel(label = "HOLD", pieceType = engine.holdPieceType, overrideColors = overrideColors) { engine.hold() }
                }
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    GameBoardContainer(engine, overrideColors, onBack)
                }
                Column(modifier = Modifier.weight(0.3f), horizontalAlignment = Alignment.CenterHorizontally) {
                    SidePanel(label = "NEXT", pieceType = engine.nextPieceType, overrideColors = overrideColors)
                    Spacer(modifier = Modifier.weight(1f))
                    ElegantConsole(engine, audioManager)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                GameHeader(engine)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        SidePanel(label = "HOLD", pieceType = engine.holdPieceType, overrideColors = overrideColors) { engine.hold() }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("LVL ${engine.level}", color = NeonYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    GameBoardContainer(engine, overrideColors, onBack)
                    Spacer(modifier = Modifier.width(16.dp))
                    SidePanel(label = "NEXT", pieceType = engine.nextPieceType, overrideColors = overrideColors)
                }
                Spacer(modifier = Modifier.height(24.dp))
                ElegantConsole(engine, audioManager)
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onBack) {
                    Text("EXIT UNIVERSE", color = Color.White.copy(0.3f), fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
                }
            }
        }
    }
}

@Composable
fun GameBoardContainer(engine: TetrisEngine, overrideColors: List<Color>? = null, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(0.5f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark.copy(alpha = 0.6f))
            .border(2.dp, Brush.linearGradient(listOf(NeonBlue, NeonPink)), RoundedCornerShape(16.dp))
            .shadow(20.dp, ambientColor = NeonBlue, spotColor = NeonPink)
    ) {
        TetrisBoardCanvas(engine, overrideColors)
        if (engine.isPaused) Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
            Button(onClick = { engine.isPaused = false }, colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)) { Text("RESUME", fontWeight = FontWeight.Black) }
        }
        if (engine.isGameOver) Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(Icons.Default.Celebration, null, tint = NeonYellow, modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("UNIVERSE COLLAPSED", color = NeonPink, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("FINAL SCORE: ${engine.score}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("HIGH SCORE: ${engine.highScore}", color = NeonYellow, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { engine.reset() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("TRY AGAIN", fontWeight = FontWeight.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { onBack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.3f))
                ) {
                    Text("EXIT TO MENU", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                Text("Score synced to Leaderboard", color = NeonBlue, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}


@Composable
fun ElegantConsole(engine: TetrisEngine, audio: AudioManager) {
    val haptic = LocalHapticFeedback.current
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(160.dp).background(Color.White.copy(0.03f), CircleShape).border(1.dp, Color.White.copy(0.1f), CircleShape))
            TactileButton(Icons.Default.KeyboardArrowUp, Modifier.align(Alignment.TopCenter), NeonBlue) {
                engine.rotate(); audio.playRotate(); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            TactileButton(Icons.Default.KeyboardArrowLeft, Modifier.align(Alignment.CenterStart), NeonBlue, isContinuous = true) {
                engine.move(-1, 0); audio.playMove(); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            TactileButton(Icons.Default.KeyboardArrowRight, Modifier.align(Alignment.CenterEnd), NeonBlue, isContinuous = true) {
                engine.move(1, 0); audio.playMove(); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            TactileButton(Icons.Default.KeyboardArrowDown, Modifier.align(Alignment.BottomCenter), NeonBlue, isContinuous = true) {
                engine.move(0, 1); audio.playMove(); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Brush.radialGradient(listOf(NeonPink, NeonPink.copy(0.6f)))).border(4.dp, Color.White.copy(0.3f), CircleShape).clickable { 
                engine.hardDrop(); audio.playMove(); haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }.shadow(15.dp, CircleShape, spotColor = NeonPink), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.KeyboardDoubleArrowDown, null, tint = Color.White, modifier = Modifier.size(36.dp))
                Text("DROP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun TactileButton(icon: ImageVector, modifier: Modifier, glowColor: Color, isContinuous: Boolean = false, onAction: () -> Unit) {
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var isPressed by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.size(54.dp).pointerInput(Unit) {
                detectTapGestures(onPress = {
                        isPressed = true
                        onAction()
                        if (isContinuous) { job = scope.launch { delay(170); while (true) { onAction(); delay(60) } } }
                        tryAwaitRelease()
                        isPressed = false
                        job?.cancel()
                    }
                )
            },
        color = if (isPressed) glowColor.copy(0.4f) else Color.White.copy(0.08f),
        shape = CircleShape,
        border = BorderStroke(1.5.dp, if (isPressed) glowColor else Color.White.copy(0.2f))
    ) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(30.dp)) } }
}

@Composable
fun SidePanel(label: String, pieceType: PieceType?, overrideColors: List<Color>? = null, onClick: (() -> Unit)? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceDark.copy(0.8f)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(14.dp)).then(if (onClick != null) Modifier.clickable { onClick() } else Modifier), contentAlignment = Alignment.Center) {
            pieceType?.let { type ->
                Canvas(modifier = Modifier.size(32.dp)) {
                    val blockSize = size.width / 4
                    val blockColors = overrideColors ?: listOf(type.color)
                    type.blocks.forEach { block -> draw3DBlock(block.x + 1, block.y + 1, blockSize, blockSize, blockColors) }
                }
            }
        }
    }
}

@Composable
fun TetrisBoardCanvas(engine: TetrisEngine, overrideColors: List<Color>? = null) {
    val currentPiece = engine.currentPiece
    val grid = engine.grid
    Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        val w = size.width / 10
        val h = size.height / 20
        for (y in 0 until 20) {
            val row = grid[y]
            for (x in 0 until 10) { row[x]?.let { 
                val colors = overrideColors ?: listOf(it)
                draw3DBlock(x, y, w, h, colors) 
            } }
        }
        currentPiece?.let { p -> p.blocks.forEach { b -> 
            val colors = overrideColors ?: listOf(p.type.color)
            draw3DBlock(p.position.x + b.x, p.position.y + b.y, w, h, colors) 
        } }
    }
}

fun DrawScope.draw3DBlock(x: Int, y: Int, w: Float, h: Float, colors: List<Color>) {
    val rect = Size(w - 2f, h - 2f)
    val left = x * w + 1f
    val top = y * h + 1f
    
    // Base Gradient
    val brush = if (colors.size > 1) {
        Brush.linearGradient(
            colors = colors,
            start = Offset(left, top),
            end = Offset(left + rect.width, top + rect.height)
        )
    } else {
        Brush.linearGradient(listOf(colors[0], colors[0]))
    }

    drawRoundRect(brush = brush, topLeft = Offset(left, top), size = rect, cornerRadius = CornerRadius(4.dp.toPx()))

    // Glossy Overlay
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.0f to Color.White.copy(alpha = 0.4f),
            0.4f to Color.White.copy(alpha = 0.1f),
            0.5f to Color.Transparent,
            1.0f to Color.Black.copy(alpha = 0.2f)
        ),
        topLeft = Offset(left, top),
        size = rect,
        cornerRadius = CornerRadius(4.dp.toPx())
    )

    // Bevels
    drawPath(path = Path().apply {
            moveTo(left, top + rect.height); lineTo(left, top); lineTo(left + rect.width, top)
            lineTo(left + rect.width - 4f, top + 4f); lineTo(left + 4f, top + 4f); lineTo(left + 4f, top + rect.height - 4f); close()
        }, color = Color.White.copy(alpha = 0.3f))
    drawPath(path = Path().apply {
            moveTo(left + rect.width, top); lineTo(left + rect.width, top + rect.height); lineTo(left, top + rect.height)
            lineTo(left + 4f, top + rect.height - 4f); lineTo(left + rect.width - 4f, top + rect.height - 4f); lineTo(left + rect.width - 4f, top + 4f); close()
        }, color = Color.Black.copy(alpha = 0.3f))
}

@Composable
fun GameHeader(engine: TetrisEngine) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("SCORE", color = Color.White.copy(0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(String.format(Locale.US, "%,d", engine.score), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
        Surface(onClick = { engine.isPaused = !engine.isPaused }, modifier = Modifier.size(46.dp), color = Color.White.copy(0.1f), shape = CircleShape, border = BorderStroke(1.dp, Color.White.copy(0.2f))) {
            Box(contentAlignment = Alignment.Center) { Icon(if (engine.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = Color.White) }
        }
    }
}
