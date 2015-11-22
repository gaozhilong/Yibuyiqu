package org.jianyi.yibuyiqu.servers.command

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.LocalMap

import org.jianyi.yibuyiqu.utils.ConfigUtil.ApplicationCfg
import org.jianyi.yibuyiqu.utils.ConfigUtil.Session
import org.jianyi.yibuyiqu.utils.ConfigUtil.ShareMap

class CommandService extends AbstractVerticle {
	
	public enum CommandAttribute { command,sessionid,proxy,result,message,group }
	
	static final Logger log = LoggerFactory.getLogger(CommandService.class)
	
	void start() {
		JsonObject cfg = config()
		String address = cfg.getString(ApplicationCfg.address.toString())
		def eb = vertx.eventBus()
		//eb.consumer(address, { message -> execute(message) })
		eb.consumer(address, { message -> executeWithLog(message, cfg) })
	}
	
	//发送消息到制定客户端
	def sendMsg(msg) {
		LocalMap<String,String> map = vertx.sharedData().getLocalMap(ShareMap.alluser.toString())
		JsonObject json = new JsonObject(map.get(msg.getString(CommandAttribute.sessionid.toString())))
		vertx.eventBus().send("server."+json.getString(CommandAttribute.proxy.toString())+".send", msg)
	}
	
	def execute(message) {}
	
	def executeWithLog(message, cfg) {
		long startTime = System.nanoTime()
		execute(message)
		long endTime = System.nanoTime()
		long duration = endTime - startTime
		JsonObject log = new JsonObject()
		log.put(ApplicationCfg.name.toString(), cfg.getString(ApplicationCfg.name.toString()))
		log.put(ApplicationCfg.verticlefile.toString(), cfg.getString(ApplicationCfg.verticlefile.toString()))
		log.put(Session.duration.toString(), duration)
		vertx.eventBus().send("server.log.runtime",	log)
	}
	
	

}
