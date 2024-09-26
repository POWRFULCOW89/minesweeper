package me.diegomelo.minesweeper

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt
import kotlin.random.Random

class ShakeListener(val onShake: () -> Unit) : SensorEventListener {

    private var lastShakeTime: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // Detectar la aceleración en los ejes X, Y, Z
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calcular la magnitud del vector de aceleración
        val acceleration = sqrt(x * x + y * y + z * z)

        // Umbral de detección de sacudida (puedes ajustar el valor)
        val shakeThreshold = 12.0f

        // Detección de sacudida si la magnitud excede el umbral
        if (acceleration > shakeThreshold) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTime > 1000) { // Evitar múltiples sacudidas rápidas
                lastShakeTime = currentTime
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeListener: ShakeListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinesweeperApp(onRestartGame = { restartGame() })
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shakeListener = ShakeListener {
            restartGame()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(shakeListener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeListener)
    }

    // Method to restart the game
    private fun restartGame() {
        setContent {
            MinesweeperApp(onRestartGame = { restartGame() })
        }
    }
}

@Composable
fun MinesweeperApp(onRestartGame: () -> Unit) {
    val rows = 20
    val columns = 10
    val mineCount = 10
    var game = remember { MinesweeperGame(rows, columns, mineCount) }

    Box(modifier = Modifier.background(Color.Gray)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (game.isGameOver) {
                Text("Game Over", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            } else if (game.isGameWon) {
                Text(
                    "You Win!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
            } else {
                MinesweeperGrid(game = game, onRestartGame = {
                    game =
                        MinesweeperGame(rows, columns, mineCount) // Reiniciar el estado del juego
                })
            }
        }
    }
}

@Composable
fun MinesweeperGrid(game: MinesweeperGame, onRestartGame: () -> Unit) {
    Column {
        for (row in 0 until game.rows) {
            Row {
                for (col in 0 until game.columns) {
                    CellView(
                        cell = game.grid[row][col],
                        onClick = { game.revealCell(row, col) },
                        onFlag = { game.toggleFlag(row, col) }
                    )
                }
            }
        }
    }
}

@Composable
fun CellView(cell: Cell, onClick: () -> Unit, onFlag: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(if (cell.isRevealed.value) Color.Gray else Color.DarkGray)
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onFlag() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isRevealed.value && cell.isMine -> Text(
                "💣",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            cell.isRevealed.value && cell.adjacentMines.value > 0 -> Text(
                "${cell.adjacentMines.value}",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            cell.isFlagged.value -> Text("🚩", fontSize = 16.sp, textAlign = TextAlign.Center)
            else -> Text(
                "",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ) // Handle unrevealed cells
        }
    }
}

class MinesweeperGame(val rows: Int, val columns: Int, val mineCount: Int) {
    val grid: Array<Array<Cell>> = Array(rows) { Array(columns) { Cell() } }
    var isGameOver by mutableStateOf(false)
    var isGameWon by mutableStateOf(false)

    init {
        placeMines()
        calculateAdjacentMines()
    }

    private fun placeMines() {
        var placedMines = 0
        while (placedMines < mineCount) {
            val row = Random.nextInt(rows)
            val column = Random.nextInt(columns)
            if (!grid[row][column].isMine) {
                grid[row][column].isMine = true
                placedMines++
            }
        }
    }

    private fun calculateAdjacentMines() {
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                if (!grid[row][column].isMine) {
                    grid[row][column].adjacentMines.value =
                        getAdjacentCells(row, column).count { it.isMine }
                }
            }
        }
    }

    private fun getAdjacentCells(row: Int, column: Int): List<Cell> {
        val adjacentCells = mutableListOf<Cell>()
        for (r in (row - 1)..(row + 1)) {
            for (c in (column - 1)..(column + 1)) {
                if (r in 0 until rows && c in 0 until columns && (r != row || c != column)) {
                    adjacentCells.add(grid[r][c])
                }
            }
        }
        return adjacentCells
    }

    fun revealCell(row: Int, column: Int) {
        val cell = grid[row][column]
        if (cell.isRevealed.value || cell.isFlagged.value || isGameOver) return

        // Reveal the current cell
        cell.isRevealed.value = true

        // Check if the cell is a mine
        if (cell.isMine) {
            isGameOver = true
        } else if (cell.adjacentMines.value == 0) {
            // Reveal adjacent cells if no adjacent mines
            getAdjacentCells(row, column).forEach { adjacentCell ->
                val adjacentRow = grid.indexOfFirst { rowCells -> rowCells.contains(adjacentCell) }
                val adjacentColumn = grid[adjacentRow].indexOf(adjacentCell)
                revealCell(adjacentRow, adjacentColumn)
            }
        }

        checkForWin()
    }

    fun toggleFlag(row: Int, column: Int) {
        val cell = grid[row][column]
        if (!cell.isRevealed.value) {
            cell.isFlagged.value = !cell.isFlagged.value
        }
    }

    private fun checkForWin() {
        if (grid.all { row -> row.all { it.isRevealed.value || it.isMine } }) {
            isGameWon = true
        }
    }
}

data class Cell(
    var isMine: Boolean = false,
    var isRevealed: MutableState<Boolean> = mutableStateOf(false),
    var isFlagged: MutableState<Boolean> = mutableStateOf(false),
    var adjacentMines: MutableState<Int> = mutableIntStateOf(0)
)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MinesweeperApp(onRestartGame = {})
}
