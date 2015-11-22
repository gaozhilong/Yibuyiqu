package org.jianyi.yibuyiqu.servers.input

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.LocalMap

import org.jianyi.yibuyiqu.cache.Cache
import org.jianyi.yibuyiqu.cache.Cache.DataCache
import org.jianyi.yibuyiqu.command.Result
import org.jianyi.yibuyiqu.command.Result.ResultType
import org.jianyi.yibuyiqu.servers.command.CommandService.CommandAttribute
import org.jianyi.yibuyiqu.utils.ConfigUtil.ApplicationCfg
import org.jianyi.yibuyiqu.utils.ConfigUtil.ShareMap

import rx.Observable
import rx.functions.Action1

import com.hazelcast.core.HazelcastInstance

class InputServer extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(InputServer.class)
	
	private HazelcastInstance hazelcastInstance

	public void start() {

		JsonObject cfg = config()
		String address = cfg.getString(ApplicationCfg.address.toString())
		boolean isAuthor = cfg.getBoolean(ApplicationCfg.author.toString())
		
		hazelcastInstance = Cache.getHazelcastInstance()

		def eb = vertx.eventBus()
		eb.consumer(address, { message ->
			log.info("server.input")
			JsonObject command = message.body()
			boolean author = true
			String cmdType = command.getString(CommandAttribute.command.toString())
			if (isAuthor) {
				LocalMap<String,String> users = vertx.sharedData().getLocalMap(ShareMap.alluser.toString())
				
				if (!hazelcastInstance.getMap(DataCache.SESSIONS.toString()).containsKey(command.getString(CommandAttribute.sessionid.toString()))) {
					if (cmdType != null && cmdType != "login") {
						users.remove(command.getString(CommandAttribute.sessionid.toString()))
						author = false
					}
				}
			}

			if (author) {
				LocalMap<String, String> commandMap = vertx.sharedData().getLocalMap(ShareMap.commandMap.toString())
				Observable.just(command).subscribe(
						new Action1<JsonObject>() {
							@Override
							public void call(JsonObject commandObj) {
								String sessionId = commandObj.getString(CommandAttribute.sessionid.toString())
								String type = commandObj.getString(CommandAttribute.command.toString())
								if (type != null
								&& commandMap.keySet().contains(type)) {
									logCall(commandObj)
									eb.send(
											commandMap.get(type),
											commandObj,{ reply ->
												log(reply.result().body())
											}
											)
								} else {
									String msg = "命令没有相应的处理服务"
									if (type == null) {
										msg = "命令类型为空"
									}
									Result result = new Result(
											sessionId, msg, ResultType.ERROR.toString())
									log(result)
									sendMsg(result.toJsonObject())
								}
							}
						})
			} else {
				String sessionId = command.getString(CommandAttribute.sessionid.toString())
				Result result = new Result(
						command.getString(CommandAttribute.sessionid.toString()), "没有登录", ResultType.ERROR.toString())
				log(result)
				sendMsg(result.toJsonObject())
			}
		})
	}

	def private logCall(message) {
		JsonObject log = new JsonObject()
		String type = message.getString(CommandAttribute.command.toString())
		String sessionId = message.getString(CommandAttribute.sessionid.toString())
		log.put(ApplicationCfg.type.toString(), type)
		//log.putString("username", message.getString("username"))
		log.put(CommandAttribute.sessionid.toString(), sessionId)
		log.put(CommandAttribute.result.toString(),	"call")
		vertx.eventBus().send("server.log", log)
	}

	def private log(Result result) {
		JsonObject log = new JsonObject()
		log.put(ApplicationCfg.type.toString(), "用户命令-" + result.getResult())
		log.put(CommandAttribute.sessionid.toString(), result.getSessionID())
		log.put(CommandAttribute.result.toString(), result.getMessage())
		vertx.eventBus().send("server.log",	log)
	}

	def private log(JsonObject result) {
		JsonObject log = new JsonObject()
		log.put(ApplicationCfg.type.toString(), "用户命令执行结果")
		log.put(CommandAttribute.sessionid.toString(), result.getString(CommandAttribute.sessionid.toString()))
		log.put(CommandAttribute.result.toString(), result.getString(CommandAttribute.result.toString()))
		log.put(CommandAttribute.message.toString(), result.getString(CommandAttribute.message.toString()))
		vertx.eventBus().send("server.log",	log)
	}

	def sendMsg(JsonObject msg) {
		LocalMap<String,String> map = vertx.sharedData().getLocalMap(ShareMap.alluser.toString())
		JsonObject json = new JsonObject(map.get(msg.getString(CommandAttribute.sessionid.toString())))
		vertx.eventBus().send("server."+json.getString(CommandAttribute.proxy.toString())+".send", msg)
	}
}
