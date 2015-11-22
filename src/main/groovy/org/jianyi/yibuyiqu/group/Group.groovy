package org.jianyi.yibuyiqu.group

import io.vertx.core.json.JsonObject

import org.jianyi.yibuyiqu.utils.ConfigUtil.ApplicationCfg

import com.google.common.collect.Maps
import com.google.common.collect.Sets

class Group implements Serializable {
	
	public enum GroupCollections { sessions,users }
	
	String id,name,type
	Map<String, Set<String>> objects
	
	def Group(JsonObject groupCfg) {
		id = groupCfg.getString(ApplicationCfg.id.toString())
		name = groupCfg.getString(ApplicationCfg.name.toString())
		type = groupCfg.getString(ApplicationCfg.type.toString())
		objects = Maps.newConcurrentMap()
		objects.put(GroupCollections.sessions.toString(), Sets.newConcurrentHashSet())
		objects.put(GroupCollections.users.toString(), Sets.newConcurrentHashSet())
	}

}
