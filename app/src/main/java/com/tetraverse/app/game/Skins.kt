package com.tetraverse.app.game

import androidx.compose.ui.graphics.Color
import com.tetraverse.app.ui.theme.*

data class Skin(val id: Int, val name: String, val price: String, val colors: List<Color>)

val premiumSkins = listOf(
    // Common - Single Color
    Skin(1, "CYBER CRYSTAL", "0.001 SOL", listOf(NeonBlue)),
    Skin(2, "NEON PULSE", "0.001 SOL", listOf(NeonPink)),
    
    // Rare - Dual Gradients
    Skin(3, "LAVA CORE", "0.002 SOL", listOf(Color.Red, Color(0xFFFF4500))),
    Skin(4, "EMERALD GHOST", "0.002 SOL", listOf(NeonGreen, Color(0xFF006400))),
    Skin(5, "FROST BITE", "0.002 SOL", listOf(Color(0xFFAFEEEE), Color(0xFF00CED1))),
    Skin(6, "TOXIC WASTE", "0.003 SOL", listOf(Color(0xFFADFF2F), Color(0xFF00FF00))),
    
    // Epic - Glossy Premium Gradients
    Skin(7, "SOLAR FLARE", "0.005 SOL", listOf(Color(0xFFFF8C00), Color(0xFFFFD700), Color(0xFFFF0000))),
    Skin(8, "DEEP OCEAN", "0.005 SOL", listOf(Color(0xFF00008B), Color(0xFF1E90FF), Color(0xFF00FFFF))),
    Skin(9, "ROYAL VIOLET", "0.006 SOL", listOf(Color(0xFF9400D3), Color(0xFFEE82EE), Color(0xFF4B0082))),
    Skin(10, "RUBY GEM", "0.008 SOL", listOf(Color(0xFFDC143C), Color(0xFFFF69B4), Color(0xFF8B0000))),
    
    // Legendary - Ultra Glossy Multi-tone
    Skin(11, "GOLDEN RELIC", "0.015 SOL", listOf(NeonYellow, Color(0xFFFFD700), Color(0xFFDAA520), Color.White)),
    Skin(12, "VOID MIST", "0.025 SOL", listOf(Color.White, Color.Gray, Color.Black, Color(0xFF4B0082)))
)
