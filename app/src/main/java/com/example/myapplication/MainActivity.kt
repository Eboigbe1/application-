package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Booking(
    val id: Int,
    val r: String,
    val d: String,
    val s: String,
    var st: String,
    val by: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark = isSystemInDarkTheme()
            val bg = if (dark) Color(0xFF101715) else Color(0xFFF4F6F5)
            val cardBg = if (dark) Color(0xFF1A2420) else Color(0xFFFFFFFF)
            val ink = if (dark) Color(0xFFE8F0ED) else Color(0xFF16211E)
            val acc = if (dark) Color(0xFF4FC3AD) else Color(0xFF0B6B5C)
            val onAcc = if (dark) Color(0xFF06231D) else Color(0xFFFFFFFF)

            MaterialTheme(
                colorScheme = if (dark) darkColorScheme(
                    primary = acc,
                    background = bg,
                    surface = cardBg,
                    onPrimary = onAcc,
                    onBackground = ink,
                    onSurface = ink
                ) else lightColorScheme(
                    primary = acc,
                    background = bg,
                    surface = cardBg,
                    onPrimary = onAcc,
                    onBackground = ink,
                    onSurface = ink
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OfficeBookingApp()
                }
            }
        }
    }
}

val D = listOf("Mon 9 Nov", "Tue 10 Nov", "Wed 11 Nov", "Thu 12 Nov")
val S = listOf("09:00–10:00", "10:00–11:00", "11:00–12:00", "13:00–14:00")
val ROOMS_DATA = listOf(
    "Meeting Room 1" to 4,
    "Meeting Room 2" to 6,
    "Meeting Room 3" to 6,
    "Meeting Room 4" to 8,
    "Meeting Room 5" to 10,
    "Seminar Room 1" to 30,
    "Seminar Room 2" to 40
)

