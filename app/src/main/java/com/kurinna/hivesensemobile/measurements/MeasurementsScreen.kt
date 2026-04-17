package com.kurinna.hivesensemobile.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MeasurementsScreen(
    onBack: () -> Unit,
    currentWarehouseId: Int?
) {
    val measurementsViewModel: MeasurementsViewModel = viewModel()

    val measurements by measurementsViewModel.measurements.collectAsState()
    val isLoading by measurementsViewModel.isLoading.collectAsState()
    val errorMessage by measurementsViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        measurementsViewModel.loadMeasurements()
        measurementsViewModel.startAutoRefresh()
    }

    DisposableEffect(Unit) {
        onDispose {
            measurementsViewModel.stopAutoRefresh()
        }
    }

    val warehouseMeasurements = measurements.filter {
        it.sensor.warehouse_id == currentWarehouseId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Поточні показники",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Невідома помилка",
                    color = MaterialTheme.colorScheme.error
                )
            }

            warehouseMeasurements.isEmpty() -> {
                Text(text = "Для цього складу вимірювань не знайдено")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(warehouseMeasurements) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Датчик: ${item.sensor.serial_number}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Температура: ${item.temperature_c} °C")
                                Text(text = "Вологість: ${item.humidity_percent} %")
                                Text(text = "Час вимірювання: ${formatMeasurementTime(item.measured_at)}")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад")
        }
    }
}

private fun formatMeasurementTime(raw: String): String {
    return raw.replace("T", " ").replace("Z", "")
}