package me.diegomelo.minesweeper

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(context: Context, onSaveSettings: (Int, Int, Int) -> Unit, onCancel: () -> Unit) {
    var rows by remember { mutableIntStateOf(10) }
    var columns by remember { mutableIntStateOf(10) }
    var mineCount by remember { mutableIntStateOf(10) }

    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(16.dp)
            .background(Color.Gray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = rows.toString(),
            onValueChange = { newValue ->
                rows = newValue.toIntOrNull() ?: rows
            },
            label = { Text("Rows") },
            modifier = Modifier,
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.LightGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = columns.toString(),
            onValueChange = { newValue ->
                columns = newValue.toIntOrNull() ?: columns
            },
            label = { Text("Columns") },
            modifier = Modifier,
            shape = RoundedCornerShape(15.dp),

            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.LightGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = mineCount.toString(),
            onValueChange = { newValue ->
                mineCount = newValue.toIntOrNull() ?: mineCount
            },
            label = { Text("Mines") },
            modifier = Modifier,
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.LightGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(colors = ButtonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White,
                disabledContentColor = Color.White,
                disabledContainerColor = Color.DarkGray
            ), onClick = {
                Log.d("ConfigActivity", "rows: $rows, columns: $columns, mineCount: $mineCount")
                onSaveSettings(rows, columns, mineCount)
            }) {
                Text("Save")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                colors = ButtonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White,
                    disabledContentColor = Color.White,
                    disabledContainerColor = Color.DarkGray
                ), onClick = onCancel
            ) {
                Text("Cancel")
            }
        }
    }
}


//@Composable
//@Preview
//fun ConfigScreenPreview() {
//    ConfigScreen(Context(),  onSaveSettings = { _, _, _ -> }, onCancel = {})
//}