package org.jianyi.yibuyiqu.servers.log

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

import org.jianyi.yibuyiqu.storage.elastic.ElasticClient
import org.jianyi.yibuyiqu.utils.ConfigUtil.Session

class LogServer extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(LogServer.class)
	
	

	public void start(Future<Void> startFuture) {
		// TODO Auto-generated method stub
		
		def eb = vertx.eventBus()
		eb.consumer("server.log", { message -> saveLog(message) })
		eb.consumer("server.log.runtime", { message -> commandRunTimeLog(message) })
	}

	def saveLog(message) {
		def log = message.body()
		log.put(Session.createtime.toString(), new Date().format("yyyy-MM-dd'T'HH:mm:ss SSS"))
		ElasticClient elasticClient = ElasticClient.getInstance()
		elasticClient.insertDocument("logs",log)
	}

	def commandRunTimeLog(message) {
		def log = message.body()
		log.put(Session.createtime.toString(), new Date().format("yyyy-MM-dd'T'HH:mm:ss SSS"))
		ElasticClient elasticClient = ElasticClient.getInstance()
		elasticClient.insertDocument("logs",log)
	}

}
