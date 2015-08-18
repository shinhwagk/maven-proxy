package org.gk.server

import java.io.{File, FileWriter}

import org.gk.server.workers.RequestHeaders
/**
 * Created by goku on 2015/8/18.
 */
object FileWriterTest {
  val file = new File("./aaa")
  val writer = new FileWriter(file.getName, true);
  def insert(headers:RequestHeaders): Unit ={
    headers.headText.split("\r\n").foreach(p=>{
      writer.write(p+"\r\n")
      writer.write("\r\n")
      writer.write("\r\n")
    })
  }
}
