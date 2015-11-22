package org.jianyi.yibuyiqu.storage

import groovy.json.JsonSlurper
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import org.jianyi.yibuyiqu.storage.cassandra.CassandraClient

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session

class CassandraService extends AbstractVerticle {
	
	private static final Logger log = LoggerFactory.getLogger(CassandraService.class)
	
	private Cluster cluster
	private Session session
	
	void start() {
		JsonObject dbconfig = config()
		
		def dbsct = this.getClass().getResource("storage.json").text
		JsonObject dbstruct = new JsonSlurper().parseText(dbsct)
		
		cluster = Cluster.builder().addContactPoint(dbconfig.getString("contactpoint")).build()
		session = cluster.connect()
		CassandraClient cassandraClient = CassandraClient.getInstance(session, dbconfig.getString("keyspacename"), dbstruct)
		cassandraClient.init()
		
		def eb = vertx.eventBus()
		
		eb.consumer("cassandra.init", { message ->
			cassandraClient.init()
		})
		/*eb.consumer("cassandra.insert", Sync.awaitResult({ message ->
			if (message.body().size() > 0) {
				JsonObject resultObjects = cassandraClient.insert(message.body())
				message.reply(resultObjects)
			}
		}))
		
		eb.consumer("cassandra.delete", Sync.awaitResult({ message ->
			if (message.body().size() > 0) {
				JsonObject resultObjects = cassandraClient.delete(message.body())
				message.reply(resultObjects)
			}
		}))
		
		eb.consumer("cassandra.query", Sync.awaitResult({ message ->
			if (message.body().size() > 0) {
				JsonObject resultObjects = cassandraClient.query(message.body())
				message.reply(resultObjects)
			}
		}))
		
		eb.consumer("cassandra.update", Sync.awaitResult({ message ->
			if (message.body().size() > 0) {
				JsonObject resultObjects = cassandraClient.update(message.body())
				message.reply(resultObjects)
			}
		}))*/
		
	}
	
	void stop() {
		cluster.close()
	}

}
