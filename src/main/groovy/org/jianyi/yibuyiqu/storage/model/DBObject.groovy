package org.jianyi.yibuyiqu.storage.model

import io.vertx.core.json.JsonObject

import org.jianyi.yibuyiqu.storage.DataObjectAttribute
import org.jianyi.yibuyiqu.utils.JsonUril
import org.jianyi.yibuyiqu.utils.StringUtils

import com.google.common.base.Strings
import com.google.common.collect.Maps

class DBObject implements Serializable {

    private String table,id,name
	
	private Map<String, String> attributs
	
	private Map<String, String> strings
	
	private Map<String, Date> datetimes
	
	private Map<String, Integer> ints
	
	private Map<String, Map<String, String>> maps
	def DBObject() {
		attributs = Maps.newConcurrentMap()
		strings = Maps.newConcurrentMap()
		datetimes = Maps.newConcurrentMap()
		ints = Maps.newConcurrentMap()
		maps = Maps.newConcurrentMap()
	}
	
	def DBObject(CassandraTable tableModel, JsonObject datas) {
		table = tableModel.getName()
		attributs = Maps.newConcurrentMap()
		strings = Maps.newConcurrentMap()
		datetimes = Maps.newConcurrentMap()
		ints = Maps.newConcurrentMap()
		maps = Maps.newConcurrentMap()
		Map<String, String> attributs = tableModel.getAttrbuts()
		Set<String> attrs = attributs.keySet()
		for (String key : attrs) {
			String type = attributs.get(key)
			if (datas.containsKey(key)) {
				
					switch (type) {
						case "timeuuid":
							this.id = datas.getString(key)
							break
						case "text":
							if (!Strings.isNullOrEmpty(datas.getString(key))) {
								strings.put(key, datas.getString(key))
							}
							break
						case "timestamp":
							if (!Strings.isNullOrEmpty(datas.getString(key))) {
								datetimes.put(key, StringUtils.getDate(datas.getString(key)))
							}
							break
						case "int":
							if (datas.getInteger(key) != null) {
								ints.put(key, datas.getInteger(key).value)
							}
							break
						case "map<text,text>":
							maps.put(key, JsonUril.jsonToMapString(datas.getJsonObject(key).toString()))
							break
						default:
							if (!Strings.isNullOrEmpty(datas.getString(key))) {
								strings.put(key, datas.getString(key))
							}
					}
					switch (key) {
						case "id":
							this.id = datas.getString(key)
							break
						case "name":
							this.name = datas.getString(key)
							break
						case "type":
							this.type = datas.getString(key)
							break
						case "attributs":
							this.attributs = JsonUril.jsonToMapString(datas.getJsonObject(key).toString())
							break
						default:
							break
					}

			}
		}
	}
	
	def getInsertOrUpdateObject()  {
		JsonObject jsonObject = new JsonObject()
		jsonObject.put(DataObjectAttribute.name.toString(), table)
		JsonObject data = new JsonObject()
		data.put(DataObjectAttribute.id.toString(), this.id)
		data.put(DataObjectAttribute.name.toString(), this.name)
		data.put(DataObjectAttribute.attributs.toString(), new JsonObject(this.attributs))
		
		strings.each { k, v ->
			data.put(k, v)
		}
		
		datetimes.each { k, v ->
			data.put(k, v)
		}
		
		ints.each { k, v ->
			data.put(k, v)
		}
		
		maps.each { k, v ->
			data.put(k, new JsonObject(v))
		}
		
		jsonObject.put(DataObjectAttribute.data.toString(), data)
		return jsonObject
	}
	
	def getJsonObject()  {
		JsonObject data = new JsonObject()
		data.put(DataObjectAttribute.type.toString(), table)
		data.put(DataObjectAttribute.id.toString(), this.id)
		data.put(DataObjectAttribute.name.toString(), this.name)
		data.put(DataObjectAttribute.attributs.toString(), new JsonObject(this.attributs))
		
		strings.each { k, v ->
			data.put(k, v)
		}
		
		datetimes.each { k, v ->
			data.put(k, StringUtils.getDateString(v))
		}
		
		ints.each { k, v ->
			data.put(k, v)
		}
		
		maps.each { k, v ->
			data.put(k, new JsonObject(v))
		}
		
		return data
	}
	
	def getString(String key) {
		return strings.get(key)
	}
	
	def setString(String key, String value) {
		strings.put(key, value)
	}
	
	def getInt(String key) {
		return ints.get(key)
	}
	
	def setInts(String key, int value) {
		ints.put(key, value)
	}
	
	def getAttribut(String key) {
		return attributs.get(key)
	}
	
	def setAttribut(String key, String value) {
		attributs.put(key, value)
	}
	
	def getMapAttribut(String map, String key) {
		return maps.get(map).get(key)
	}
	
	def setMapAttribut(String map, String key, String value) {
		maps.get(map).put(key, value)
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getAttributs() {
		return attributs;
	}

	public void setAttributs(Map<String, String> attributs) {
		this.attributs = attributs;
	}

	public Map<String, String> getStrings() {
		return strings;
	}

	public void setStrings(Map<String, String> strings) {
		this.strings = strings;
	}

	public Map<String, Integer> getInts() {
		return ints;
	}

	public void setInts(Map<String, Integer> ints) {
		this.ints = ints;
	}

	public Map<String, Map<String, String>> getMaps() {
		return maps;
	}

	public void setMaps(Map<String, Map<String, String>> maps) {
		this.maps = maps;
	}
	
}
