import js.core.toFixed

actual fun Double.roundDecimals(decimals: Int): String {
    return toFixed(decimals)
}