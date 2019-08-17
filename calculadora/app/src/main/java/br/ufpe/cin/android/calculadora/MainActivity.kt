package br.ufpe.cin.android.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    private var expr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (button in arrayListOf(
            btn_0,
            btn_1,
            btn_2,
            btn_3,
            btn_4,
            btn_5,
            btn_6,
            btn_7,
            btn_8,
            btn_9,
            btn_Dot,
            btn_Add,
            btn_Subtract,
            btn_Multiply,
            btn_Divide,
            btn_Power,
            btn_LParen,
            btn_RParen
        )) {
            button.setOnClickListener {
                if (expr.length < 15) { // Limits the size of the expression
                    expr += button.text // Appends the character from the pressed button
                    updateInputField() // Updates the input field
                }
            }
        }

        btn_Equal.setOnClickListener {
            updateResultField() // Updates the result field
        }

        btn_Clear.setOnClickListener {
            clearInputField() // Clears the input field
            clearResultField() // Clears the result field
        }

        btn_Backspace.setOnClickListener {
            if (expr != "") { // Pops the last character out of the expression
                expr = expr.substring(0, expr.length - 1)
                updateInputField()
            }
        }
    }

    private fun clearInputField() {
        expr = "" // Clears expression
        text_calc.setText("") // Clears input field
    }

    private fun clearResultField() {
        text_info.text = "" // Clears result field
    }

    private fun updateInputField() {
        text_calc.setText(expr) // Updates input field

        // Clears the result field if the user is typing a new expression
        if (text_info.text != "") {
            clearResultField()
        }
    }

    private fun updateResultField() {
        try {
            val result = eval(expr) // May throw if the expression is ill-formed

            if (result == truncate(result)) { // Checks if the result is an integer
                text_info.text = result.toInt().toString() // Converts the result to an integer and update the result field
            } else {
                text_info.text = result.toString() // Updates the result field
            }
        } catch (e: RuntimeException) { // The expression is ill-formed
            text_info.text = "Error!" // Shows "Error!" in the result field
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show() // Shows the error in a toast
        }
    }

    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    private fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str[pos] else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: $ch")
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+') -> x += parseTerm() // adição
                        eat('-') -> x -= parseTerm() // subtração
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*') -> x *= parseFactor() // multiplicação
                        eat('/') -> {
                            val operand = parseFactor()
                            if (operand == 0.0) throw RuntimeException("Erro: divisão por zero")
                            x /= operand // divisão
                        }
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        "tan" -> tan(Math.toRadians(x))
                        else -> throw RuntimeException("Função desconhecida: $func")
                    }
                } else {
                    throw RuntimeException("Caractere inesperado: $ch")
                }
                if (eat('^')) x = x.pow(parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
