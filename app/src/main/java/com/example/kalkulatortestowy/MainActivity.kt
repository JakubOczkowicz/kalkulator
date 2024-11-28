package com.example.kalkulatortestowy

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.math.BigInteger

class MainActivity : AppCompatActivity() {
    var liczba1: String = ""     // pierwsza liczba
    var liczba2: String = ""     // druga liczba
    var wynik: String = ""       // wynik dzialania
    var operator: String = ""    // wybrane działanie
    var isOpJustPressed: Boolean = false  // informacja czy wlasnie nacisnelismy przycisk operatora jesli tak
    // to zapisujemy pierwsza liczbe i zaczynamy czytac druga liczbe
    var isResultDisplayed: Boolean = false // czy wynik już jest wyświetlony

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    fun onClickDigit(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)

        if (isResultDisplayed) {
            // Resetowanie po obliczeniu, zaczynamy nowe działanie
            workingsTextView.text = ""
            isResultDisplayed = false
        }

        // Dodawanie cyfry do bieżącego działania
        val currentText = workingsTextView.text.toString()
        val digit = (view as Button).text
        workingsTextView.text = if (currentText == "0") digit else currentText + digit
    }




    fun onClickOperator(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
        val resultsTextView = findViewById<TextView>(R.id.resultsTextView)

        // Jeśli wynik jest wyświetlany, zacznij nowe działanie od wyniku
        if (isResultDisplayed) {
            workingsTextView.text = resultsTextView.text
            isResultDisplayed = false
        }

        val currentText = workingsTextView.text.toString()

        // Dodaj operator do działania, jeśli nie ma go na końcu
        if (currentText.isNotEmpty() && currentText.last().isDigit()) {
            workingsTextView.text = "$currentText ${(view as Button).text} "
            operator = (view as Button).text.toString()
            liczba1 = currentText // Pierwsza liczba działania
            isOpJustPressed = true
        }
    }




    fun onClickEqual(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
        val resultsTextView = findViewById<TextView>(R.id.resultsTextView)

        val currentText = workingsTextView.text.toString()

        try {
            // Oblicz wyrażenie
            val result = evaluateExpression(currentText)

            // Wyświetl wynik
            val displayResult = if (result % 1 == 0.0) result.toInt().toString() else result.toString()
            resultsTextView.text = displayResult
            workingsTextView.text = "$currentText ="

            // Aktualizuj stan
            liczba1 = displayResult
            liczba2 = ""
            operator = ""
            isResultDisplayed = true
        } catch (e: ArithmeticException) {
            resultsTextView.text = "Error: ${e.message}"
        } catch (e: Exception) {
            resultsTextView.text = "Error"
        }
    }



    // Obsługa przycisku `C`
    fun onClickC(view: View) {
        liczba1 = ""
        liczba2 = ""
        operator = ""
        isOpJustPressed = false
        isResultDisplayed = false

        findViewById<TextView>(R.id.workingsTextView).text = ""
        findViewById<TextView>(R.id.resultsTextView).text = "0"
    }



    // Obsługa przycisku `CE`
    fun onClickCE(view: View) {
        findViewById<TextView>(R.id.workingsTextView).text = "0"
    }



    // Obsługa przycisku `⌫`
    fun onClickBack(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
        val currentInput = workingsTextView.text.toString()

        if (currentInput.isNotEmpty() && currentInput != "0") {
            workingsTextView.text = currentInput.dropLast(1)
            if (workingsTextView.text.isEmpty()) {
                workingsTextView.text = "0"
            }
        }
    }




    // Obsługa zmiany znaku `+/-`
    fun onClickPlusMinus(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
        val currentText = workingsTextView.text.toString()

        if (currentText.isEmpty()) {
            return // Nic nie rób, jeśli pole jest puste
        }

        // Znajdź ostatnią liczbę w wyrażeniu
        val regex = Regex("(-?\\d+(\\.\\d+)?)\$") // Pasuje do ostatniej liczby (z opcjonalnym minusem i kropką)
        val matchResult = regex.find(currentText)

        if (matchResult != null) {
            val lastNumber = matchResult.value // Ostatnia liczba
            val lastNumberIndex = matchResult.range.first // Indeks startowy liczby w tekście

            try {
                // Negujemy ostatnią liczbę
                val newValue = lastNumber.toBigDecimal().negate()

                // Zastępujemy starą liczbę nową
                val updatedText = currentText.replaceRange(
                    lastNumberIndex,
                    currentText.length,
                    newValue.stripTrailingZeros().toPlainString()
                )
                workingsTextView.text = updatedText
            } catch (e: NumberFormatException) {
                workingsTextView.text = "Error"
            }
        }
    }




    private fun calculateCombinations(n: Int, k: Int): BigInteger {
        // Walidacja - tylko liczby całkowite i n >= k >= 0
        if (n < 0 || k < 0 || n < k) throw IllegalArgumentException("Invalid values for n and k in nCk")

        // Optymalizacja - wykorzystaj symetrię nCk = nC(n-k)
        val effectiveK = if (k > n / 2) n - k else k

        var result = BigInteger.ONE
        for (i in 1..effectiveK) {
            result = result.multiply(BigInteger.valueOf((n - i + 1).toLong()))
                .divide(BigInteger.valueOf(i.toLong()))
        }
        return result
    }



    private fun factorial(num: BigInteger): BigInteger {
        if (num < BigInteger.ZERO) throw IllegalArgumentException("Factorial is not defined for negative numbers")
        return if (num == BigInteger.ZERO || num == BigInteger.ONE) {
            BigInteger.ONE
        } else {
            num * factorial(num - BigInteger.ONE)
        }
    }





    // Obsługa kropki `.`
    fun onClickDot(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
        val currentInput = workingsTextView.text.toString()

        if (!currentInput.contains(".")) {
            workingsTextView.text = "$currentInput."
        }
    }

    fun onClickNewton(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
        val currentText = workingsTextView.text.toString()

        // Sprawdź, czy ostatnim znakiem nie jest już 'C'
        if (!currentText.endsWith("C")) {
            workingsTextView.append("C")
        }
    }




    // Obsługa przycisku si(x) - Sinus Całkowy (Si(x))
    fun onClickSinusIntegral(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
        val resultsTextView = findViewById<TextView>(R.id.resultsTextView)

        try {
            val input = workingsTextView.text.toString().toDouble()
            if (input < 0) {
                resultsTextView.text = "Liczba musi być dodatnia"
                return
            }

            // Obliczanie Sinusa Całkowego Si(x)
            val result = sinusIntegral(input)

            // Wyświetlenie wyniku
            resultsTextView.text = result.toString()
            workingsTextView.text = result.toString()

            // Aktualizacja zmiennej liczba1
            liczba1 = result.toString()
            liczba2 = ""
            operator = ""
            isResultDisplayed = true
        } catch (e: Exception) {
            resultsTextView.text = "Error"
        }
    }



    // Funkcja obliczająca Sinus Całkowy Si(x)
    private fun sinusIntegral(x: Double): Double {
        val steps = 10000  // Liczba kroków (im więcej, tym dokładniej)
        val deltaT = x / steps  // Szerokość pojedynczego przedziału

        var sum = 0.0

        // Całka numeryczna - metoda trapezów
        for (i in 1..steps) {
            val t = i * deltaT
            val term = Math.sin(t) / t  // Funkcja (sin(t) / t)
            sum += term * deltaT  // Dodanie do sumy z uwzględnieniem szerokości przedziału
        }

        // Zwracamy przybliżoną wartość
        return sum
    }


