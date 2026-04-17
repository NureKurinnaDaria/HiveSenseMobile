package com.kurinna.hivesensemobile.alerts

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
fun AlertsScreen(
    onBack: () -> Unit,
    currentWarehouseId: Int?
) {
    val alertsViewModel: AlertsViewModel = viewModel()

    val alerts by alertsViewModel.alerts.collectAsState()
    val isLoading by alertsViewModel.isLoading.collectAsState()
    val errorMessage by alertsViewModel.errorMessage.collectAsState()
    val actionInProgressId by alertsViewModel.actionInProgressId.collectAsState()

    LaunchedEffect(Unit) {
        alertsViewModel.loadAlerts()
        alertsViewModel.startAutoRefresh()
    }

    DisposableEffect(Unit) {
        onDispose {
            alertsViewModel.stopAutoRefresh()
        }
    }

    val warehouseAlerts = alerts.filter { it.warehouse_id == currentWarehouseId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Тривоги",
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

            warehouseAlerts.isEmpty() -> {
                Text(text = "Для цього складу тривог не знайдено")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(warehouseAlerts) { alert ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = mapAlertType(alert.type),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Статус: ${mapAlertStatus(alert.status)}")
                                Text(text = "Створено: ${formatDateTime(alert.created_at)}")
                                Text(
                                    text = "Закрито: ${
                                        alert.resolved_at?.let { formatDateTime(it) } ?: "Ще не закрито"
                                    }"
                                )

                                if (alert.status == "NEW") {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            alertsViewModel.acknowledgeAlert(alert.alert_id)
                                        },
                                        enabled = actionInProgressId != alert.alert_id,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            if (actionInProgressId == alert.alert_id)
                                                "Підтвердження..."
                                            else
                                                "Підтвердити"
                                        )
                                    }
                                }

                                if (alert.status == "ACKNOWLEDGED") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Тривога підтверджена. Очікується автоматичне закриття після нормалізації показників.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                if (alert.status == "RESOLVED") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Тривога закрита автоматично.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
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

private fun mapAlertType(type: String): String {
    return when (type) {
        "TEMP_HIGH" -> "Висока температура"
        "TEMP_LOW" -> "Низька температура"
        "HUMIDITY_HIGH" -> "Висока вологість"
        "HUMIDITY_LOW" -> "Низька вологість"
        else -> type
    }
}

private fun mapAlertStatus(status: String): String {
    return when (status) {
        "NEW" -> "Нова"
        "ACKNOWLEDGED" -> "Підтверджена"
        "RESOLVED" -> "Закрита"
        else -> status
    }
}

private fun formatDateTime(raw: String): String {
    return raw.replace("T", " ").replace("Z", "")
}