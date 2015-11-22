package org.jianyi.yibuyiqu.servers.socket

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.impl.BufferImpl
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.LocalMap
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions

import org.jianyi.yibuyiqu.servers.command.CommandService.CommandAttribute
import org.jianyi.yibuyiqu.utils.JsonUril
import org.jianyi.yibuyiqu.utils.ConfigUtil.ApplicationCfg
import org.jianyi.yibuyiqu.utils.ConfigUtil.Session
import org.jianyi.yibuyiqu.utils.ConfigUtil.ShareMap

class SockJSServer extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(SockJSServer.class)

	void start() {
		// TODO Auto-generated method stub

		JsonObject cfg = config()
		String servername = cfg.getString(ApplicationCfg.name.toString())

		def server = vertx.createHttpServer()

		def router = Router.router(vertx)

		def options = new SockJSHandlerOptions().setHeartbeatInterval(2000)

		def sockJSHandler = SockJSHandler.create(vertx, options)
		
		def eb = vertx.eventBus()
		
		sockJSHandler.socketHandler({ sockJSSocket ->
			log.info("A client has connected! SID:"	+ sockJSSocket.writeHandlerID())
			JsonObject log = new JsonObject()
			log.put("type", "客户端连接！")
			log.put("ip",sockJSSocket.remoteAddress().host())
			eb.send("server.log", log)
			LocalMap<String,String> map = vertx.sharedData().getLocalMap(ShareMap.alluser.toString())
			Map<String, String> clientMap = new HashMap<String, String>()
			clientMap.put(CommandAttribute.proxy.toString(), servername)
			map.put(sockJSSocket.writeHandlerID(), JsonUril.objectToJsonStr(clientMap))
			sockJSSocket.handler({data ->
				JsonObject message = null
				try {
					message = new JsonObject(data.toString())
					message.put(CommandAttribute.sessionid.toString(), sockJSSocket.writeHandlerID())
					message.put(CommandAttribute.proxy.toString(), servername)
					eb.send("server.input", message)
				} catch (Exception e) {
					log = new JsonObject()
					log.put(Session.value.toString(),data.toString())
					log.put(ApplicationCfg.type.toString(),
							"用户命令-命令格式错误，不是有效的JSON字符串")
					log.put(CommandAttribute.result.toString(),
							"用户命令-命令格式错误，不是有效的JSON字符串")
					eb.send("server.log", log)
					eb.send(sockJSSocket.writeHandlerID(), new BufferImpl("用户命令-命令格式错误，不是有效的JSON字符串"))
				}
			})
		})

		router.route(config().getString(ApplicationCfg.prefix.toString())+"/*").handler(sockJSHandler)

		server.requestHandler(router.&accept)

		server.listen(config().getInteger(ApplicationCfg.port.toString()))

		eb.consumer("server." + servername + ".send", { message ->
			if (message.body().size() > 0) {
				JsonObject msg = message.body()
				String sessionId = msg.getString(CommandAttribute.sessionid.toString())
				msg.remove(CommandAttribute.sessionid.toString())
				eb.send(sessionId, new BufferImpl(msg.toString()))
			}
		})

	}

}
