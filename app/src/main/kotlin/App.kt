package io.github.dmitrysulman.app

import io.github.dmitrysulman.utils.Printer

// This is the main entry point of the application.
// It uses the `Printer` class from the `:utils` subproject.
fun main() {
    val message = "Hello JetBrains!"
    val printer = Printer(message)
    printer.printMessage()
}
