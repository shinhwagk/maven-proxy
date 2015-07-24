package org.gk.config

/**
 * Created by goku on 2015/7/24.
 */
object name {
//  val RequertSender = format("Requert发送者")
  val RequertHandler = "<Requert处理者>"
}
case class format(l:String = "<",name:String,R:String=">")
