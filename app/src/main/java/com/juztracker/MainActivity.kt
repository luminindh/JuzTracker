package com.juztracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val Bg        = Color(0xFF1B2922)
private val Surface0  = Color(0xFF223329)
private val Cell      = Color(0xFF1E2D24)
private val Border    = Color(0xFF2D4035)
private val TextCol   = Color(0xFFDDD8CC)
private val Dim       = Color(0xFF5A7868)
private val Green     = Color(0xFF4A9E6E)
private val GreenDk   = Color(0xFF2D5A3D)
private val GoldBg    = Color(0xFF2A2410)
private val GoldBdr   = Color(0xFF6A5018)
private val GoldTxt   = Color(0xFFD4A843)
private val TodayBg   = Color(0xFF2D5A3D)
private val TodayTx   = Color(0xFFE8F5EE)
private val ShineBg   = Color(0xFF2E2500)
private val ShineGold = Color(0xFFFFD966)
private val SuggRed   = Color(0xFFE05555)
private val SuggBlue  = Color(0xFF43B4D4)

fun calcJuz(date: LocalDate, startDate: LocalDate, startJuz: Int): Int {
    val diff = date.toEpochDay() - startDate.toEpochDay()
    return (((startJuz - 1 + diff) % 30 + 30) % 30 + 1).toInt()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { JuzTrackerApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuzTrackerApp() {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("juz_tracker", Context.MODE_PRIVATE) }
    val today   = remember { LocalDate.now() }

    var startDate by remember {
        mutableStateOf(
            prefs.getString("start_date", null)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.of(2026, 6, 25)
        )
    }
    var startJuz by remember { mutableIntStateOf(prefs.getInt("start_juz", 30)) }
    var viewYM       by remember { mutableStateOf(YearMonth.from(today)) }
    var showSettings by remember { mutableStateOf(false) }

    val todayJuz = calcJuz(today, startDate, startJuz)

    val suggestion: Pair<String, Color>? = remember(startDate, startJuz, today) {
        for (i in 0..30) {
            val d = today.plusDays(i.toLong())
            if (d.dayOfWeek != DayOfWeek.THURSDAY) continue
            val j   = calcJuz(d, startDate, startJuz)
            val fmt = "${d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}, " +
                      "${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${d.year}"
            if (j == 13) return@remember Pair("1 more juz before $fmt.", SuggRed)
            if (j == 16) return@remember Pair("Rest 1 juz before $fmt.", SuggBlue)
        }
        null
    }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
                .padding(top = 20.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Al-Kahf Tracker", fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold, color = TextCol)
                    Text(
                        "${viewYM.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${viewYM.year}",
                        fontSize = 13.sp, color = Dim
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TODAY · JUZ", fontSize = 9.sp, color = Dim, letterSpacing = 1.sp)
                    Text(todayJuz.toString(), fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold, color = Green,
                        letterSpacing = (-2).sp, lineHeight = 52.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                NavButton("‹") { viewYM = viewYM.minusMonths(1) }
                TextButton(onClick = { viewYM = YearMonth.from(today) }) {
                    Text("Today", color = Dim, fontSize = 11.sp)
                }
                NavButton("›") { viewYM = viewYM.plusMonths(1) }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showSettings = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface0),
                contentPadding = PaddingValues(vertical = 7.dp)
            ) {
                Text("⚙  STARTING POINT", color = Dim, fontSize = 11.sp, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth()) {
                listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat").forEachIndexed { i, label ->
                    val h = i == 4 || i == 5
                    Text(label, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center, fontSize = 9.sp,
                        color = if (h) GoldTxt else Dim,
                        fontWeight = if (h) FontWeight.Bold else FontWeight.Normal)
                }
            }

            Spacer(Modifier.height(3.dp))

            CalendarGrid(yearMonth = viewYM, today = today,
                startDate = startDate, startJuz = startJuz)

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                LegendDot(TodayBg, null, "TODAY")
                LegendDot(GoldBg, GoldBdr, "THU / FRI")
                Text("N = JUZ", fontSize = 9.sp, color = Dim)
            }

            if (suggestion != null) {
                Spacer(Modifier.height(14.dp))
                Text(suggestion.first, fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, color = suggestion.second)
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            startDate = startDate, startJuz = startJuz,
            onDismiss = { showSettings = false },
            onSave = { d, j ->
                startDate = d; startJuz = j
                prefs.edit().putString("start_date", d.toString()).putInt("start_juz", j).apply()
                showSettings = false
            }
        )
    }
}

@Composable
private fun NavButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface0),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
        Text(label, color = TextCol, fontSize = 18.sp)
    }
}

