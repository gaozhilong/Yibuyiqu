package org.jianyi.yibuyiqu.servers.auth

import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.LocalMap
import io.vertx.ext.sync.SyncVerticle

import org.jianyi.yibuyiqu.cache.Cache
import org.jianyi.yibuyiqu.cache.Cache.DataCache
import org.jianyi.yibuyiqu.servers.command.CommandService.CommandAttribute
import org.jianyi.yibuyiqu.storage.cassandra.CassandraDao
import org.jianyi.yibuyiqu.storage.model.DBObject
import org.jianyi.yibuyiqu.utils.JsonUril
import org.jianyi.yibuyiqu.utils.ConfigUtil.ApplicationCfg
import org.jianyi.yibuyiqu.utils.ConfigUtil.Database
import org.jianyi.yibuyiqu.utils.ConfigUtil.Session
import org.jianyi.yibuyiqu.utils.ConfigUtil.ShareMap
import org.mindrot.jbcrypt.BCrypt

import com.google.common.collect.Maps
import com.hazelcast.core.HazelcastInstance

class CassandraAuthServer extends SyncVerticle {

	private static final Logger log = LoggerFactory.getLogger(CassandraAuthServer.class)

	private HazelcastInstance hazelcastInstance

	void start() {
		hazelcastInstance = Cache.getHazelcastInstance()
		def eb = vertx.eventBus()

		eb.consumer("server.author.login", { message -> login(message) })
		eb.consumer("server.author.logout", { message -> logout(message) })
		eb.consumer("server.author.authorise", { message -> authorise(message) })
	}

	void stop() {
	}

	//def private re

	def private login(message) {
		String sessionID = message.body().getString(CommandAttribute.sessionid.toString())
		String username = message.body().getString(Database.username.toString())
		String password = message.body().getString(Database.password.toString())
		String proxy = message.body().getString(CommandAttribute.proxy.toString())
		if (username != null || password != null) {
			JsonObject userdata = findUserByName(username)
			if (userdata == null) {
				log.info("用户不存在！")
				message.reply(false)
			} else {
				if (BCrypt.checkpw(password, userdata.getString(Session.password.toString()))) {
					String oldSessionID = userdata.getString(CommandAttribute.sessionid.toString().toString())
					JsonObject session
					if (oldSessionID != null) {
						session = get(oldSessionID)
					}
					if (session == null) {
						session = new JsonObject()
					}
					session.put(CommandAttribute.sessionid.toString(), sessionID)
					session.put(Database.username.toString(), username)
					session.put(ApplicationCfg.id.toString(), userdata.getString(ApplicationCfg.id.toString()))
					JsonObject sessionVal = new JsonObject()
					sessionVal.put(Database.username.toString(), username)
					session.put(Session.sessionVal.toString(), sessionVal.toString())
					session.put(Session.createtime.toString(), String.valueOf(System.nanoTime()))
					session.put(CommandAttribute.proxy.toString(), proxy)
					if (create(session)) {
						log.info("Session信息存储成功")
						message.reply(true)
					} else {
						log.info("Session信息存储失败！")
						message.reply(false)
					}
				} else {
					log.info("用户口令不正确！")
					message.reply(false)
				}
			}
		}
	}

	def private logout(message) {
		if (destroy(message.body().getString(CommandAttribute.sessionid.toString()))) {
			message.reply(true)
			log.info("Session信息销毁成功")
		} else {
			message.reply(false)
			log.info("Session信息销毁失败！")
		}
	}

	def private authorise(message) {
		JsonObject session = get(message.body().getString(CommandAttribute.sessionid.toString()))
		if (session == null) {
			message.reply(false)
		} else {
			message.reply(true)
		}
	}

	def private findUserByName(username) {
		JsonObject jsonObject = null
		CassandraDao dao = new CassandraDao()
		JsonObject where = new JsonObject()
		where.put("name", username)
		List<DBObject> result = dao.getObjectsByParams("users", where)
		if (result != null && !result.isEmpty()) {
			jsonObject = result.get(0).getJsonObject()
		}
		return jsonObject
	}

	private create(JsonObject session) {
		LocalMap<String, String> map = vertx.sharedData().getLocalMap(ShareMap.user.toString())
		Map<String, String> clientMap = Maps.newHashMap()
		clientMap.put(CommandAttribute.proxy.toString(),
				session.getString(CommandAttribute.proxy.toString()))
		String jsons = JsonUril.objectToJsonStr(clientMap)
		map.put(session.getString(CommandAttribute.sessionid.toString()), jsons)

		hazelcastInstance.getMap(DataCache.SESSIONS.toString()).put(
				session.getString(CommandAttribute.sessionid.toString()), session.getMap())
		String loaduser = config().getString(Session.loaduseraddress.toString())
		if (loaduser != null) {
			vertx.eventBus().send(loaduser, session)
		}
		return true;
	}

	private get(String sessionId) {
		JsonObject sessionVal = new JsonObject(hazelcastInstance.getMap(DataCache.SESSIONS.toString()).get(sessionId))
		return sessionVal;
	}

	private clear(String sessionId) {
		LocalMap<String, String> map = vertx.sharedData().getLocalMap(ShareMap.user.toString());
		map.remove(sessionId);
		hazelcastInstance.getMap(DataCache.SESSIONS.toString()).remove(sessionId);
		String unloaduser = config().getString(Session.unloaduseraddress.toString())
		if (unloaduser != null) {
			JsonObject session = new JsonObject()
			session.put(CommandAttribute.sessionid.toString(), sessionId)
			vertx.eventBus().send(unloaduser, session)
		}
		return true;
	}

	private destroy(String sessionId) {
		LocalMap<String, String> map = vertx.sharedData().getLocalMap(ShareMap.user.toString());
		map.remove(sessionId);
		hazelcastInstance.getMap(DataCache.SESSIONS.toString()).remove(sessionId);
		String unloaduser = config().getString(Session.unloaduseraddress.toString())
		if (unloaduser != null) {
			JsonObject session = new JsonObject()
			session.put(CommandAttribute.sessionid.toString(), sessionId)
			vertx.eventBus().send(unloaduser, session)
		}
		return true;
	}
}
