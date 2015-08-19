package org.gk.client.centrol

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem}
import com.typesafe.config.ConfigFactory
import org.gk.server.centrol.CommandServer._

/**
 * Created by goku on 2015/8/10.
 */
class CommandClient extends Actor {
  val commandServer = context.actorSelection("akka.tcp://MavenProxyServer@127.0.0.1:2552/user/CommandServer")


  override def receive: Receive = {
    case "add" => {
      import scala.io.StdIn._
      val repoName = readLine("请输入仓库名称:")
      val repoUrl = readLine("请输入仓库地址:")

      print("请输入此仓库的优先级:")
      val priority = readInt()

      val start = readLine("否现在启动[Y/N]:") match {
        case "Y" => true
        case "N" => false
        case _ => throw new Exception("输入错误. 请输入[Y/N].")
      }
      commandServer ! addRepository(repoName, repoUrl, priority, start)
    }
    case "del" => {
      import scala.io.StdIn._
      val repoName = readLine("请输入仓库名称:")
      commandServer ! deleteRepository(repoName)
    }
    case "list" => {
      commandServer ! listRepository
    }
    case "ok" =>
      context.system.shutdown()
      System.exit(0)
    case a:List[(String,String,Int,Boolean)]=>
      a.map(println(_))
      context.system.shutdown()
      System.exit(0)

  }
}
