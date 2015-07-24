package org.gk.log

/**
 * Created by goku on 2015/7/23.
 */
trait Logger {
  def log(msg:String)
  def info(msg:String) { log("INFO: " + msg) }
  def warn(msg:String) { log("warn: " + msg) }
  def Error(msg:String) { log("Error: " + msg) }
}

trait ConsoleLogger extends Logger{
  def log(msg:String) { println(msg) }
}
trait FileLogger extends Logger {
  def log(msg:String) { println(msg) }
}

object GkConsoleLogger extends ConsoleLogger with Logger
object GkFileLogger extends FileLogger with Logger


