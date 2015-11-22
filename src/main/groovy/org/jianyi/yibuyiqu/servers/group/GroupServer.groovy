package org.jianyi.yibuyiqu.servers.group

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

import org.jianyi.yibuyiqu.cache.Cache
import org.jianyi.yibuyiqu.cache.Cache.DataCache
import org.jianyi.yibuyiqu.command.Result
import org.jianyi.yibuyiqu.command.Result.ResultType
import org.jianyi.yibuyiqu.group.Group
import org.jianyi.yibuyiqu.group.Group.GroupCollections
import org.jianyi.yibuyiqu.servers.command.CommandService.CommandAttribute
import org.jianyi.yibuyiqu.utils.ConfigUtil.Database

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap

class GroupServer extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(GroupServer.class)

	private HazelcastInstance hazelcastInstance

	String name

	void start() {

		hazelcastInstance = Cache.getHazelcastInstance()

		JsonObject groupCfg = config()

		Group group = new Group(groupCfg)
		name = group.name

		IMap<String,Group> map = hazelcastInstance.getMap(DataCache.GROUPS.toString())
		if (!map.containsKey(name)) {
			map.put(name, group)
		}

		def eb = vertx.eventBus()

		eb.consumer("group." + name + ".broadcast", { message ->
			if (message.body().size() > 0) {
				Result result = sendMsg(msg)
				message.reply(result.toJsonObject())
			}
		})

		eb.consumer("group." + name + ".regist", { message ->
			if (message.body().size() > 0) {
				Result result = regist(message)
				message.reply(result.toJsonObject())
			}
		})

		eb.consumer("group." + name + ".unregist", { message ->
			if (message.body().size() > 0) {
				Result result = unregist(message)
				message.reply(result.toJsonObject())
			}
		})
	}

	private regist(message) {
		def sessionID = message.body().getString(CommandAttribute.sessionid.toString())
		Map<String, String> session = hazelcastInstance.getMap(DataCache.SESSIONS).get(sessionID)
		def username = session.get(Database.username.toString())
		Group group = hazelcastInstance.getMap(DataCache.GROUPS.toString()).get(name)
		Set<String> sessions = group.objects.get(GroupCollections.sessions.toString())
		sessions.add(sessionID)
		group.objects.put(GroupCollections.sessions.toString(), sessions)

		Set<String> users = group.objects.get(GroupCollections.users.toString())
		users.add(username)
		hazelcastInstance.getMap(DataCache.GROUPS.toString()).get(name).objects.put(GroupCollections.users.toString(), users)
		Result result = new Result(sessionID, "Group注册成功", ResultType.SUCCESS.toString())
		return result
	}


	private unregist(message) {
		def sessionID = message.body().getString(CommandAttribute.sessionid.toString())
		Map<String, String> session = hazelcastInstance.getMap(DataCache.SESSIONS.toString()).get(sessionID)
		def username = session.get(Database.username.toString())
		Group group = hazelcastInstance.getMap(DataCache.GROUPS.toString()).get(name)
		Set<String> sessions = group.objects.get(GroupCollections.sessions.toString())
		Result result
		if (sessions.contains(sessionID)) {
			sessions.remove(sessionID)
			group.objects.put(GroupCollections.sessions.toString(), sessions)
			Set<String> users = group.objects.get(GroupCollections.users.toString())
			users.remove(username)
			group.objects.put(GroupCollections.users.toString(), users)
			result = new Result(sessionID, "Group注销成功", ResultType.SUCCESS.toString())
		} else {
			result = new Result(sessionID, "Group:"+name+"中没有注册当前用户", ResultType.SUCCESS.toString())
		}
		return result
	}

	public sendMsg(msg) {
		def sessionID = message.body().getString(CommandAttribute.sessionid.toString())
		Set<String> sessions = hazelcastInstance.getMap(DataCache.GROUPS.toString()).get(name).objects.get(GroupCollections.sessions.toString())
		if (sessions != null) {
			sessions.each {
				vertx.eventBus().send("server."+hazelcastInstance.getMap(DataCache.SESSIONS.toString()).get(it).get(CommandAttribute.proxy.toString())+".send", msg)
			}
		}
		Result result = new Result(sessionID, "Group:"+name+",消息广播成功", ResultType.SUCCESS.toString())
		return result
	}

}
