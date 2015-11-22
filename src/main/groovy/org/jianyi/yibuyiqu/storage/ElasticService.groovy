package org.jianyi.yibuyiqu.storage

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.jianyi.yibuyiqu.storage.elastic.ElasticClient

class ElasticService extends AbstractVerticle {
	
	private static final Logger log = LoggerFactory.getLogger(ElasticService.class)
	
	private Client client
	
	private ElasticClient elasticClient
	
	void start() {
		JsonObject config = config()
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", config.getString("clustername")).build();
		TransportClient transportClient = new TransportClient(settings);
		client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(config.getString("host"), 9300))
		elasticClient = ElasticClient.getInstance(client)
		elasticClient.init(config.getString("index"))
	}
	
	void stop() {
		client.close()
	}

}