//    //Funkcja Dzeta Riemanna
//    fun onClickF2(view: View) {
//        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
//        val resultsTextView = findViewById<TextView>(R.id.resultsTextView)
//
//        try {
//            // Pobieranie wartości s
//            val s = workingsTextView.text.toString().toDouble()
//
//            if (s <= 1.0) {
//                // Dla s <= 1 funkcja dzeta Riemanna nie jest zbieżna
//                resultsTextView.text = "Error"
//                return
//            }
//
//            // Obliczanie funkcji dzeta Riemanna
//            val result = riemannZeta(s, 1000) // Przybliżenie dla 1000 iteracji
//
//            // Wyświetlenie wyniku
//            resultsTextView.text = result.toString()
//            workingsTextView.text = result.toString()
//
//            // Aktualizacja zmiennych kalkulatora
//            liczba1 = result.toString()
//            liczba2 = ""
//            operator = ""
//            isResultDisplayed = true
//        } catch (e: Exception) {
//            resultsTextView.text = "Error"
//        }
//    }
//
//    // Funkcja pomocnicza do obliczania przybliżenia funkcji dzeta Riemanna
//    private fun riemannZeta(s: Double, iterations: Int): Double {
//        var sum = 0.0
//        for (n in 1..iterations) {
//            sum += 1 / Math.pow(n.toDouble(), s)
//        }
//        return sum
//    }



    //Ciag Fibonacciego
    fun onClickFibonacci(view: View) {
        val workingsTextView = findViewById<TextView>(R.id.workingsTextView)
        val resultsTextView = findViewById<TextView>(R.id.resultsTextView)

        try {
            val input = workingsTextView.text.toString().toInt()
            if (input < 0) {
                resultsTextView.text = "Liczba musi być nieujemna"
                return
            }
            if (input > 111111) {
                resultsTextView.text = "Zbyt duża wartość"
                return
            }

            // Obliczanie n-tego elementu ciągu Fibonacciego
            val result = fibonacci(input)

            // Konwersja wyniku do notacji wykładniczej, jeśli liczba ma więcej niż 10 cyfr
            val resultText = if (result.toString().length > 10) {
                bigIntegerToExponential(result)
            } else {
                result.toString()
            }

            // Wyświetlenie wyniku
            resultsTextView.text = resultText
            workingsTextView.text = resultText

            // Aktualizacja zmiennej liczba1
            liczba1 = resultText
            liczba2 = ""
            operator = ""
            isResultDisplayed = true
        } catch (e: Exception) {
            resultsTextView.text = "Liczba musi być całkowita"
        }
    }



    // Funkcja pomocnicza do obliczania n-tego elementu ciągu Fibonacciego
    private fun fibonacci(n: Int): BigInteger {
        if (n == 0) return BigInteger.valueOf(0)
        if (n == 1) return BigInteger.valueOf(1)

        var a:BigInteger = BigInteger.valueOf(0)
        var b:BigInteger = BigInteger.valueOf(1)
        var fib:BigInteger = BigInteger.valueOf(1)
        for (i in 2..n) {
            fib = a + b
            a = b
            b = fib
        }
        return fib
    }

    private fun bigIntegerToExponential(value: BigInteger): String {
        val valueString = value.toString()
        val length = valueString.length
        val mantissa = valueString.substring(0, 1) + "." + valueString.substring(1, 6) // 5 cyfr po kropce
        return "$mantissa" + "e+$length"
    }
    private fun evaluateExpression(expression: String): Double {
        val operators = setOf("+", "-", "x", "/", "C")
        val precedence = mapOf("+" to 1, "-" to 1, "x" to 2, "/" to 2, "C" to 3)

        val outputQueue = mutableListOf<String>()
        val operatorStack = mutableListOf<String>()

        // Tokenizuj wyrażenie, uwzględniając liczby ujemne
        val tokens = Regex("(-?\\d+(\\.\\d+)?)|[+x/C-]")
            .findAll(expression)
            .map { it.value }
            .toList()

        for (i in tokens.indices) {
            val token = tokens[i]

            when {
                token.toDoubleOrNull() != null -> {
                    // Jeśli to liczba, dodaj do kolejki wyjściowej
                    outputQueue.add(token)
                }
                token in operators -> {
                    // Jeśli to operator, sprawdź priorytety
                    while (operatorStack.isNotEmpty() &&
                        precedence[operatorStack.last()] ?: 0 >= precedence[token]!!) {
                        outputQueue.add(operatorStack.removeAt(operatorStack.lastIndex))
                    }
                    operatorStack.add(token)
                }
            }
        }

        // Przenieś pozostałe operatory na stos wyjściowy
        while (operatorStack.isNotEmpty()) {
            outputQueue.add(operatorStack.removeAt(operatorStack.lastIndex))
        }

        // Oblicz wynik z wyrażenia w notacji odwrotnej (RPN)
        val resultStack = mutableListOf<Double>()
        for (token in outputQueue) {
            when {
                token.toDoubleOrNull() != null -> {
                    resultStack.add(token.toDouble())
                }
                token in operators -> {
                    val b = resultStack.removeAt(resultStack.lastIndex)
                    val a = resultStack.removeAt(resultStack.lastIndex)
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "x" -> a * b
                        "/" -> {
                            if (b == 0.0) throw ArithmeticException("Division by zero")
                            a / b
                        }
                        "C" -> {
                            if (a % 1 != 0.0 || b % 1 != 0.0) {
                                throw IllegalArgumentException("nCk requires integer values")
                            }
                            calculateCombinations(a.toInt(), b.toInt()).toDouble()
                        }
                        else -> throw IllegalArgumentException("Unknown operator: $token")
                    }
                    resultStack.add(result)
                }

            }
        }
        return resultStack.first()
    }

}
