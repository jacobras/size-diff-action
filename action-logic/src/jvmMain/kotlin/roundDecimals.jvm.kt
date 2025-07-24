import java.util.Locale

actual fun Double.roundDecimals(decimals: Int): String {
    return String.format(Locale.ROOT, "%.2f", this)
}