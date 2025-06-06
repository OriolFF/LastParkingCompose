package com.uriolus.lastparking.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uriolus.lastparking.R

@Composable
fun GpsAccuracyIndicator(accuracy: Float?) {
    val screenAccuracy: Float = accuracy ?: 1000f // No accuracy means very bad accuracy

    val color = when {
        screenAccuracy <= 5f -> Color(0xFF4CAF50) // Green
        screenAccuracy <= 15f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.gps_accuracy_label, "%.1f".format(screenAccuracy)),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(4.dp))

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val parentMaxWidth = this.maxWidth
            val fiveDpInPixels = with(LocalDensity.current) { 5.dp.toPx() }
            val parentMaxWidthInPixels = with(LocalDensity.current) { parentMaxWidth.toPx() }

            val indicatorWidthInPixels = when {
                screenAccuracy >= 30f -> parentMaxWidthInPixels
                screenAccuracy <= 5f -> fiveDpInPixels
                else -> {
                    val progress = (screenAccuracy - 5f) / (30f - 5f) // progress from 0.0 to 1.0
                    fiveDpInPixels + (progress * (parentMaxWidthInPixels - fiveDpInPixels))
                }
            }
            val indicatorWidth = with(LocalDensity.current) { indicatorWidthInPixels.toDp() }

            // Outer Box for the gray background track
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray, RoundedCornerShape(2.dp))
            ) {
                // Inner Box for the colored indicator
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(indicatorWidth)
                        .background(color, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterStart) // Aligns the colored bar to the start of the gray track
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Good Accuracy")
@Composable
fun GpsAccuracyIndicatorPreviewGood() {
    MaterialTheme {
        GpsAccuracyIndicator(accuracy = 3.5f)
    }
}

@Preview(showBackground = true, name = "Medium Accuracy")
@Composable
fun GpsAccuracyIndicatorPreviewMedium() {
    MaterialTheme {
        GpsAccuracyIndicator(accuracy = 12.0f)
    }
}

@Preview(showBackground = true, name = "Bad Accuracy")
@Composable
fun GpsAccuracyIndicatorPreviewBad() {
    MaterialTheme {
        GpsAccuracyIndicator(accuracy = 25.0f)
    }
}
