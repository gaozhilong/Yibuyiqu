package org.jianyi.yibuyiqu.storage

import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress


// on startup
Settings settings = Settings.settingsBuilder().put("cluster.name", "localtestsearch").build();
Client client = TransportClient.builder().settings(settings).build()
		.addTransportAddress(new InetSocketTransportAddress("localhost", 9300))

client.admin().indices().getIndex(null)

// on shutdown

client.close()
