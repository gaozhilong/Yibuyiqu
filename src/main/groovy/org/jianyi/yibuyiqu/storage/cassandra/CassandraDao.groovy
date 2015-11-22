package org.jianyi.yibuyiqu.storage.cassandra

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

import org.jianyi.yibuyiqu.storage.DataObjectAttribute
import org.jianyi.yibuyiqu.storage.model.DBObject

import com.datastax.driver.core.ResultSet
import com.google.common.collect.Lists

class CassandraDao {
	
	private CassandraClient cassandraClient
	
	def CassandraDao() {
		cassandraClient = CassandraClient.getInstance()
	}
	
	def getJsonStringById(String table,String id) {
		DBObject object = getObjectById(table, id)
		return object.getJsonObject().toString()
	}
	
	def getObjectById(String table,String id) {
		JsonObject jsonObject = new JsonObject()
		jsonObject.put(DataObjectAttribute.name.toString(), table)
		JsonObject where = new JsonObject()
		where.put(DataObjectAttribute.id.toString(), id)
		jsonObject.put(DataObjectAttribute.where.toString(), where)
		JsonArray resultObjects = cassandraClient.query(jsonObject)
		DBObject object = null;
		if (!resultObjects.empty) {
			object = new DBObject(cassandraClient.getDbstructure().getTableModels().get(table) ,resultObjects.getJsonObject(0))
		}
		return object
	}
	
	def getJsonStringByParams(String table, JsonObject where) {
		JsonObject jsonObject = new JsonObject()
		jsonObject.put(DataObjectAttribute.name.toString(), table)
		jsonObject.put(DataObjectAttribute.where.toString(), where)
		JsonArray resultObjects = cassandraClient.query(jsonObject)
		JsonArray result = null
		if (!resultObjects.empty) {
			result = Lists.newArrayList()
			resultObjects.each {
				result.add((new DBObject(cassandraClient.getDbstructure().getTableModels().get(table) ,it)).getJsonObject())
			}
		}
		
		return result.toString()
	}
	
	def getObjectsByParams(String table, JsonObject where) {
		JsonObject jsonObject = new JsonObject()
		jsonObject.put(DataObjectAttribute.name.toString(), table)
		jsonObject.put(DataObjectAttribute.where.toString(), where)
		JsonArray resultObjects = cassandraClient.query(jsonObject)
		List<DBObject> result = null
		if (!resultObjects.empty) {
			result = Lists.newArrayList()
			resultObjects.each {
				result.add(new DBObject(cassandraClient.getDbstructure().getTableModels().get(table) ,it))
			}
		}
		
		return result
	}
	
	def insert(DBObject object) {
		ResultSet results = cassandraClient.insert(object.getInsertOrUpdateObject())
		return results
	}
	
	def delete(String table, JsonObject where) {
		JsonObject jsonObject = new JsonObject()
		jsonObject.put(DataObjectAttribute.name.toString(), table)
		jsonObject.put(DataObjectAttribute.where.toString(), where)
		ResultSet results = cassandraClient.delete(jsonObject)
		return results
	}
	
	def update(DBObject object) {
		ResultSet results = cassandraClient.update(object.getInsertOrUpdateObject())
		return results
	}

}
