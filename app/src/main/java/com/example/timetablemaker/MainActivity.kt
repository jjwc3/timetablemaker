package com.example.timetablemaker

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.widget.Toast
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = viewModel< MainViewModel>()

            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "Main",
            ){
                composable("Main") {
                    Main(
                        modifier = Modifier
                            .background(color = Color(0xFFF6F6F8)),
                        onNavigateToDrawing = {navController.navigate("Drawing")},
                        viewModel = viewModel,
                        finishApp = {
                            this@MainActivity.finish()
                        }
                    )
                }
                composable("Drawing") {
                    Drawing(
                        modifier = Modifier
                            .background(color = Color(0xFFF6F6F8)),
                        onNavigateToMain = {navController.navigate("Main")},
                        viewModel = viewModel,
                    )
                }
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
    modifier: Modifier = Modifier,
    onNavigateToDrawing : () -> Unit,
    viewModel: MainViewModel,
    finishApp: () -> Unit
) {
    Scaffold (
        /** TopBar */
        topBar = {
            TopAppBar(
                title = { Text("기본 설정", fontWeight = FontWeight.Bold)},
                modifier = modifier,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F6F8))
            )
        },
        /** Main Content */
        content = {
            /** 수업 요일 */
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(color = Color(0xFFF6F6F8)
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .background(color = Color.White, shape = RoundedCornerShape(size = 20.dp))
                        .padding(10.dp)
                        ,
                ) {
                    Text(
                        text = "수업 요일",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.size(3.dp))
                    /** 월/일부터 시작 */
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularButton(
                            text = "월",
                            isEnabled = viewModel.dayStartWith.value == 0,
                        ) {
                            if (viewModel.dayStartWith.value == 1) {
                                viewModel.updateDayStartWith(0)
                            }
                        }
                        CircularButton(
                            text = "일",
                            isEnabled = viewModel.dayStartWith.value == 1,
                        ) {
                            if (viewModel.dayStartWith.value == 0) {
                                viewModel.updateDayStartWith(1)
                            }
                        }
                        Text(
                            text = "부터 시작",
                            fontSize = 20.sp,
                        )
                    }
                    Spacer(modifier = Modifier.size(3.dp))
                    /** 월~일 선택 */
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dateList: Array<String> = arrayOf("월", "화", "수", "목", "금", "토", "일")
                        val orderType by viewModel.dayStartWith
                        val adjustedDateList: List<String> = if (orderType == 1) (dateList.takeLast(1) + dateList.dropLast(1)).toList() else dateList.toList()

                        for (day in adjustedDateList) {
                            val originalIndex = dateList.indexOf(day) // 항상 고정된 월화수목금토일 기준 인덱스 찾기

                            CircularButton(
                                text = day,
                                isEnabled = viewModel.daySelected[originalIndex], // 원래 인덱스 참조
                            ) {
                                viewModel.updateDaySelected(index = originalIndex, value = !viewModel.daySelected[originalIndex])
                            }
                        }
//                        for (i in 0..6)
//                            CircularButton(
//                                text = dateList[i],
//                                isEnabled = viewModel.daySelected[i],
//                            ) {
//                                viewModel.updateDaySelected(index = i, value = !viewModel.daySelected[i])
//                            }
                    }
                }
                /** 수업 시간 */
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .background(color = Color.White, shape = RoundedCornerShape(size = 20.dp))
                        .padding(10.dp)
                ) {
                    Text(text = "수업 시간", fontSize = 25.sp, fontWeight = FontWeight.Medium)
                    /** 수업 교시 선택 */
                    Row (
                        modifier = Modifier
                            .height(40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.size(3.dp))
                        Text(
                            text = "1교시 ~ ",
                            fontSize = 20.sp,
                        )
                        NumberTextField(
                            value = viewModel.classEnd.value,
                            width = 50.dp,
                            fontSize = 25.sp
                        ) {
                            changedValue -> run {
                                viewModel.updateClassEnd(changedValue)
                        }
                        }
                        Text(
                            text = " 교시",
                            fontSize = 20.sp,
                        )
                    }
                    /** 수업 시작 시간 */
                    Row (
                        modifier = Modifier
                            .height(40.dp),
                    ) {
                        Text(text="수업 시작 시간: ", fontSize = 20.sp)
                        NumberTextField(
                            value = viewModel.classStartTime[0],
                            width = 70.dp,
                            fontSize = 25.sp
                        ) { changedValue -> run {
                            viewModel.updateClassStartTime(index = 0, value = changedValue)
                        }

                        }
                        Text(text = ":", fontSize = 20.sp)
                        NumberTextField(
                            value = viewModel.classStartTime[1],
                            width = 80.dp,
                            fontSize = 25.sp
                        ) { changedValue -> run {
                            viewModel.updateClassStartTime(index = 1, value = changedValue)
                        } }
                    }
                    /** 수업 시간 */
                    Row(
                        modifier = Modifier
                            .height(40.dp),
                    ) {
                        Text(text="수업 시간: ", fontSize = 20.sp, modifier = Modifier)
                        NumberTextField(
                            value = viewModel.classDuration.value,
                            width = 60.dp,
                            fontSize = 25.sp
                        ) { changedValue -> run {
                            viewModel.updateClassDuration(changedValue)
                        }
                        }
                        Text(text="분", fontSize = 20.sp, modifier = Modifier)
                    }

                }
                /** 점심 시간 */
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .background(color = Color.White, shape = RoundedCornerShape(size = 20.dp))
                        .padding(10.dp)
                ) {
                    /** 점심 시간 여부, 교시 */
                    Row(
                        modifier = Modifier
                            .height(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = viewModel.lunchTime[0].toBoolean(),
                            onCheckedChange = {
                                run {
                                viewModel.updateLunchTime(index = 0, value = it.toString())
                            }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF376FDD),
                                uncheckedColor = Color.Black,
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .padding(10.dp)
                        )
                        Text(text = "점심시간 : ", fontSize = 20.sp)
                        NumberTextField(
                            value = viewModel.lunchTime[1]
                        ) { changedValue -> run {
                            viewModel.updateLunchTime(index = 1, value = changedValue)
                        } }
                        Text(text = " 교시 뒤", fontSize = 20.sp)
                    }
                    /** 점심 시간 시간 */
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                    ) {
                        Spacer(Modifier
                            .width(30.dp)
                        )
                        NumberTextField(
                            value = viewModel.lunchTime[2],
                            width = 60.dp
                        ) { changedValue -> run {
                            viewModel.updateLunchTime(index = 2, value = changedValue)
                        } }
                        Text(text = " 분", fontSize = 20.sp)
                    }

                }
            }
        },
        bottomBar = {
            BottomAppBar(
                modifier = modifier,
                containerColor = Color(0xFFF6F6F8),
                content = {
                    Button(
                        onClick = {
                            finishApp()
                        },
                        shape = RoundedCornerShape(corner = CornerSize(100)),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(20.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent
                        )
                    ) {
                        Text("Exit", color = Color.Black)
                    }
                    Button(
                        onClick = {
                            onNavigateToDrawing()
                        },
                        shape = RoundedCornerShape(corner = CornerSize(100)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent
                        )
                    ) {
                        Text("Next", color = Color.Black)
                    }
                }
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Drawing(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val captureController = rememberCaptureController()
    val uiScope = rememberCoroutineScope()

    Scaffold (
        /** TopBar */
        topBar = {
            TopAppBar(
                title = { Text("시간표 구성", fontWeight = FontWeight.Bold)},
                modifier = modifier,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F6F8))
            )
        },
        /** Main Content */
        content = { paddingValues ->
            val dateList: Array<String> = arrayOf("월", "화", "수", "목", "금", "토", "일")
            val adjustedDateList: List<String> = if (viewModel.dayStartWith.value == 1) (dateList.takeLast(1) + dateList.dropLast(1)).toList() else dateList.toList()

            val rowCount = viewModel.classEnd.value.toInt() + 1
            val columnCount = viewModel.daySelected.count { it } + 1
            val columnProperties = remember {
                Array(rowCount) { Array(columnCount) {
                    mutableStateMapOf("text" to "", "subText" to "", "subTextColor" to Color.Black, "fontSize" to "15", "subFontSize" to "10", "bgColor" to Color(0xFFF2F2F2))
                } }
            }

            val trueList = mutableListOf<String>()
            for (day in adjustedDateList) {
                val originalIndex = dateList.indexOf(day)
                    if ( viewModel.daySelected[originalIndex] ) {
                        trueList.add(day)
                    }
            }
            for (k in 1..<columnCount) {
                columnProperties[0][k]["text"] = trueList[k-1]
                columnProperties[0][k]["fontSize"] = "20"
            }
            val startHour = viewModel.classStartTime[0].toInt()
            val startMinute = viewModel.classStartTime[1].toInt()
            val classDuration = viewModel.classDuration.value.toInt()
            val lunch = viewModel.lunchTime

            for (l in 1..<rowCount) {
                columnProperties[l][0]["text"] = "${l}교시"
                columnProperties[l][0]["subTextColor"] = Color.Black
                columnProperties[l][0]["fontSize"] = "15"
                columnProperties[l][0]["subFontSize"] = "15"

                if (lunch[0] == "true") {
                    if (l <= lunch[1].toInt()) {
                        columnProperties[l][0]["subText"] = timeAdder(
                            firstHour = startHour,
                            firstMinute = startMinute,
                            secondMinute = classDuration,
                            count = l-1
                        )
                    } else {
                        columnProperties[l][0]["subText"] = timeAdder(
                            firstHour = startHour,
                            firstMinute = startMinute,
                            secondMinute = classDuration,
                            count = l-1,
                            lunchMinute = lunch[2].toInt()
                        )
                    }
                } else {
                    columnProperties[l][0]["subText"] = timeAdder(
                        firstHour = startHour,
                        firstMinute = startMinute,
                        secondMinute = classDuration,
                        count = l-1
                    )
                }

            }

            var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
            var showDialog by remember { mutableStateOf(false) }
            var inputText by remember { mutableStateOf("") }
            var inputSubText by remember { mutableStateOf("") }
            var inputFontSize by remember { mutableStateOf("15") }
            var inputSubFontSize by remember { mutableStateOf("10") }
            var selectedColor by remember { mutableStateOf(Color(0xFFF2F2F2)) }


            Box(modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .background(color = Color(0xFFF6F6F8))
                .capturable(captureController),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .wrapContentSize()
                        .background(Color(0xFFF2F2F2))
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(5.dp)
                            .background(color = Color(0xFFF6F6F8))
                            .border(
                                border = BorderStroke(1.dp, Color.Black),
                                shape = RectangleShape
                            )
                            .padding(1.dp)
                    ) {
                        for (i in 0..<rowCount) {
                            Row(
                                modifier = Modifier
                                    .height(50.dp)
                            ) {
                                for ( j in 0..<columnCount) {
                                    TimeTableBoxColumn(
                                        text = columnProperties[i][j].getValue("text") as String,
                                        subText = columnProperties[i][j].getValue("subText") as String,
                                        subTextColor = columnProperties[i][j].getValue("subTextColor") as Color,
                                        fontSize = (columnProperties[i][j].getValue("fontSize") as String).toInt().sp,
                                        subFontSize = (columnProperties[i][j].getValue("subFontSize") as String).toInt().sp,
                                        bgColor = columnProperties[i][j].getValue("bgColor") as Color,
                                        leftWidth = if ( j<=1 ) 1.dp else 0.5.dp,
                                        rightWidth = if ( j == 0 || j == columnCount-1 ) 1.dp else 0.5.dp,
                                        topWidth = if ( i <= 1 ) 1.dp else 0.5.dp,
                                        bottomWidth = if ( i == 0 || i == rowCount-1 ) 1.dp else 0.5.dp,
                                        onClick = {
                                            selectedCell = i to j
                                            inputText = columnProperties[i][j].getValue("text") as String
                                            inputSubText = columnProperties[i][j].getValue("subText") as String
                                            inputFontSize = columnProperties[i][j].getValue("fontSize") as String
                                            inputSubFontSize = columnProperties[i][j].getValue("subFontSize") as String
                                            selectedColor = columnProperties[i][j].getValue("bgColor") as Color
                                            showDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }


            }

            if (showDialog && selectedCell != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("수업 정보 입력") },
                    text = {
                        Column {
                            TextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                label = { Text("Main Text") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = inputSubText,
                                onValueChange = { inputSubText = it },
                                label = { Text("Optional Text") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Font Size: ", modifier = Modifier.padding(end = 8.dp))

                                TextField(
                                    value = inputFontSize,
                                    onValueChange = {
                                        inputFontSize = it
                                    },
                                    label = { Text("Main") },
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(100.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                TextField(
                                    value = inputSubFontSize,
                                    onValueChange = {
                                        inputSubFontSize = it
                                    },
                                    label = { Text("Sub") },
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(100.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                listOf(
                                    Color(0xFFF2F2F2), // 기본
                                    Color(0xFFD7D4D4), // 회색
                                    Color(0xFFFFDEDE), // 빨강
                                    Color(0xFFDEE0FF)  // 파랑
                                ).forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(color, shape = CircleShape)
                                            .border(2.dp, if (selectedColor == color) Color.Black else Color.Transparent, shape = CircleShape)
                                            .clickable { selectedColor = color }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (isNumeric(inputFontSize) && isNumeric(inputSubFontSize)) {
                                selectedCell?.let { (row, col) ->
                                    columnProperties[row][col]["text"] = inputText
                                    columnProperties[row][col]["subText"] = inputSubText
                                    columnProperties[row][col]["fontSize"] = inputFontSize
                                    columnProperties[row][col]["subFontSize"] = inputSubFontSize
                                    columnProperties[row][col]["bgColor"] = selectedColor
                                }
                                showDialog = false
                            } else {
                                Toast.makeText(context, "Font size should not be empty.", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("확인")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("취소")
                        }
                    }
                )
            }
            Spacer(
                modifier = Modifier.padding(paddingValues)
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = modifier,
                containerColor = Color(0xFFF6F6F8),
                content = {
                    Button(
                        onClick = {
                            onNavigateToMain()
                        },
                        shape = RoundedCornerShape(corner = CornerSize(100)),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(20.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent
                        )
                    ) {
                        Text("Cancel", color = Color.Black)
                    }
                    Button(
                        onClick = {
                            uiScope.launch {
                                val bitmapAsync = captureController.captureAsync()
                                try {
                                    val bitmap = bitmapAsync.await().asAndroidBitmap()

                                    // 파일 경로 설정 (예: 외부 저장소의 'Pictures' 폴더)
                                    val fileName = "captured_image_${System.currentTimeMillis()}.png"
                                    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                                    val file = File(storageDir, fileName)

                                    // 디렉토리가 없다면 생성
                                    if (!storageDir.exists()) {
                                        storageDir.mkdirs()
                                    }

                                    // 비트맵을 파일로 저장
                                    val outputStream = FileOutputStream(file)
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                    outputStream.flush()
                                    outputStream.close()

                                    // 저장 후 Toast나 메시지로 사용자에게 알림
                                    Toast.makeText(context, "이미지가 저장되었습니다: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                                } catch (error: Throwable) {
                                    print(error)
                                }
                            }

                        },
                        shape = RoundedCornerShape(corner = CornerSize(100)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent
                        )
                    ) {
                        Text("Download", color = Color.Black)
                    }
                }
            )
        }
    )
}

@Composable
fun CircularButton (
    modifier: Modifier = Modifier,
    text: String,
    isEnabled: Boolean = false,
    callback: () -> Unit,
) {
    Button(
        onClick = { callback() },
        enabled = true,
        shape = CircleShape,
        border = if (isEnabled) BorderStroke(2.dp, Color(0xFF376FDD)) else null,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.White,
            disabledContentColor = Color.Black,
            containerColor = Color.White,
            contentColor = Color.Black,
        ),
        modifier = modifier.size(45.dp).padding(0.dp),
        contentPadding = PaddingValues(0.dp)

    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth(),
            color = if (isEnabled) Color(0xFF376FDD) else Color.Black,
            fontSize = 18.sp,
            softWrap = false
        )
    }
    Spacer(modifier = Modifier.size(3.dp))
}

@Composable
fun NumberTextField(
    value : String = "",
    width: Dp = 50.dp,
    fontSize: TextUnit = 25.sp,
    callback: (changedValue: String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = {callback(it)},
        modifier = Modifier
            .width(width)
            .height(30.dp),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = fontSize,
            color = Color.Black,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        decorationBox = { innerTextField -> run {
            Box(
                modifier = Modifier
                    .border(
                        border = BorderStroke(2.dp, Color.Black),
                        shape = RoundedCornerShape(corner = CornerSize(10.dp))
                    ),
                contentAlignment = Alignment.Center,
            ) { innerTextField() }
        }}
    )
}

@Composable
fun TimeTableBoxColumn(
    text: String = "",
    subText: String? = null,
    subTextColor: Color = Color(0xFF8D8D8D),
    fontSize: TextUnit = 15.sp,
    subFontSize: TextUnit = 10.sp,
    bgColor: Color = Color(0xFFF2F2F2),
    leftWidth: Dp = 0.5.dp,
    rightWidth: Dp = 0.5.dp,
    topWidth: Dp = 0.5.dp,
    bottomWidth: Dp = 0.5.dp,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
//            .fillMaxHeight()
            .height(50.dp)
            .width(50.dp)
            .background(color = bgColor)
            .drawBehind {
                val lw = leftWidth.toPx()
                val rw = rightWidth.toPx()
                val tw = topWidth.toPx()
                val bw = bottomWidth.toPx()

                drawLine(Color.Black, Offset(0f, 0f), Offset(0f, size.height), lw)
                drawLine(Color.Black, Offset(size.width, 0f), Offset(size.width, size.height), rw)
                drawLine(Color.Black, Offset(0f, 0f), Offset(size.width, 0f), tw)
                drawLine(Color.Black, Offset(0f, size.height), Offset(size.width, size.height), bw)

            }
            .clickable { onClick() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = fontSize, fontFamily = FontFamily(Font(R.font.npsfont_bold))),

        )
        if (!subText.isNullOrBlank()) {
            Text(
                text = subText,
                fontSize = 12.sp,
                color = subTextColor,
//                color = Color(0xFF8D8D8D),
                style = TextStyle(fontSize = subFontSize, fontFamily = FontFamily(Font(R.font.npsfont_regular))),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}






fun isNumeric(str: String): Boolean {
    return str.toIntOrNull() != null
}

fun timeAdder(
    firstHour: Int,
    firstMinute: Int,
    secondMinute: Int,
    count: Int = 1,
    lunchMinute: Int = 0
) : String {
    val totalMinute = firstMinute + (secondMinute + 10) * count + lunchMinute
    val finalHour = (firstHour + totalMinute / 60) % 24
    val finalMinute = totalMinute % 60

    return String.format(Locale.KOREA, "%02d:%02d", finalHour, finalMinute)
}








class MainViewModel : ViewModel() {
    val dayStartWith: MutableState<Int> = mutableIntStateOf(0)
    var daySelected by mutableStateOf(listOf(true, true, true, true, true, false, false))

    val classEnd: MutableState<String> = mutableStateOf("7")
    var classStartTime by mutableStateOf(listOf("8", "50"))
    val classDuration: MutableState<String> = mutableStateOf("50")

    var lunchTime by mutableStateOf(listOf("true", "4", "50"))

    fun updateDayStartWith(value: Int) {
        dayStartWith.value = value
    }
    fun updateDaySelected(index: Int, value: Boolean) {
        daySelected = daySelected.toMutableList().also { it[index] = value}
    }

    fun updateClassEnd(value: String) {
        classEnd.value = value
    }
    fun updateClassStartTime(index: Int, value: String) {
        classStartTime = classStartTime.toMutableList().also { it[index] = value }
    }
    fun updateClassDuration(value: String) {
        classDuration.value = value
    }

    fun updateLunchTime(index: Int, value: String) {
        lunchTime = lunchTime.toMutableList().also { it[index] = value}
    }

}




//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Main()
//}
