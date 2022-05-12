package connectfour

import kotlin.math.abs
import kotlin.system.exitProcess

fun main() {
    println("Connect Four")
    println("First player's name:")
    val firstPlayer = Player(readln(), "o")
    println("Second player's name:")
    val secondPlayer = Player(readln(), "*")
    ConnectFour(firstPlayer, secondPlayer)
}

class Player(val name: String, val simbol: String) {
    val step = mutableListOf<Pair<Int, Int>>()
    var score = 0
}

class ConnectFour(val first: Player, val second: Player) {
    var activePlayer = first
    var numGames = 1
    var actualNumGame = 1
    var gameBegin = false
    init { initGame() }

    fun initGame() {
        while (true) {
            println("Set the board dimensions (Rows x Columns)\nPress Enter for default (6 x 7)")
            val respones = readln().trim()
            if (respones.isEmpty()) {
                setBoard(6, 7)
                break
            }
            if (respones.matches("\\d+\\s*[xX]\\s*\\d+".toRegex())) {
                val (rows, columns) = respones.split("x", "X").map { it.trim().toInt() }
                if (rows !in 5..9) {
                    println("Board rows should be from 5 to 9")
                    continue
                }
                if (columns !in 5..9) {
                    println("Board columns should be from 5 to 9")
                    continue
                }
                setBoard(rows, columns)
                break
            } else {
                println("Invalid input")
                continue
            }
        }
        while (true) {
            println(
                "Do you want to play single or multiple games?\n" +
                        "For a single game, input 1 or press Enter\n" +
                        "Input a number of games:"
            )
            val n = readln()
            if (n.isEmpty()) { break }

            if (n.matches("[123456789]+\\s*".toRegex())) {
                numGames = n.toInt()
                break
            } else println("Invalid input")
        }

        println(toString())
        runGame()
    }

    fun runGame() {
        while (true) {
            if (numGames > 1 && !gameBegin) {
                println("Game #$actualNumGame")
                gameBegin = true
            }
            println(Board)
            while (true) {
                println("${activePlayer.name}'s turn:")
                val resp = readln()
                if (resp == "end") { exit() }
                val response = try { resp.toInt() } catch (e: NumberFormatException) {
                    println("Incorrect column number")
                    continue
                }
                if (response !in 1..columns) {
                    println("The column number is out of range (1 - $columns)")
                    continue
                }
                if (!stepBoard(response, activePlayer)) {
                    println("Column $response is full")
                    continue
                }

                break
            }
            gameEnd()
            activePlayer = if (activePlayer == first) second else first
        }
    }

    fun gameEnd() {
        val end = {
            println("Score\n${first.name}: ${first.score} ${second.name}: ${second.score}")
            if (actualNumGame == numGames) exit()
            actualNumGame++
            gameBegin = false
            first.step.clear()
            second.step.clear()
            board = MutableList(rows) { MutableList(columns) {" "} }
        }

        if (checkWin(activePlayer.step.last().first, activePlayer.step.last().second)) {
            println(Board)
            println("Player ${activePlayer.name} won")
            end()
        }
        if (isDraw()) {
            println(Board)
            println("It is a draw")
            end()
        }

    }

    fun checkWin( _rows: Int, _columns: Int ): Boolean {
        if (activePlayer.step.size < 4) return false

        fun isVertical(x: Int, y: Int): Boolean {
            val checkList = { _x:Int, _y: Int -> listOf(abs(_x) to _y, abs(_x + 1) to _y, abs(_x + 2) to _y, abs(_x + 3) to _y) }
            return activePlayer.step.containsAll(checkList(x, y)) || activePlayer.step.containsAll(checkList(-x, y))
        }

        if (isVertical(_rows, _columns)) { activePlayer.score += 2; return true }

        fun isHorizontal(x: Int, y: Int): Boolean {
            val checkList = { _x:Int, _y: Int -> listOf(_x to abs(_y), _x to abs(_y + 1), _x to abs(_y + 2), _x to abs(_y + 3)) }
            return activePlayer.step.containsAll(checkList(x, -y)) || activePlayer.step.containsAll(checkList(x, y))
        }

        if (isHorizontal(_rows, _columns)) { activePlayer.score += 2; return true }

        fun isDiagonal(x: Int, y: Int): Boolean {
            val checkList = { _x:Int, _y: Int -> listOf(abs(_x) to abs(_y), abs(_x + 1) to abs(_y + 1), abs(_x + 2) to abs(_y + 2), abs(_x + 3) to abs(_y + 3)) }
            if (activePlayer.step.containsAll(checkList(x, y))) return true
            if (x >= 3 && activePlayer.step.containsAll(checkList(-x, y))) return true
            if (y >= 3 && activePlayer.step.containsAll(checkList(x, -y))) return true
            if (x >= 3 && y >= 3 && activePlayer.step.containsAll(checkList(-x, -y))) return true
            return false
        }

        if (isDiagonal(_rows, _columns)) { activePlayer.score += 2;return true }

        return false
    }

    fun isDraw(): Boolean {
        for (i in 0..columns - 1) {
            if (board[0][i] == " ") return false
        }
        first.score += 1
        second.score += 1
        return true
    }

    fun exit() {
        println("Game Over!")
        exitProcess(0)
    }

    companion object Board {
        var board = mutableListOf<MutableList<String>>()
        var rows = 0
        var columns = 0
        fun setBoard(_rows: Int, _columns: Int) {
            rows = _rows
            columns = _columns
            board = MutableList(rows) { MutableList(columns) {" "} }
        }
        fun stringBoard(): String {
            var str = "\n"
            var strHight = ""
            var strDown = "${'\u255A'}"
            for (j in 1..columns) {
                strHight += " $j"
                if (j != columns) {
                    strDown += "${'\u2550'}".repeat(1) + "${'\u2569'}"
                } else {
                    strDown += "${'\u2550'}".repeat(1) + "${'\u255D'}"
                }
            }
            var strMiddle = "\n"
            for (i in 1..rows) {
                strMiddle += "${'\u2551'}"
                for (j in 1..columns) {
                    strMiddle += "${board[i - 1][j - 1]}${'\u2551'}"
                    if (j == columns) strMiddle += "\n"
                }
            }
            str += strHight + strMiddle + strDown
            return str
        }
        override fun toString(): String = stringBoard()
        fun stepBoard(_columns: Int, _activPlayer: Player): Boolean {
            for (i in board.lastIndex downTo 0) {
                if (board[i][_columns - 1] == " ") {
                    board[i][_columns - 1] = _activPlayer.simbol
                    _activPlayer.step.add(Pair(i, _columns - 1))
                    return true
                }
            }
            return false
        }
    }
    override fun toString(): String {
        return if (numGames == 1) {
            "${first.name} VS ${second.name}\n$rows x $columns board\nSingle game"
        } else {
            "${first.name} VS ${second.name}\n$rows x $columns board\nTotal $numGames games"
        }
    }
}