package org.jianyi.yibuyiqu.storage.dbstructure

import io.vertx.core.json.JsonObject

import org.jianyi.yibuyiqu.storage.DataObjectAttribute
import org.jianyi.yibuyiqu.storage.model.CassandraTable

import com.google.common.base.Splitter
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets

class DBstructure {
	
	private final JsonObject dbstruct
	
	private Map<String, Object> common
	
	private List<Map<String, Object>> tables
	
	private Map<String, CassandraTable> tableModels
	
	def DBstructure(JsonObject dbstruct) {
		this.dbstruct = dbstruct.getJsonObject(DataObjectAttribute.table.toString())
		common = Maps.newConcurrentMap()
		tables = Lists.newArrayList()
		tableModels = Maps.newConcurrentMap()
		init()
	}
	
	private def init() {
		JsonObject commoncfg = dbstruct.getJsonObject("common")
		common = commoncfg.getMap()
		Map dataMap = dbstruct.getMap()
		Set<String> keys = dataMap.keySet()
		keys.remove("common")
		for (String key : keys) {
			JsonObject table = dbstruct.getJsonObject(key)
			tables.add(table.getMap())
			CassandraTable tableModel = getTableModel(table)
			tableModel.setName(key)
			tableModels.put(key, tableModel)
		}
	}
	
	private def CassandraTable getTableModel(JsonObject table) {
		
		Map<String, String> attributs = Maps.newConcurrentMap()
		
		Set<String> commonKeys = Sets.newHashSet(common.keySet())
		
		String pkss = common.get("primary_key").toString()
		//创建表时使用
		List<String> pkey = Lists.newArrayList()
		pkey.add(pkss)
		if (!Strings.isNullOrEmpty(table.getString("primary_key"))) {
			Iterable<String> pks = Splitter.on(",").trimResults().omitEmptyStrings().split(table.getString("primary_key"))
			pks.each {
				pkey.add(it)
			}
		}
		
		pkss = pkss-"("-")"
		if (!Strings.isNullOrEmpty(table.getString("primary_key"))) {
			pkss = pkss + "," + table.getString("primary_key")
		}
		Iterable<String> pks = Splitter.on(",").trimResults().omitEmptyStrings().split(pkss)

		List<String> pkeys = pks.asList()
		
		commonKeys.remove("primary_key")
		
		
		String indexstr = common.get("index").toString()
		
		if (!Strings.isNullOrEmpty(table.getString("index"))) {
			indexstr = indexstr + "," + table.getString("index")
		}
		Iterable<String> indexits = Splitter.on(",").trimResults().omitEmptyStrings().split(indexstr)

		List<String> indexs = indexits.asList()
		
		commonKeys.remove("index")
		
		for (String key : commonKeys) {
			attributs.put(key,common.get(key).toString())
		}
		
		Map attrMap = table.getMap()
		Set<String> keys = attrMap.keySet()
		keys.remove("primary_key")
		keys.remove("index")
		for (String key : keys) {
			attributs.put(key,table.getString(key))
		}
		CassandraTable tableModel = new CassandraTable(attributs,pkey, pkeys, indexs)
		
		return tableModel
	}
	
	def getTableModels() {
		return tableModels
	}
}
