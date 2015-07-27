import java.io.RandomAccessFile
import java.net.{URL, HttpURLConnection}

import akka.actor.{Actor, Props, ActorSystem}
import akka.actor.Actor.Receive
import akka.routing.RoundRobinPool
import org.gk.config.cfg

/**
 * Created by goku on 2015/7/27.
 */



