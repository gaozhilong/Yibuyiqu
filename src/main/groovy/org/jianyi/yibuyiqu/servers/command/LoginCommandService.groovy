package org.jianyi.yibuyiqu.servers.command

import io.vertx.core.logging.Logger

import org.jianyi.yibuyiqu.command.CommandUtil
import org.jianyi.yibuyiqu.command.Result
import org.jianyi.yibuyiqu.command.Result.ResultType
import org.jianyi.yibuyiqu.servers.command.CommandService.CommandAttribute

class LoginCommandService extends CommandService {
	
	def execute(message) {
		// TODO Auto-generated method stub
		log.info("execute Login Command")
		vertx.eventBus().send("server.author.login", message.body(), {
					reply -> 
						if (reply.result().body().asBoolean()) {
							Result result = new Result(message.body().getString(CommandAttribute.sessionid.toString()),
									"登录成功", ResultType.SUCCESS.toString())
							message.reply(result.toJsonObject())
							sendMsg(result.toJsonObject())
						} else {
							Result result = new Result(message.body().getString(CommandAttribute.sessionid.toString()),
									"登录失败", ResultType.ERROR.toString())
							message.reply(result.toJsonObject())
							sendMsg(result.toJsonObject())
						}
					}
				)

	}
}
