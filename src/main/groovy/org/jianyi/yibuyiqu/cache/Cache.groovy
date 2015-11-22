package org.jianyi.yibuyiqu.cache

import com.hazelcast.config.Config
import com.hazelcast.config.GroupConfig
import com.hazelcast.config.MapConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.instance.HazelcastInstanceFactory

class Cache {
	
	private static HazelcastInstance hazelcastInstance = null
	
	public enum DataCache {
		SESSIONS, CONFIG, GROUPS
	}
	
	private Cache() {
		Config config = new Config()
		config.setInstanceName("YibuyiquServer")
		config.getNetworkConfig().setPort(5701)
		config.getNetworkConfig().setPortAutoIncrement(true)
		
		GroupConfig groupConfig = new GroupConfig()
		groupConfig.setName("serverCluster")
		config.setGroupConfig(groupConfig)
		
		MapConfig mapConfig = new MapConfig()
		mapConfig.setBackupCount(1)
		config.getMapConfigs().put(DataCache.CONFIG.toString(), mapConfig)
		config.getMapConfigs().put(DataCache.GROUPS.toString(), mapConfig)
		
		MapConfig sessionConfig = new MapConfig()
		sessionConfig.setBackupCount(1)
		sessionConfig.setTimeToLiveSeconds(3600)
		config.getMapConfigs().put(DataCache.SESSIONS.toString(), sessionConfig)
		
		hazelcastInstance = HazelcastInstanceFactory.newHazelcastInstance(config)
	}
	
	void close() {
		Hazelcast.shutdownAll()
	}
	
	static HazelcastInstance getHazelcastInstance() {
		if (hazelcastInstance == null) {
			new Cache();
		}
		return hazelcastInstance;
	}

}