@Composable
fun CalendarGrid(yearMonth: YearMonth, today: LocalDate,
                 startDate: LocalDate, startJuz: Int) {
    val firstDay    = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val rows        = (startOffset + daysInMonth + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - startOffset + 1
                    if (day < 1 || day > daysInMonth) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val date     = yearMonth.atDay(day)
                        val juz      = calcJuz(date, startDate, startJuz)
                        val isToday  = date == today
                        val isThuFri = date.dayOfWeek == DayOfWeek.THURSDAY ||
                                       date.dayOfWeek == DayOfWeek.FRIDAY
                        val isPast   = date.isBefore(today)
                        DayCell(modifier = Modifier.weight(1f), day = day, juz = juz,
                            isToday = isToday, isThuFri = isThuFri,
                            isPast = isPast && !isToday,
                            isShine = juz == 15 && isThuFri)
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(modifier: Modifier = Modifier, day: Int, juz: Int,
            isToday: Boolean, isThuFri: Boolean, isPast: Boolean, isShine: Boolean) {
    val transition = rememberInfiniteTransition(label = "shine")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.4f, targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse), label = "glow")

    val bg = when { isToday -> TodayBg; isShine -> ShineBg; isThuFri -> GoldBg; else -> Cell }
    val borderColor = when { isShine -> GoldTxt.copy(alpha = glowAlpha); isThuFri -> GoldBdr; else -> Color.Transparent }
    val juzColor = when { isToday -> TodayTx; isShine -> ShineGold; isThuFri -> GoldTxt; else -> Dim }
    val dayNumColor = when { isToday -> TodayTx.copy(alpha = 0.45f); isShine -> Color(0xFF8A6820); isThuFri -> GoldBdr; else -> Dim }

    Box(modifier = modifier.height(56.dp)
        .graphicsLayer { alpha = if (isPast) 0.36f else 1f }
        .clip(RoundedCornerShape(8.dp))
        .background(bg)
        .border(1.dp, borderColor, RoundedCornerShape(8.dp))) {
        Text(day.toString(), modifier = Modifier.align(Alignment.TopEnd)
            .padding(end = 3.dp, top = 3.dp),
            fontSize = 8.sp, color = dayNumColor, lineHeight = 8.sp)
        Text(juz.toString(), modifier = Modifier.align(Alignment.Center),
            fontSize = 15.sp, fontWeight = FontWeight.ExtraBold,
            color = juzColor, textAlign = TextAlign.Center)
    }
}

@Composable
private fun LegendDot(color: Color, borderColor: Color?, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(2.dp)) else Modifier))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, color = Dim)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(startDate: LocalDate, startJuz: Int,
                  onDismiss: () -> Unit, onSave: (LocalDate, Int) -> Unit) {
    var selectedDate   by remember { mutableStateOf(startDate) }
    var selectedJuz    by remember { mutableIntStateOf(startJuz) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showJuzDialog  by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface0,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(Modifier.padding(vertical = 12.dp).size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp)).background(Border))
        }) {
        Column(modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 18.dp).padding(bottom = 44.dp)) {
            Text("Starting Point", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = TextCol, modifier = Modifier.padding(bottom = 22.dp))

            Text("DATE", fontSize = 11.sp, color = Dim, letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 7.dp))
            Surface(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                shape = RoundedCornerShape(10.dp), color = Bg,
                border = BorderStroke(1.dp, Border)) {
                Text(selectedDate.toString(),
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                    fontSize = 14.sp, color = TextCol)
            }

            Spacer(Modifier.height(18.dp))

            Text("JUZ ON THAT DATE", fontSize = 11.sp, color = Dim, letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 7.dp))
            Surface(modifier = Modifier.fillMaxWidth().clickable { showJuzDialog = true },
                shape = RoundedCornerShape(10.dp), color = Bg,
                border = BorderStroke(1.dp, Border)) {
                Text("Juz $selectedJuz",
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                    fontSize = 14.sp, color = TextCol)
            }

            Spacer(Modifier.height(18.dp))

            Button(onClick = { onSave(selectedDate, selectedJuz) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenDk),
                contentPadding = PaddingValues(vertical = 13.dp)) {
                Text("Done", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let {
                        selectedDate = LocalDate.ofEpochDay(it / 86_400_000L)
                    }
                    showDatePicker = false
                }) { Text("OK", color = Green) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Dim)
                }
            }
        ) { DatePicker(state = dpState) }
    }

    if (showJuzDialog) {
        AlertDialog(onDismissRequest = { showJuzDialog = false },
            containerColor = Surface0,
            title = { Text("Select Juz", color = TextCol, fontSize = 16.sp,
                fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()
                    .heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    (1..30).forEach { j ->
                        Row(modifier = Modifier.fillMaxWidth()
                            .clickable { selectedJuz = j; showJuzDialog = false }
                            .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = j == selectedJuz,
                                onClick = { selectedJuz = j; showJuzDialog = false },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Green, unselectedColor = Dim))
                            Spacer(Modifier.width(8.dp))
                            Text("Juz $j", color = TextCol, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showJuzDialog = false }) {
                    Text("Cancel", color = Dim)
                }
            })
    }
}
