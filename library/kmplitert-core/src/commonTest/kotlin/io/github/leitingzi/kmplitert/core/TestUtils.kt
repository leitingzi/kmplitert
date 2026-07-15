package io.github.leitingzi.kmplitert.core

expect suspend fun loadResourceAsBytes(name: String): ByteArray

expect fun LiteRTAccelerator.isSupportedOnCurrentPlatform(): Boolean


val CELSIUS_TO_FAHRENHEIT_MODEL_BASE64 = 
    "HAAAAFRGTDMUACAAHAAYABQAEAAMAAAACAAEABQAAAAcAAAAHAAAAHQAAABMAQAA" +
    "XAEAABQDAAADAAAAAAAAAAIAAAA0AAAABAAAANz///8GAAAABAAAABMAAABDT05W" +
    "RVJTSU9OX01FVEFEQVRBAAgADAAIAAQACAAAAAUAAAAEAAAAEwAAAG1pbl9ydW50" +
    "aW1lX3ZlcnNpb24ABwAAANQAAADMAAAAuAAAAJwAAACUAAAAdAAAAAQAAABi////" +
    "BAAAAGAAAAAQAAAAAAAAAAgADgAIAAQACAAAABAAAAAkAAAAAAAGAAgABAAGAAAA" +
    "BAAAAAAAAAAMABgAFAAQAAwABAAMAAAAlXasyTA/XcgDAAAAAgAAAAQAAAAGAAAA" +
    "Mi4yMC4wAADO////BAAAABAAAAAxLjUuMAAAAAAAAAAAAAAAHP7//+7///8EAAAA" +
    "BAAAAIhM50EAAAYACAAEAAYAAAAEAAAABAAAAIoi6z9I/v//TP7//w8AAABNTElS" +
    "IENvbnZlcnRlZC4AAQAAABQAAAAAAA4AGAAUABAADAAIAAQADgAAABQAAAAcAAAA" +
    "YAAAAGQAAABoAAAABAAAAG1haW4AAAAAAQAAABQAAAAAAA4AFAAAABAADAALAAQA" +
    "DgAAABAAAAAAAAAIDAAAABAAAADM/v//AQAAAAMAAAADAAAAAAAAAAEAAAACAAAA" +
    "AQAAAAMAAAABAAAAAAAAAAQAAADsAAAAiAAAAEAAAAAEAAAAOv///wAAAAEQAAAA" +
    "EAAAAAQAAAAYAAAAJP///wgAAABJZGVudGl0eQAAAAACAAAAAQAAAAEAAABy////" +
    "AAAAARAAAAAQAAAAAwAAACgAAABc////GAAAAHNlcXVlbnRpYWxfMS9kZW5zZV8x" +
    "L0FkZAAAAAABAAAAAQAAALb///8AAAABEAAAABAAAAACAAAAKAAAAKD///8bAAAA" +
    "c2VxdWVudGlhbF8xL2RlbnNlXzEvTWF0TXVsAAIAAAABAAAAAQAAAAAAFgAYABQA" +
    "AAAQAAwACAAAAAAAAAAHABYAAAAAAAABFAAAABQAAAABAAAAGAAAAAQABAAEAAAA" +
    "BwAAAGlucHV0X2MAAgAAAAEAAAABAAAAAQAAABAAAAAMAAwACwAAAAAABAAMAAAA" +
    "CQAAAAAAAAk="

fun decodeBase64(base64: String): ByteArray {
    val table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val clean = base64.replace("\n", "").replace("\r", "").replace(" ", "").replace("=", "")
    val output = mutableListOf<Byte>()
    var bitBuffer = 0
    var bitCount = 0
    for (char in clean) {
        val value = table.indexOf(char)
        if (value == -1) continue
        bitBuffer = (bitBuffer shl 6) or value
        bitCount += 6
        if (bitCount >= 8) {
            bitCount -= 8
            output.add(((bitBuffer shr bitCount) and 0xFF).toByte())
            bitBuffer = bitBuffer and ((1 shl bitCount) - 1)
        }
    }
    return output.toByteArray()
}
