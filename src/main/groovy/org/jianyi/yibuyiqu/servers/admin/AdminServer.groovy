package org.jianyi.yibuyiqu.servers.admin

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

import org.jianyi.yibuyiqu.utils.ConfigUtil.ApplicationCfg

class AdminServer extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(AdminServer.class)

	void start() {
		// TODO Auto-generated method stub

		JsonObject cfg = config()
		String servername = cfg.getString(ApplicationCfg.name.toString())

		def server = vertx.createHttpServer().requestHandler({ req ->
			if (req.uri() == "/") {
				// Serve the index page
				req.response().sendFile(this.getClass().getResource('index.html').getPath())
			} else if (req.uri().startsWith("/form")) {
				req.response().setChunked(true)
				req.setExpectMultipart(true)
				req.endHandler({ v ->
					JsonObject cmdcfg = new JsonObject()
					req.formAttributes().names().each { attr ->
						cmdcfg.put(attr,req.formAttributes().get(attr))
					}
					def eb = vertx.eventBus()
					eb.send("server.deploy", cmdcfg)
					req.response().write("部署成功\n")
					req.response().end()
				})
			} else {
				req.response().setStatusCode(404).end()
			}
		})

		server.listen(config().getInteger(ApplicationCfg.port.toString()))
	}
}
