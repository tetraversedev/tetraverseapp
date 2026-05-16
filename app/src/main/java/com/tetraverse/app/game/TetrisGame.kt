package com.tetraverse.app.game

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.tetraverse.app.ui.theme.*

data class Point(val x: Int, val y: Int)

enum class PieceType(val blocks: List<Point>, val color: Color) {
    I(listOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(3, 1)), TetrisI),
    J(listOf(Point(0, 0), Point(0, 1), Point(1, 1), Point(2, 1)), TetrisJ),
    L(listOf(Point(2, 0), Point(0, 1), Point(1, 1), Point(2, 1)), TetrisL),
    O(listOf(Point(1, 0), Point(2, 0), Point(1, 1), Point(2, 1)), TetrisO),
    S(listOf(Point(1, 0), Point(2, 0), Point(0, 1), Point(1, 1)), TetrisS),
    T(listOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(2, 1)), TetrisT),
    Z(listOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(2, 1)), TetrisZ)
}

class TetrisPiece(val type: PieceType, initialPos: Point = Point(3, 0)) {
    var blocks by mutableStateOf(type.blocks)
    var position by mutableStateOf(initialPos)
    
    fun rotate() {
        // Standard rotation matrix
        blocks = blocks.map { Point(-it.y + 1, it.x) }
    }
}

class TetrisEngine(private val onSaveScore: (Int) -> Unit = {}) {
    val grid = mutableStateListOf<MutableList<Color?>>()
    
    var currentPiece by mutableStateOf<TetrisPiece?>(null)
    var nextPieceType by mutableStateOf(PieceType.entries.random())
    var holdPieceType by mutableStateOf<PieceType?>(null)
    var canHold by mutableStateOf(true)
    
    var score by mutableIntStateOf(0)
    var level by mutableIntStateOf(1)
    var isGameOver by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var linesCleared by mutableIntStateOf(0)
    var highScore by mutableIntStateOf(0)

    private var pieceBag = mutableListOf<PieceType>()
    private var lockDelayTicks = 0

    var onLineCleared: (() -> Unit)? = null
    var onGameOver: (() -> Unit)? = null

    init {
        reset()
    }

    private fun generateBag() {
        val newBag = PieceType.entries.toMutableList()
        newBag.shuffle()
        pieceBag.addAll(newBag)
    }

    private fun pullFromBag(): PieceType {
        if (pieceBag.size < 7) generateBag()
        return pieceBag.removeAt(0)
    }

    fun spawnPiece() {
        currentPiece = TetrisPiece(nextPieceType)
        nextPieceType = pullFromBag()
        canHold = true
        lockDelayTicks = 0
        if (checkCollision(currentPiece!!, currentPiece!!.position)) {
            isGameOver = true
            // Save final score on GameOver
            if (score > highScore) {
                highScore = score
            }
            // Always trigger save score to ensure sync with leaderboard if higher than previous local high
            onSaveScore(highScore.coerceAtLeast(score))
            onGameOver?.invoke()
        }
    }

    fun hold() {
        if (!canHold || isGameOver || isPaused) return
        val current = currentPiece ?: return
        val temp = holdPieceType
        holdPieceType = current.type
        if (temp == null) {
            spawnPiece()
        } else {
            currentPiece = TetrisPiece(temp)
            canHold = false
        }
    }

    fun tick() {
        if (isGameOver || isPaused) return
        val piece = currentPiece ?: return
        
        if (checkCollision(piece, Point(piece.position.x, piece.position.y + 1))) {
            lockDelayTicks++
            if (lockDelayTicks >= 2) lockPiece()
        } else {
            lockDelayTicks = 0
            piece.position = Point(piece.position.x, piece.position.y + 1)
        }
    }

    fun move(dx: Int, dy: Int): Boolean {
        if (isGameOver || isPaused) return false
        val piece = currentPiece ?: return false
        val newPos = Point(piece.position.x + dx, piece.position.y + dy)
        
        if (!checkCollision(piece, newPos)) {
            piece.position = newPos
            return true
        } else if (dy > 0 && dx == 0) {
            lockPiece()
            return false
        }
        return false
    }

    fun hardDrop() {
        if (isGameOver || isPaused) return
        val current = currentPiece ?: return
        var ghostY = current.position.y
        while (!checkCollision(current, Point(current.position.x, ghostY + 1))) {
            ghostY++
        }
        current.position = Point(current.position.x, ghostY)
        lockPiece()
    }

    fun rotate() {
        if (isGameOver || isPaused) return
        val piece = currentPiece ?: return
        val oldBlocks = piece.blocks
        piece.rotate()
        
        // Simple SRS (Super Rotation System) subset
        val kicks = listOf(Point(0, 0), Point(-1, 0), Point(1, 0), Point(0, -1), Point(-1, -1), Point(1, -1))
        var success = false
        for (kick in kicks) {
            val kickedPos = Point(piece.position.x + kick.x, piece.position.y + kick.y)
            if (!checkCollision(piece, kickedPos)) {
                piece.position = kickedPos
                success = true
                break
            }
        }
        if (!success) piece.blocks = oldBlocks
    }

    private fun checkCollision(piece: TetrisPiece, pos: Point): Boolean {
        return piece.blocks.any { block ->
            val gx = block.x + pos.x
            val gy = block.y + pos.y
            gx !in 0..9 || gy >= 20 || (gy >= 0 && grid[gy][gx] != null)
        }
    }

    private fun lockPiece() {
        val piece = currentPiece ?: return
        piece.blocks.forEach { block ->
            val gx = block.x + piece.position.x
            val gy = block.y + piece.position.y
            if (gy in 0..19 && gx in 0..9) {
                grid[gy][gx] = piece.type.color
            }
        }
        clearLines()
        spawnPiece()
    }

    private fun clearLines() {
        var linesCount = 0
        val fullRows = mutableListOf<Int>()
        for (y in 0 until 20) {
            if (grid[y].all { it != null }) fullRows.add(y)
        }

        if (fullRows.isNotEmpty()) {
            fullRows.forEach { y ->
                grid.removeAt(y)
                grid.add(0, MutableList(10) { null })
            }
            linesCount = fullRows.size
            linesCleared += linesCount
            score += when(linesCount) {
                1 -> 100 * level
                2 -> 300 * level
                3 -> 500 * level
                4 -> 800 * level
                else -> 0
            }
            level = (linesCleared / 10) + 1
            if (score > highScore) {
                highScore = score
                onSaveScore(highScore)
            }
            onLineCleared?.invoke()
        }
    }

    fun reset() {
        grid.clear()
        repeat(20) { grid.add(MutableList(10) { null }) }
        score = 0
        level = 1
        linesCleared = 0
        isGameOver = false
        isPaused = false
        pieceBag.clear()
        generateBag()
        holdPieceType = null
        nextPieceType = pullFromBag()
        spawnPiece()
    }
}