@Composable
fun OfficeBookingApp() {
    var role by remember { mutableStateOf<String?>(null) } // "emp", "mgr", null
    var scr by remember { mutableStateOf("login") }
    var tab by remember { mutableStateOf("rooms") }
    var d by remember { mutableStateOf(D[0]) }
    var s by remember { mutableStateOf(S[1]) }
    var room by remember { mutableStateOf<String?>(null) }
    var who by remember { mutableStateOf("me") }
    var emp by remember { mutableStateOf("") }
    var nbRoom by remember { mutableStateOf(ROOMS_DATA[0].first) }
    var err by remember { mutableStateOf("") }
    var cancelId by remember { mutableStateOf<Int?>(null) }
    var sentTarget by remember { mutableStateOf("") } // purpose

    val bookings = remember {
        mutableStateListOf(
            Booking(1, "Meeting Room 2", D[0], S[1], "Approved", "A. Khan"),
            Booking(2, "Seminar Room 1", D[1], S[3], "Pending", "A. Khan"),
            Booking(5, "Seminar Room 1", D[0], S[3], "Approved", "You"),
            Booking(10, "Meeting Room 3", D[1], S[2], "Pending", "S. Patel"),
            Booking(11, "Seminar Room 2", D[2], S[0], "Pending", "J. Lee"),
            Booking(3, "Meeting Room 1", D[0], S[0], "Approved", "A. Khan"),
            Booking(4, "Meeting Room 3", D[0], S[0], "Approved", "A. Khan")
        )
    }

    val act = { b: Booking -> b.st != "Cancelled" }
    val reqs = bookings.filter { it.st == "Pending" || it.st == "Cancellation requested" }
    fun taken(rName: String, date: String, slot: String): Booking? {
        return bookings.find { it.r == rName && it.d == date && it.s == slot && act(it) }
    }

    Scaffold(
        bottomBar = {
            if (role != null && scr != "request" && scr != "sent" && scr != "cfm" && scr != "sched" && scr != "nb" && scr != "mcfm") {
                if (role == "emp") {
                    NavigationBar {
                        NavigationBarItem(
                            selected = tab == "rooms",
                            onClick = { tab = "rooms"; scr = "rooms"; err = "" },
                            icon = {},
                            label = { Text("Find") }
                        )
                        NavigationBarItem(
                            selected = tab == "bookings",
                            onClick = { tab = "bookings"; scr = "bookings"; err = "" },
                            icon = {},
                            label = { Text("My bookings") }
                        )
                    }
                } else if (role == "mgr") {
                    NavigationBar {
                        NavigationBarItem(
                            selected = tab == "mrooms",
                            onClick = { tab = "mrooms"; scr = "mrooms"; err = "" },
                            icon = {},
                            label = { Text("Rooms") }
                        )
                        NavigationBarItem(
                            selected = tab == "mreq",
                            onClick = { tab = "mreq"; scr = "mreq"; err = "" },
                            icon = {},
                            label = { Text("Requests (${reqs.size})") }
                        )
                        NavigationBarItem(
                            selected = tab == "mbk",
                            onClick = { tab = "mbk"; scr = "mbk"; err = "" },
                            icon = {},
                            label = { Text("Bookings") }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val backTarget = when (scr) {
                    "request" -> "rooms"
                    "cfm" -> "bookings"
                    "sched" -> "mrooms"
                    "nb" -> "mrooms"
                    "mcfm" -> "mbk"
                    else -> null
                }
                if (backTarget != null) {
                    TextButton(onClick = {
                        scr = backTarget
                        err = ""
                    }) {
                        Text("←", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                val title = when (scr) {
                    "login" -> "Office Booking"
                    "rooms" -> "Available rooms"
                    "request" -> "Request booking"
                    "sent" -> "Request sent"
                    "bookings" -> "My bookings"
                    "cfm" -> "Cancel booking"
                    "mrooms" -> "All rooms"
                    "sched" -> room ?: "Room schedule"
                    "nb" -> "New booking"
                    "mreq" -> "Requests"
                    "mcfm" -> "Cancel booking"
                    "mbk" -> "All bookings"
                    else -> "Office Booking"
                }
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Main Content Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (scr) {
                    "login" -> Column {
                        Text("Your role is detected from your account. For this demo, pick one:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Work email", fontSize = 14.sp)
                        OutlinedTextField(value = "name@company.com", onValueChange = {}, modifier = Modifier.fillMaxWidth(), readOnly = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Password", fontSize = 14.sp)
                        OutlinedTextField(value = "••••••••", onValueChange = {}, modifier = Modifier.fillMaxWidth(), readOnly = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { role = "emp"; tab = "rooms"; scr = "rooms" },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Log in as employee", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { role = "mgr"; tab = "mrooms"; scr = "mrooms" },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Log in as manager", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "rooms" -> {
                        val availableRooms = ROOMS_DATA.filter { taken(it.first, d, s) == null }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                ChipRow(items = D, selected = d) { d = it; err = "" }
                                Spacer(modifier = Modifier.height(8.dp))
                                ChipRow(items = S, selected = s) { s = it; err = "" }
                            }
                            if (availableRooms.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                        Text("No rooms are free at this time. Try another slot or date.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else {
                                items(availableRooms) { rItem ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(rItem.first, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                            Text("${rItem.second} seats", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            StatusTag("Available")
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(onClick = { room = rItem.first; scr = "request" }) {
                                                Text("Request")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "request" -> Column {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(room ?: "", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("$d, $s", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Purpose (optional)", fontSize = 14.sp)
                        OutlinedTextField(value = sentTarget, onValueChange = { sentTarget = it }, placeholder = { Text("Team catch-up") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("A manager must approve this request before it is confirmed.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                bookings.add(Booking(id = 50 + bookings.size, r = room ?: ROOMS_DATA[0].first, d = d, s = s, st = "Pending", by = "A. Khan"))
                                sentTarget = ""
                                scr = "sent"
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Submit request")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { scr = "rooms" }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancel")
                        }
                    }

                    "sent" -> Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Request sent", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusTag("Pending")
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { tab = "bookings"; scr = "bookings" }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text("View my bookings")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { tab = "rooms"; scr = "rooms" }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text("Book another room")
                        }
                    }

                    "bookings" -> {
                        val myList = bookings.filter { it.by == "A. Khan" }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(myList) { b ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(b.r, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Text("${b.d}, ${b.s}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        StatusTag(b.st)
                                        if (b.st == "Approved" || b.st == "Pending") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedButton(onClick = { cancelId = b.id; scr = "cfm" }) {
                                                Text("Cancel")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "cfm" -> {
                        val b = bookings.find { it.id == cancelId }
                        if (b != null) {
                            Column {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(b.r, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Text("${b.d}, ${b.s}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        StatusTag(b.st)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Cancel this booking?${if (b.st == "Approved") " A manager will be notified." else ""}")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (b.st == "Approved") {
                                            b.st = "Cancellation requested"
                                        } else {
                                            b.st = "Cancelled"
                                        }
                                        scr = "bookings"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Yes, cancel booking")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { scr = "bookings" }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                                    Text("Keep booking")
                                }
                            }
                        }
                    }

                    "mrooms" -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                ChipRow(items = D, selected = d) { d = it; err = "" }
                                Spacer(modifier = Modifier.height(8.dp))
                                ChipRow(items = S, selected = s) { s = it; err = "" }
                            }
                            items(ROOMS_DATA) { rItem ->
                                val b = taken(rItem.first, d, s)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { room = rItem.first; scr = "sched" }
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(rItem.first, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Text(if (b != null) "Booked: ${b.by}" else "${rItem.second} seats", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        StatusTag(if (b != null) "Booked" else "Vacant")
                                    }
                                }
                            }
                            item {
                                Button(
                                    onClick = { who = "me"; nbRoom = ROOMS_DATA[0].first; scr = "nb" },
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("New booking")
                                }
                            }
                        }
                    }

                    "sched" -> {
                        Column {
                            Text(d, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    S.forEach { slotTime ->
                                        val b = taken(room ?: "", d, slotTime)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(slotTime.take(5), fontWeight = FontWeight.Medium)
                                            StatusTag(if (b != null) "${b.by} – Booked" else "Vacant")
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { nbRoom = room ?: ROOMS_DATA[0].first; who = "me"; scr = "nb" },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Text("Book this room")
                            }
                        }
                    }

                    "nb" -> {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { who = "me"; err = "" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (who == "me") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("For me", color = if (who == "me") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                }
                                Button(
                                    onClick = { who = "other"; err = "" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (who == "other") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("For someone else", color = if (who == "other") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (who != "me") {
                                Text("Employee", fontSize = 14.sp)
                                OutlinedTextField(value = emp, onValueChange = { emp = it }, placeholder = { Text("Search name…") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (err.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Text(err, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                                }
                            }
                            Text("Room", fontSize = 14.sp)
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(nbRoom)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    ROOMS_DATA.forEach { rOpt ->
                                        DropdownMenuItem(
                                            text = { Text(rOpt.first) },
                                            onClick = { nbRoom = rOpt.first; expanded = false; err = "" }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Date", fontSize = 14.sp)
                            ChipRow(items = D, selected = d) { d = it; err = "" }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Time", fontSize = 14.sp)
                            ChipRow(items = S, selected = s) { s = it; err = "" }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (who != "me" && emp.trim().isEmpty()) {
                                        err = "Enter the employee’s name."
                                        return@Button
                                    }
                                    val conflict = taken(nbRoom, d, s)
                                    if (conflict != null) {
                                        err = "Room already booked $s on $d. Choose a free slot or another room."
                                        return@Button
                                    }
                                    bookings.add(
                                        Booking(
                                            id = 50 + bookings.size,
                                            r = nbRoom,
                                            d = d,
                                            s = s,
                                            st = "Approved",
                                            by = if (who == "me") "You" else (emp.ifEmpty { "Employee" })
                                        )
                                    )
                                    emp = ""
                                    scr = "mbk"
                                    tab = "mbk"
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Text("Confirm booking")
                            }
                        }
                    }

                    "mreq" -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (reqs.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                        Text("No pending requests.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else {
                                items(reqs) { q ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(q.by, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                            Text("${q.r}, ${q.d}, ${q.s}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            StatusTag(if (q.st == "Pending") "Booking request" else "Cancellation request")
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        if (q.st == "Pending") q.st = "Approved" else q.st = "Cancelled"
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Approve")
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        if (q.st == "Pending") q.st = "Cancelled" else q.st = "Approved"
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reject")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "mcfm" -> {
                        val b = bookings.find { it.id == cancelId }
                        if (b != null) {
                            Column {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(b.r, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Text("${b.by}, ${b.d}, ${b.s}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Cancel this booking? ${b.by} will be notified.")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        b.st = "Cancelled"
                                        scr = "mbk"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Yes, cancel booking")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { scr = "mbk" }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                                    Text("Keep booking")
                                }
                            }
                        }
                    }

                    "mbk" -> {
                        val activeList = bookings.filter(act)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(activeList) { b ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(b.r, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Text("${b.by}, ${b.d}, ${b.s}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        StatusTag(b.st)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { cancelId = b.id; scr = "mcfm" },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Hint / switch role footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (role != null) {
                    TextButton(onClick = { role = null; scr = "login" }) {
                        Text("Switch role / restart", color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Text("Prototype: no data is saved", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ChipRow(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(item) },
                label = { Text(item) },
                modifier = Modifier.height(48.dp)
            )
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val (bgCol, textCol) = when {
        status.contains("Available") || status.contains("Approved") || status.contains("Vacant") ->
            Color(0xFFD5F0DF) to Color(0xFF12522D)
        status.contains("Pending") || status.contains("Cancellation") ->
            Color(0xFFFDECC8) to Color(0xFF6B4500)
        else ->
            Color(0xFFFBDCDC) to Color(0xFF7A1D1D)
    }
    Surface(
        color = bgCol,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            color = textCol,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
