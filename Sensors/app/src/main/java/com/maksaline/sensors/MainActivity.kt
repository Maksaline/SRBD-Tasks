package com.maksaline.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maksaline.sensors.ui.theme.SensorsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SensorsTheme {
                SensorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorApp() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensors = remember { sensorManager.getSensorList(Sensor.TYPE_ALL) }
    
    var selectedSensor by remember { mutableStateOf<Sensor?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedSensor?.name ?: "Available Sensors") },
                navigationIcon = {
                    if (selectedSensor != null) {
                        IconButton(onClick = { selectedSensor = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (selectedSensor == null) {
            SensorList(
                sensors = sensors,
                onSensorClick = { selectedSensor = it },
                modifier = Modifier.padding(padding)
            )
        } else {
            SensorDataView(
                sensor = selectedSensor!!,
                sensorManager = sensorManager,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun SensorList(
    sensors: List<Sensor>,
    onSensorClick: (Sensor) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(sensors) { sensor ->
            ListItem(
                headlineContent = { Text(sensor.name) },
                supportingContent = { Text("Vendor: ${sensor.vendor}") },
                modifier = Modifier.clickable { onSensorClick(sensor) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun SensorDataView(
    sensor: Sensor,
    sensorManager: SensorManager,
    modifier: Modifier = Modifier
) {
    var sensorValues by remember { mutableStateOf(floatArrayOf()) }
    
    DisposableEffect(sensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { sensorValues = it.values.clone() }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Sensor Info", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Name: ${sensor.name}")
        Text("Vendor: ${sensor.vendor}")
        Text("Max Range: ${sensor.maximumRange}")
        Text("Resolution: ${sensor.resolution}")

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
        )
        
        Text("Live Data", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        if (sensorValues.isEmpty()) {
            Text("No Sensor value found.")
        } else {
            sensorValues.forEachIndexed { index, value ->
                Text("Value[$index]: $value")
            }
        }
    }
}