package com.kurinna.hivesensemobile.batches

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BatchesScreen(
    onBack: () -> Unit,
    currentWarehouseId: Int?
) {
    val batchesViewModel: BatchesViewModel = viewModel()

    val batches by batchesViewModel.batches.collectAsState()
    val isLoading by batchesViewModel.isLoading.collectAsState()
    val errorMessage by batchesViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        batchesViewModel.loadBatches()
    }

    val warehouseBatches = batches.filter { it.warehouse_id == currentWarehouseId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Партії меду",
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

            warehouseBatches.isEmpty() -> {
                Text(text = "Для цього складу партій меду не знайдено")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(warehouseBatches) { batch ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Партія #${batch.batch_id}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Сорт меду: ${batch.variety}")
                                Text(text = "Кількість: ${batch.quantity_kg} кг")
                                Text(text = "Дата надходження: ${batch.received_date}")
                                Text(
                                    text = "Термін придатності: ${batch.expiration_date ?: "Не вказано"}"
                                )
                                Text(text = "Статус: ${mapBatchStatus(batch.status)}")
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

private fun mapBatchStatus(status: String): String {
    return when (status) {
        "ACTIVE" -> "Активна"
        "INACTIVE" -> "Неактивна"
        else -> status
    }
}