package com.alex.akka

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
/**
  * Created by Administrator on 2018/2/12.
  */
class Worker(val cores: Int,val memory: Int, val masterHost:String,val masterPort:Int) extends Actor {

  var masterActor : ActorSelection = _
  val workerId = UUID.randomUUID().toString

  var masterUrl:String = _

  override def preStart(): Unit = {
    masterActor =  context.actorSelection(s"akka.tcp://${Master.MASTER_ACTOR_SYSTEM}@$masterHost:$masterPort/user/${Master.MASTER_ACTOR_NAME}")
    masterActor ! RegisterWorker(workerId,cores,memory)
  }

  override def receive: Receive = {
    case RegisteredWorker(masterUrl) => {
      this.masterUrl = masterUrl

      //schedule heartbeat
      import context.dispatcher
      context.system.scheduler.schedule(0 millis,10000 millis,self,SendHeartbeat)
    }



    case SendHeartbeat => {
      masterActor ! Heartbeat(workerId)
    }

  }
}

object Worker{

  def main(args: Array[String]): Unit = {

    val host = "127.0.0.1"
    val port = 9900

    val cores = 2
    val memory = 512

    val masterHost = "127.0.0.1"
    val masterPort = 9999

    val confStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin

    val conf = ConfigFactory.parseString(confStr);

    val actorSystem = ActorSystem("WorkerActorSystem",conf)

    val workerActor = actorSystem.actorOf(Props(new Worker(cores,memory,masterHost,masterPort)),"WorkerActor")



  }
}
