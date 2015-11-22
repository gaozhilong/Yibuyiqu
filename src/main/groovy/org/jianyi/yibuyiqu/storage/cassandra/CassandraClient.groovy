package org.jianyi.yibuyiqu.storage.cassandra

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.jianyi.yibuyiqu.storage.DataObjectAttribute
import org.jianyi.yibuyiqu.storage.dbstructure.DBstructure
import org.jianyi.yibuyiqu.storage.model.CassandraTable
import org.jianyi.yibuyiqu.utils.JsonUril
import org.jianyi.yibuyiqu.utils.StringUtils
import org.mindrot.jbcrypt.BCrypt

import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.Delete
import com.datastax.driver.core.querybuilder.Insert
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.Select
import com.datastax.driver.core.querybuilder.Update
import com.datastax.driver.core.utils.UUIDs


class CassandraClient {

	private Session session

	private String keyspace

	private DBstructure dbstructure

	private JsonObject dbstruct

	private static CassandraClient cassandraClient = null

	private CassandraClient(Session session, String keyspace, JsonObject dbstruct) {
		super()
		// TODO Auto-generated constructor stub
		this.session = session
		this.keyspace = keyspace
		this.dbstruct = dbstruct
		this.dbstructure = new DBstructure(dbstruct)
	}

	void init() {
		session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace +
				" with replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}")
		initTables(dbstructure)
		initData(dbstruct)
	}

	private void initTables(DBstructure dbstructure) {
		boolean forcecreate = dbstruct.getBoolean(DataObjectAttribute.forcecreate.toString())
		Map<String, CassandraTable> tableModels = dbstructure.getTableModels()
		Set<String> vkeys = tableModels.keySet()
		for (String vkey : vkeys) {
			CassandraTable tableModel = tableModels.get(vkey)
			if (forcecreate) {
				StringBuffer cqldrop = new StringBuffer("DROP TABLE IF EXISTS ").append(keyspace).append(".").append(tableModel.getName())
				session.execute(cqldrop.toString())
			}

			StringBuffer cql = new StringBuffer("CREATE TABLE IF NOT EXISTS ")
					.append(keyspace).append(".").append(tableModel.getName()).append(" (")
			Map<String, String> attributs = tableModel.getAttrbuts()
			List<String> keys = tableModel.getPkeys()
			Set<String> attrs = attributs.keySet()
			for (String key : attrs) {
				cql.append(key)
				cql.append(" ")
				cql.append(attributs.get(key))
				cql.append(",")
			}
			cql.append("PRIMARY KEY (")
			for (String pk : keys) {
				cql.append(pk)
				cql.append(",")
			}
			String cqls = cql.substring(0,cql.length()-1)

			cqls = cqls + "))"
			session.execute(cqls)

			cql = new StringBuffer("CREATE INDEX IF NOT EXISTS ON ").append(keyspace).append(".").append(tableModel.getName())
			List<String> indexs = tableModel.getIndexs()
			for (String index : indexs) {
				cql.append("(").append(index).append(")")
				session.execute(cql.toString())
				cql = new StringBuffer("CREATE INDEX IF NOT EXISTS ON ").append(keyspace).append(".").append(tableModel.getName())
			}
		}
	}

	private def initData(JsonObject dbstruct) {
		JsonObject datas = dbstruct.getJsonObject(DataObjectAttribute.data.toString())
		datas.getMap().each { k, v ->
			JsonObject jsonObject = new JsonObject()
			jsonObject.put(DataObjectAttribute.name.toString(), k)
			JsonObject where = new JsonObject()
			for (Map datamap : v) {
				where.put(DataObjectAttribute.name.toString(), datamap.get(DataObjectAttribute.name.toString()))
				jsonObject.put(DataObjectAttribute.where.toString(), where)
				JsonArray resultObjects = query(jsonObject)
				if (resultObjects.size() <= 0) {
					JsonObject data = new JsonObject()
					datamap.each{ attr, value ->
						if (attr == DataObjectAttribute.password.toString()) {
							data.put(attr, BCrypt.hashpw(value, BCrypt.gensalt()))
						} else {
							data.put(attr, value)
						}
					}
					jsonObject.put(DataObjectAttribute.data.toString(), data)
					insert(jsonObject)
				}
			}
		}
	}

	def insert(JsonObject jsonObject) {
		String tname = jsonObject.getString(DataObjectAttribute.name.toString())
		JsonObject datas = jsonObject.getJsonObject(DataObjectAttribute.data.toString())
		CassandraTable tableModel = dbstructure.getTableModels().get(tname)
		Insert insert = QueryBuilder.insertInto(keyspace, tableModel.getName())
		datas.remove(DataObjectAttribute.id.toString())
		insert.value(DataObjectAttribute.id.toString(), UUIDs.timeBased())

		Map<String, String> attributs = tableModel.getAttrbuts()
		Set<String> attrs = attributs.keySet()
		for (String key : attrs) {
			String type = attributs.get(key)
			if (datas.containsKey(key)) {
				switch (type) {
					case "timeuuid":
						insert.value(key, datas.getString(key))
						break
					case "text":
						insert.value(key, datas.getString(key))
						break
					case "timestamp":
						insert.value(key, new Date())
						break
					case "int":
						insert.value(key, datas.getInteger(key).value)
						break
					case "map<text,text>":
						insert.value(key, JsonUril.jsonToMapString(datas.getJsonObject(key).toString()))
						break
					default:
						insert.value(key, datas.getString(key))
				}
			}
		}
		//insert.value(DataObjectAttribute.id.toString(), UUIDs.timeBased())
		insert.value(DataObjectAttribute.createtime.toString(), new Date())
		ResultSet results = session.execute(insert)
	}

	def delete(JsonObject jsonObject) {
		String tname = jsonObject.getString(DataObjectAttribute.name.toString())
		JsonObject wheres = jsonObject.getJsonObject(DataObjectAttribute.where.toString())
		CassandraTable tableModel = dbstructure.getTableModels().get(tname)
		Delete delete = QueryBuilder.delete().from(keyspace, tableModel.getName())
		Map<String, Object> whereParas = wheres.getMap()
		Set<String> whereKeys = whereParas.keySet()
		Map<String, String> attributs = tableModel.getAttrbuts()
		for (String key : whereKeys) {
			String type = attributs.get(key)
			switch (type) {
				case "timeuuid":
					delete.where(QueryBuilder.eq(key, UUID.fromString(whereParas.get(key).toString())))
					break
				case "text":
					delete.where(QueryBuilder.eq(key, whereParas.get(key).toString()))
					break
				case "timestamp":
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date date = format.parse(whereParas.get(key).toString());
					delete.where(QueryBuilder.eq(key, date))
					break
				case "int":
					delete.where(QueryBuilder.eq(key, Integer.parseInt(whereParas.get(key).toString())))
					break
				default:
					delete.where(QueryBuilder.eq(key, whereParas.get(key).toString()))
			}
		}
		ResultSet results = session.execute(delete)
	}

	def query(JsonObject jsonObject) {
		String tname = jsonObject.getString(DataObjectAttribute.name.toString())
		JsonObject wheres = jsonObject.getJsonObject(DataObjectAttribute.where.toString())
		CassandraTable tableModel = dbstructure.getTableModels().get(tname)
		Select select = QueryBuilder.select().from(keyspace, tableModel.getName())
		Map<String, Object> whereParas = wheres.getMap()
		Set<String> whereKeys = whereParas.keySet()
		Map<String, String> attributs = tableModel.getAttrbuts()
		//定义了的查询属性
		List<String> keys = tableModel.getKeys()

		for (String key : whereKeys) {
			if (keys.contains(key)) {
				String type = attributs.get(key)
				switch (type) {
					case "timeuuid":
						select.where(QueryBuilder.eq(key, UUID.fromString(whereParas.get(key).toString())))
						break
					case "text":
						select.where(QueryBuilder.eq(key, whereParas.get(key).toString()))
						break
					case "timestamp":
						select.where(QueryBuilder.eq(key, StringUtils.getDate(whereParas.get(key).toString())))
						break
					case "int":
						select.where(QueryBuilder.eq(key, Integer.parseInt(whereParas.get(key).toString())))
						break
					default:
						select.where(QueryBuilder.eq(key, whereParas.get(key).toString()))
				}
			}
		}
		select.allowFiltering()
		JsonArray resultObjects = new JsonArray()
		ResultSet results = session.execute(select)
		int rows = 0
		for(Row input : results) {
			JsonObject resultObject = new JsonObject()
			Set<String> attrs = attributs.keySet()
			for (String key : attrs) {
				String type = attributs.get(key)
				switch (type) {
					case "timeuuid":
						resultObject.put(key, input.getUUID(key).toString())
						break
					case "text":
						resultObject.put(key, input.getString(key))
						break
					case "timestamp":
						if (input.getDate(key) != null) {
							resultObject.put(key, StringUtils.getDateString(input.getDate(key)))
						}
						break
					case "int":
						resultObject.put(key, input.getInt(key))
						break
					case "map<text,text>":
						resultObject.put(key, JsonUril.objectToJson(input.getMap(key, String.class, String.class)))
						break
					default:
						resultObject.put(key, input.getString(key))
				}
			}
			resultObjects.add(resultObject)
			rows++
		}
		return resultObjects
	}
	def update(JsonObject jsonObject) {
		String tname = jsonObject.getString(DataObjectAttribute.name.toString())
		JsonObject datas = jsonObject.getJsonObject(DataObjectAttribute.data.toString())
		CassandraTable tableModel = dbstructure.getTableModels().get(tname)
		Update update = QueryBuilder.update(keyspace, tableModel.getName())

		Map<String, String> attributs = tableModel.getAttrbuts()
		Set<String> attrs = attributs.keySet()
		for (String key : attrs) {
			String type = attributs.get(key)
			if (datas.containsKey(key)) {
				switch (type) {
					case "timeuuid":
						update.with(QueryBuilder.set(key, UUID.fromString(datas.getString(key))))
						break
					case "text":
						update.with(QueryBuilder.set(key, datas.getString(key)))
						break
					case "timestamp":
						update.with(QueryBuilder.set(key, new Date()))
						break
					case "int":
						update.with(QueryBuilder.set(key, datas.getInteger(key).value))
						break
					case "map<text,text>":
						update.with(QueryBuilder.set(key, JsonUril.jsonToMapString(datas.getJsonObject(key).toString())))
						break
					default:
						update.with(QueryBuilder.set(key, datas.getString(key)))
				}
			}
		}
		update.with(QueryBuilder.set(DataObjectAttribute.updatetime.toString(),  new Date()))
		//定义了的查询属性
		List<String> keys = tableModel.getKeys()
		keys.each {
			update.where(QueryBuilder.eq(it.toString(), datas.getString(it)))
		}

		ResultSet results = session.execute(update)
	}

	public static getInstance(Session session, String keyspace, JsonObject dbstruct) {
		if (cassandraClient == null) {
			cassandraClient = new CassandraClient(session, keyspace, dbstruct)
		}
		return cassandraClient
	}

	public static getInstance() {
		return cassandraClient
	}

	public DBstructure getDbstructure() {
		return dbstructure
	}

}
