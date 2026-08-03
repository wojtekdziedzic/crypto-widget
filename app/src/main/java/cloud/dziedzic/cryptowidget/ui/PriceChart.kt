package cloud.dziedzic.cryptowidget.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cloud.dziedzic.cryptowidget.data.Currency

/** Line chart of price history with a gradient fill and min/max labels. */
@Composable
fun PriceChart(points: List<Float>, currency: Currency, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 11.sp, color = BrandTextDim)
    Canvas(modifier = modifier.fillMaxSize()) {
        if (points.size < 2) return@Canvas
        val min = points.min()
        val max = points.max()
        val range = (max - min).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (points.size - 1)
        val pad = size.height * 0.12f
        val usableHeight = size.height - 2 * pad

        fun pointAt(index: Int) = Offset(
            x = index * stepX,
            y = pad + (1f - (points[index] - min) / range) * usableHeight,
        )

        val line = Path().apply {
            moveTo(pointAt(0).x, pointAt(0).y)
            for (i in 1 until points.size) {
                lineTo(pointAt(i).x, pointAt(i).y)
            }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                colors = listOf(BrandAccent.copy(alpha = 0.25f), Color.Transparent),
            ),
        )
        drawPath(
            path = line,
            color = BrandAccent,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Min/max labels in the corners, above/below the padded plot area.
        val maxLabel = textMeasurer.measure(
            AnnotatedString(Formatters.price(max.toDouble(), currency)),
            labelStyle,
        )
        val minLabel = textMeasurer.measure(
            AnnotatedString(Formatters.price(min.toDouble(), currency)),
            labelStyle,
        )
        drawText(maxLabel, topLeft = Offset(4.dp.toPx(), 2.dp.toPx()))
        drawText(
            minLabel,
            topLeft = Offset(4.dp.toPx(), size.height - minLabel.size.height - 2.dp.toPx()),
        )
    }
}
