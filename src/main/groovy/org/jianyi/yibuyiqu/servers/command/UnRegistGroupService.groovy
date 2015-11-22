package org.jianyi.yibuyiqu.servers.command

import io.vertx.core.json.JsonObject

import org.jianyi.yibuyiqu.cache.Cache
import org.jianyi.yibuyiqu.cache.Cache.DataCache
import org.jianyi.yibuyiqu.command.CommandUtil
import org.jianyi.yibuyiqu.command.Result
import org.jianyi.yibuyiqu.command.Result.ResultType
import org.jianyi.yibuyiqu.group.Group
import org.jianyi.yibuyiqu.servers.command.CommandService.CommandAttribute

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap

class UnRegistGroupService extends CommandService {
	
	private HazelcastInstance hazelcastInstance
	
	def execute(message) {
		log.info("execute UnRegistGroup Command")
		hazelcastInstance = Cache.getHazelcastInstance()
		JsonObject msg = message.body()
		def name = msg.getString(CommandAttribute.group.toString())
		Result result
		IMap<String,Group> map = hazelcastInstance.getMap(DataCache.GROUPS.toString())
		if (!map.containsKey(name)) {
			result = new Result(msg.getString(CommandAttribute.sessionid.toString()), "Group名称:"+name+"不存在", ResultType.ERROR.toString())
			sendMsg(result.toJsonObject())
		} else {
			vertx.eventBus().send("group." + name + ".unregist", msg, { reply ->
				if (reply.result().body() != null) {
					result = new Result(msg.getString(CommandAttribute.sessionid.toString()), "Group命令:"+msg.getString(CommandAttribute.command.toString())+"执行成功", ResultType.ERROR.toString())
					sendMsg(reply.result().body())
				} else {
					result = new Result(msg.getString(CommandAttribute.sessionid.toString()), "Group命令:"+msg.getString(CommandAttribute.command.toString())+"执行失败", ResultType.ERROR.toString())
					sendMsg(result.toJsonObject())
				}
			})
		}
		message.reply(result.toJsonObject())
	}

}
