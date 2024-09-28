package me.diegomelo.minesweeper

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    private var columns by mutableStateOf(10)
    private var mineCount by mutableStateOf(10)
    private var rows by mutableStateOf(10)
    private lateinit var configActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                run {
                    Log.d("MainActivity", "onActivityResult: result=$result")

                    if (result.resultCode == RESULT_OK) {
                        result.data?.let {
                            val newRows = it.getIntExtra("rows", 10)
                            val newColumns = it.getIntExtra("columns", 10)
                            val newMineCount = it.getIntExtra("mineCount", 10)

                            // Save the new parameters to SharedPreferences
//                        preferencesManager.saveGameParameters(rows, columns, mineCount)

                            rows = newRows
                            columns = newColumns
                            mineCount = newMineCount


                            restartGame()
                        }
                        // Restart the game with new parameters
                    }
                }
            }

        setContent {
            MinesweeperApp(
                rows = rows,
                columns = columns,
                mineCount = mineCount,
                onConfigClick = { openConfigActivity() },
                onRestartGame = { restartGame() }
            )
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shakeListener = ShakeListener {
            restartGame()
            Toast.makeText(this, "Game reset!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restartGame() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)



        setContent {
            MinesweeperApp(
                rows = rows,
                columns = columns,
                mineCount = mineCount,
                onConfigClick = { openConfigActivity() },
                onRestartGame = { restartGame() }
            )
            // Restart by resetting the MinesweeperApp content
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

    // Function to open ConfigActivity for configuring the game
    private fun openConfigActivity() {
        val intent = Intent(this, ConfigActivity::class.java)

        configActivityLauncher.launch(intent)
//        startActivity(intent)
    }


    // Handling result from ConfigActivity
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("MainActivity", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {

            Log.d("MainActivity", "onActivityResult: data=$data")

            data?.let {
                val newRows = it.getIntExtra("rows", 10)
                val newColumns = it.getIntExtra("columns", 10)
                val newMineCount = it.getIntExtra("mineCount", 10)

                rows = newRows
                columns = newColumns
                mineCount = newMineCount

                restartGame()
            }
            // Restart the game with new parameters
        }
    }


    companion object {
        const val CONFIG_REQUEST_CODE = 1
    }
}

@Composable
fun MinesweeperApp(
    rows: Int,
    columns: Int,
    mineCount: Int,
    onConfigClick: () -> Unit,
    onRestartGame: () -> Unit
) {
    val game = remember { MinesweeperGame(rows, columns, mineCount) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    if (game.isGameOver || game.isGameWon) {
        dialogMessage = if (game.isGameOver) "Game Over" else "You Win!"
        // Check for game over or win state
        showDialog = true
    }

    Column(
        modifier = Modifier
            .background(Color.Gray)
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onConfigClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray) // Make button background transparent
        ) {
            // Optional padding
            Text(
                text = "Settings",
                color = Color.White // Text color
            )
        }



        MinesweeperGrid(game = game, Modifier.weight(1f))

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                },
                title = { Text(dialogMessage) },
                text = { Text("Shake to restart!") },
                confirmButton = {
                    Button(onClick = {
                        onRestartGame()
                        showDialog = false // Close dialog and restart game
                    }) {
                        Text("Restart Game")
                    }
                },
                modifier = Modifier.fillMaxWidth(0.9f),
                dismissButton = null // Optionally add a dismiss button
            )
        }
        showDialog = false // Close dialog when dismissed

    }
}

@Composable
fun MinesweeperGrid(game: MinesweeperGame, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
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
            .border(1.dp, Color.Gray)
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onFlag() }
                )
            }
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isRevealed.value && cell.isMine -> Text(
                "💣",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            cell.isRevealed.value && cell.adjacentMines > 0 -> Text(
                "${cell.adjacentMines}",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            cell.isFlagged.value -> Text("🚩", fontSize = 16.sp, textAlign = TextAlign.Center)
            else -> Text("", fontSize = 16.sp, textAlign = TextAlign.Center)
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
                    grid[row][column].adjacentMines =
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

        cell.isRevealed.value = true
        if (cell.isMine) {
            isGameOver = true
        } else if (cell.adjacentMines == 0) {
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
    var adjacentMines: Int = 0
)

@Composable
fun GameOverModal(isGameOver: Boolean, isGameWon: Boolean, onDismiss: () -> Unit) {
    if (isGameOver) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background
                .wrapContentSize(Alignment.Center)
        ) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isGameWon) "You Win!" else "Game Over",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGameWon) Color.Green else Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) {
                        Text("Restart Game")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MinesweeperApp(
        rows = 10,
        columns = 10,
        mineCount = 10,
        onConfigClick = {},
        onRestartGame = {}
    )
}

@Preview(showBackground = true)
@Composable
fun GameOverModalPreview() {
    GameOverModal(isGameOver = true, isGameWon = false, onDismiss = {})
}

