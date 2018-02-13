package com.alex.akka

/**
  * Created by Administrator on 2018/2/12.
  */
trait Message extends Serializable {
}

case class RegisterWorker(id: String, cores: Int , memory: Int) extends Message;

case class RegisteredWorker(masterUrl: String) extends Message;

case class Heartbeat(id: String) extends Message;

case object SendHeartbeat;

case object CheckTimeoutWorker;