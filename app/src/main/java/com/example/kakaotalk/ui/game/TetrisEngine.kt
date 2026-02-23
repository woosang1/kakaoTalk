package com.example.kakaotalk.ui.game

import kotlin.random.Random

class TetrisEngine {

    private val board = Array(ROWS) { IntArray(COLS) }
    private var currentType = 0
    private var currentRotation = 0
    private var currentRow = 0
    private var currentCol = 0
    private var nextType = Random.nextInt(PIECE_COUNT)

    var score = 0
        private set
    var linesCleared = 0
        private set
    var level = 1
        private set
    var isGameOver = false
        private set

    init {
        spawnPiece()
    }

    fun tick(): Boolean {
        if (isGameOver) return false
        if (canPlace(currentRow + 1, currentCol, currentType, currentRotation)) {
            currentRow++
            return true
        }
        lockPiece()
        clearLines()
        spawnPiece()
        return true
    }

    fun moveLeft(): Boolean {
        if (isGameOver) return false
        if (canPlace(currentRow, currentCol - 1, currentType, currentRotation)) {
            currentCol--
            return true
        }
        return false
    }

    fun moveRight(): Boolean {
        if (isGameOver) return false
        if (canPlace(currentRow, currentCol + 1, currentType, currentRotation)) {
            currentCol++
            return true
        }
        return false
    }

    fun moveDown(): Boolean {
        if (isGameOver) return false
        if (canPlace(currentRow + 1, currentCol, currentType, currentRotation)) {
            currentRow++
            score += 1
            return true
        }
        return false
    }

    fun rotate(): Boolean {
        if (isGameOver) return false
        val newRotation = (currentRotation + 1) % 4
        if (canPlace(currentRow, currentCol, currentType, newRotation)) {
            currentRotation = newRotation
            return true
        }
        // Wall kick: try shifting left or right
        for (offset in intArrayOf(-1, 1, -2, 2)) {
            if (canPlace(currentRow, currentCol + offset, currentType, newRotation)) {
                currentCol += offset
                currentRotation = newRotation
                return true
            }
        }
        return false
    }

    fun hardDrop(): Boolean {
        if (isGameOver) return false
        while (canPlace(currentRow + 1, currentCol, currentType, currentRotation)) {
            currentRow++
            score += 2
        }
        lockPiece()
        clearLines()
        spawnPiece()
        return true
    }

