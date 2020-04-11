package com.alimin.hwvc.screen.ext

fun Int.align16(): Int {
    return (this shr 4) + (this and 0xF shr 3) shl 4
}