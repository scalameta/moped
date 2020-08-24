package moped.reporters

class Terminals(tput: Tput) {
  def screenWidth(): Int = {
    screenWidth(lowerBound = 40, upperBound = 100)
  }
  def screenWidth(lowerBound: Int, upperBound: Int): Int = {
    math.min(
      upperBound,
      math.max(lowerBound, tput.cols().getOrElse(80) - 20)
    )
  }
}