    fun getBoardWithPiece(): Array<IntArray> {
        val result = Array(ROWS) { board[it].copyOf() }
        val shape = SHAPES[currentType][currentRotation]
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val br = currentRow + r
                    val bc = currentCol + c
                    if (br in 0 until ROWS && bc in 0 until COLS) {
                        result[br][bc] = currentType + 1
                    }
                }
            }
        }
        return result
    }

    fun reset() {
        for (r in 0 until ROWS) board[r].fill(0)
        score = 0
        linesCleared = 0
        level = 1
        isGameOver = false
        nextType = Random.nextInt(PIECE_COUNT)
        spawnPiece()
    }

    fun serializeBoard(): String {
        val boardWithPiece = getBoardWithPiece()
        val sb = StringBuilder(ROWS * COLS + 20)
        for (row in boardWithPiece) {
            for (cell in row) sb.append(cell)
        }
        sb.append('|').append(score)
        sb.append('|').append(linesCleared)
        sb.append('|').append(level)
        sb.append('|').append(isGameOver)
        return sb.toString()
    }

    private fun spawnPiece() {
        currentType = nextType
        nextType = Random.nextInt(PIECE_COUNT)
        currentRotation = 0
        val shape = SHAPES[currentType][0]
        currentRow = 0
        currentCol = (COLS - shape[0].size) / 2
        if (!canPlace(currentRow, currentCol, currentType, currentRotation)) {
            isGameOver = true
        }
    }

    private fun canPlace(row: Int, col: Int, type: Int, rotation: Int): Boolean {
        val shape = SHAPES[type][rotation]
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val br = row + r
                    val bc = col + c
                    if (br < 0 || br >= ROWS || bc < 0 || bc >= COLS) return false
                    if (board[br][bc] != 0) return false
                }
            }
        }
        return true
    }

    private fun lockPiece() {
        val shape = SHAPES[currentType][currentRotation]
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val br = currentRow + r
                    val bc = currentCol + c
                    if (br in 0 until ROWS && bc in 0 until COLS) {
                        board[br][bc] = currentType + 1
                    }
                }
            }
        }
    }

    private fun clearLines() {
        var cleared = 0
        var row = ROWS - 1
        while (row >= 0) {
            if (board[row].all { it != 0 }) {
                for (r in row downTo 1) {
                    board[r] = board[r - 1].copyOf()
                }
                board[0] = IntArray(COLS)
                cleared++
            } else {
                row--
            }
        }
        if (cleared > 0) {
            linesCleared += cleared
            score += when (cleared) {
                1 -> 100 * level
                2 -> 300 * level
                3 -> 500 * level
                4 -> 800 * level
                else -> 0
            }
            level = linesCleared / 10 + 1
        }
    }

    companion object {
        const val ROWS = 20
        const val COLS = 10
        const val PIECE_COUNT = 7

        // Standard SRS shapes: SHAPES[pieceType][rotation] = 2D matrix
        val SHAPES: Array<Array<Array<IntArray>>> = arrayOf(
            // 0: I piece (4×4)
            arrayOf(
                arrayOf(intArrayOf(0,0,0,0), intArrayOf(1,1,1,1), intArrayOf(0,0,0,0), intArrayOf(0,0,0,0)),
                arrayOf(intArrayOf(0,0,1,0), intArrayOf(0,0,1,0), intArrayOf(0,0,1,0), intArrayOf(0,0,1,0)),
                arrayOf(intArrayOf(0,0,0,0), intArrayOf(0,0,0,0), intArrayOf(1,1,1,1), intArrayOf(0,0,0,0)),
                arrayOf(intArrayOf(0,1,0,0), intArrayOf(0,1,0,0), intArrayOf(0,1,0,0), intArrayOf(0,1,0,0))
            ),
            // 1: O piece (2×2)
            arrayOf(
                arrayOf(intArrayOf(1,1), intArrayOf(1,1)),
                arrayOf(intArrayOf(1,1), intArrayOf(1,1)),
                arrayOf(intArrayOf(1,1), intArrayOf(1,1)),
                arrayOf(intArrayOf(1,1), intArrayOf(1,1))
            ),
            // 2: T piece (3×3)
            arrayOf(
                arrayOf(intArrayOf(0,1,0), intArrayOf(1,1,1), intArrayOf(0,0,0)),
                arrayOf(intArrayOf(0,1,0), intArrayOf(0,1,1), intArrayOf(0,1,0)),
                arrayOf(intArrayOf(0,0,0), intArrayOf(1,1,1), intArrayOf(0,1,0)),
                arrayOf(intArrayOf(0,1,0), intArrayOf(1,1,0), intArrayOf(0,1,0))
            ),
            // 3: S piece (3×3)
            arrayOf(
                arrayOf(intArrayOf(0,1,1), intArrayOf(1,1,0), intArrayOf(0,0,0)),
                arrayOf(intArrayOf(0,1,0), intArrayOf(0,1,1), intArrayOf(0,0,1)),
                arrayOf(intArrayOf(0,0,0), intArrayOf(0,1,1), intArrayOf(1,1,0)),
                arrayOf(intArrayOf(1,0,0), intArrayOf(1,1,0), intArrayOf(0,1,0))
            ),
            // 4: Z piece (3×3)
            arrayOf(
                arrayOf(intArrayOf(1,1,0), intArrayOf(0,1,1), intArrayOf(0,0,0)),
                arrayOf(intArrayOf(0,0,1), intArrayOf(0,1,1), intArrayOf(0,1,0)),
                arrayOf(intArrayOf(0,0,0), intArrayOf(1,1,0), intArrayOf(0,1,1)),
                arrayOf(intArrayOf(0,1,0), intArrayOf(1,1,0), intArrayOf(1,0,0))
            ),
            // 5: J piece (3×3)
            arrayOf(
                arrayOf(intArrayOf(1,0,0), intArrayOf(1,1,1), intArrayOf(0,0,0)),
                arrayOf(intArrayOf(0,1,1), intArrayOf(0,1,0), intArrayOf(0,1,0)),
                arrayOf(intArrayOf(0,0,0), intArrayOf(1,1,1), intArrayOf(0,0,1)),
                arrayOf(intArrayOf(0,1,0), intArrayOf(0,1,0), intArrayOf(1,1,0))
            ),
            // 6: L piece (3×3)
            arrayOf(
                arrayOf(intArrayOf(0,0,1), intArrayOf(1,1,1), intArrayOf(0,0,0)),
                arrayOf(intArrayOf(0,1,0), intArrayOf(0,1,0), intArrayOf(0,1,1)),
                arrayOf(intArrayOf(0,0,0), intArrayOf(1,1,1), intArrayOf(1,0,0)),
                arrayOf(intArrayOf(1,1,0), intArrayOf(0,1,0), intArrayOf(0,1,0))
            )
        )

        fun deserializeBoard(data: String): DeserializedState {
            val parts = data.split("|")
            val boardStr = parts[0]
            val score = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val lines = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val level = parts.getOrNull(3)?.toIntOrNull() ?: 1
            val gameOver = parts.getOrNull(4)?.toBooleanStrictOrNull() ?: false

            val board = Array(ROWS) { row ->
                IntArray(COLS) { col ->
                    val idx = row * COLS + col
                    if (idx < boardStr.length) boardStr[idx].digitToInt() else 0
                }
            }
            return DeserializedState(board, score, lines, level, gameOver)
        }
    }

    data class DeserializedState(
        val board: Array<IntArray>,
        val score: Int,
        val linesCleared: Int,
        val level: Int,
        val isGameOver: Boolean
    )
}
