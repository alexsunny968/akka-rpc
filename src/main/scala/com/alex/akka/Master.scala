package com.alex.akka

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Created by Administrator on 2018/2/12.
  */
class Master(val masterHost:String ,val masterPort:Int) extends Actor {

  val idToWorker = new mutable.HashMap[String,WorkerInfo]()

  val workers = new mutable.HashSet[WorkerInfo]()

  val CHECK_INTERVAL = 15000

  override def preStart(): Unit = {
    import context.dispatcher
    context.system.scheduler.schedule(0 millis,15000 millis,self , CheckTimeoutWorker)
  }


  override def receive: Receive = {
    case RegisterWorker(workId,cores,memory) => {

      val workerInfo = new WorkerInfo(workId, cores, memory)

      if (!idToWorker.contains(workId)){
        idToWorker(workId) = workerInfo;
        workers += workerInfo

        sender ! RegisteredWorker(s"akka.tcp://${Master.MASTER_ACTOR_SYSTEM}@$masterHost:$masterPort/user/${Master.MASTER_ACTOR_NAME}");
      }
    }


    case Heartbeat(workId) => {
      val workerInfo = idToWorker(workId)
      val currentTime = System.currentTimeMillis()
      if(workerInfo != null)
        workerInfo.lastHeartbeatTime = currentTime
    }


    case  CheckTimeoutWorker =>{
      val currentTime = System.currentTimeMillis()

      val deadWokers :mutable.HashSet[WorkerInfo] =  workers.filter(w => currentTime - w.lastHeartbeatTime > CHECK_INTERVAL)

      for(w <- deadWokers){
        idToWorker -= w.id
        workers -= w
      }

      println("alive worker size "+workers.size)

    }
  }
}


object Master{

  val MASTER_ACTOR_SYSTEM = "MasterActorSystem";
  val MASTER_ACTOR_NAME = "MasterActor";

  def main(args: Array[String]): Unit = {

    val host = "127.0.0.1"
    val port = 9999

    val confStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin

    val conf = ConfigFactory.parseString(confStr);

    val actorSystem = ActorSystem(MASTER_ACTOR_SYSTEM,conf)

    val masterActor = actorSystem.actorOf(Props(new Master(host,port)),MASTER_ACTOR_NAME)

    //masterActor ! "Hello"



  }
}