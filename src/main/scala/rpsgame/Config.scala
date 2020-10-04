package rpsgame


import scopt._

import scala.Console.{BLUE, RED}

case class Config(players: Seq[Player] = Nil, games: Int = 1, console: Console = new Console){
  def withPlayer(p: Player) = copy(players = players :+ p)
  def host: Player = players.head
  def guest: Player = players.last
}

object Config {
  def parse(cmdline:String*): Option[Config] ={
    implicit val handRead: Read[Hand] = Read.reads(Hand.select)
    new scopt.OptionParser[Config]("sbt run"){
      opt[Seq[Hand]]("wheel").abbr("wh").action{
        case (hands, _) if hands.size < 2 =>  throw new IllegalArgumentException("Wheel needs at least 2 hands")
        case (hands, g) => g.withPlayer(Wheel(hands:_*))
      }.valueName("<hand>,<hand>,..").unbounded()

      opt[Hand]("statue").abbr("st").action{
        case (hand,g) => g.withPlayer(Statue(hand))
      }.valueName("<hand>").unbounded()

      opt[Unit]("noise").abbr("n").action{  (_,g) =>
        g.withPlayer(Noise())
      }.unbounded()

      opt[String]("human").abbr("h").action { (name,c) =>
        val p = new HumanPlayer(name, if(c.players.isEmpty) RED else BLUE)
        c.withPlayer(p).copy(console = p)
      }.maxOccurs(1).valueName("<your nmae>")
        .text("a human player, specify to play against computer")

      arg[Int]("#games").action { (no,g) => g.copy(games = no) }
        .optional().withFallback(() =>1)

      checkConfig {
        g => if (g.players.size < 2) Left(s"${g.players} is not enough, we need exactly 2 players")
        else if(g.players.size > 2 ) Left(s"${g.players} is too much, we need exactly 2 players")
        else Right()
      }
    }.parse(cmdline, Config())
  }
}



