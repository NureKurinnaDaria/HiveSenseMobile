package com.kurinna.hivesensemobile.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    warehouseName: String,
    warehouseLocation: String,
    warehouseStatus: String,
    temperature: String,
    humidity: String,
    activeAlertsCount: Int,
    onOpenAlerts: () -> Unit,
    onOpenMeasurements: () -> Unit,
    onOpenBatches: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Мій склад",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = warehouseName,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Локація: $warehouseLocation")
                Text(text = "Статус: $warehouseStatus")

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Температура: $temperature °C")
                Text(text = "Вологість: $humidity %")
                Text(text = "Активні тривоги: $activeAlertsCount")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenAlerts,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Тривоги")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onOpenMeasurements,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Поточні показники")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onOpenBatches,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Партії меду")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Вийти")
        }
    }
}