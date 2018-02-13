package com.alex.akka

/**
  * Created by Administrator on 2018/2/12.
  */
class WorkerInfo (val id:String , val cores : Int, val memory: Int){

  var lastHeartbeatTime :Long = _

}
