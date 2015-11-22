package org.jianyi.yibuyiqu.command

import io.vertx.core.json.JsonObject

import org.jianyi.yibuyiqu.servers.command.CommandService.CommandAttribute

class CommandUtil {

	def static getCommands(message) {
		JsonObject cmd = new JsonObject(message)
		return cmd
	}

	def static getCommands(message, sessionId) {
		JsonObject cmd = new JsonObject(message)
		cmd.put(CommandAttribute.sessionid.toString().toString(), sessionId)
		return cmd
	}
}
