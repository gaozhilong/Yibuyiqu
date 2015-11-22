package org.jianyi.yibuyiqu.storage.elastic

import io.vertx.core.json.JsonObject
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.Client
import org.elasticsearch.common.xcontent.XContentBuilder

class ElasticClient {
	
	private static ElasticClient elasticClient = null
	
	private Client client
	
	private String index
	
	private ElasticClient(Client client) {
		this.client = client
	}
	
	def init(String index) {
		this.index = index
		if (!client.admin().indices().prepareExists(index)) {
			CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(index);
			createIndexRequestBuilder.execute().actionGet();
		}
	}
	
	def insertDocument(String type, JsonObject data) {
		IndexRequestBuilder indexRequestBuilder = client.prepareIndex(index, type, UUID.randomUUID().toString())
		indexRequestBuilder.setSource(data.toString())
		IndexResponse response = indexRequestBuilder .execute().actionGet();
	}
	
	static getInstance(Client client) {
		if (elasticClient == null) {
			elasticClient = new ElasticClient(client)
		}
		return elasticClient
	}
	
	static getInstance() {
		return elasticClient
	}

}
