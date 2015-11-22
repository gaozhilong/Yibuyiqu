package org.jianyi.yibuyiqu.storage

import groovy.json.JsonSlurper
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory

import org.jianyi.yibuyiqu.storage.cassandra.CassandraClient
import org.jianyi.yibuyiqu.storage.cassandra.CassandraDao
import org.jianyi.yibuyiqu.storage.model.DBObject
import org.mindrot.jbcrypt.BCrypt

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Host
import com.datastax.driver.core.Metadata
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session

def logger = LoggerFactory.getLogger("YibuyiquStoreTest")

def dbcfg = this.getClass().getResource("db.json").text
JsonObject dbconfig = new JsonSlurper().parseText(dbcfg)

def dbsct = this.getClass().getResource("store.json").text
JsonObject dbstruct = new JsonSlurper().parseText(dbsct)


Cluster cluster = Cluster.builder().addContactPoint(dbconfig.getString("seedshost")).build()
Session session = cluster.connect()

Metadata metadata = cluster.getMetadata();

println("Connected to cluster: %s\n",
	  metadata.getClusterName())
for ( Host host : metadata.getAllHosts() ) {
   println("Datacenter: %s; Host: %s; Rack: %s\n",
	  host.getDatacenter(), host.getAddress(), host.getRack())
}

CassandraClient cassandraClient = CassandraClient.getInstance(session, dbconfig.getString("keyspacename"), dbstruct)
cassandraClient.init()

CassandraDao dao = new CassandraDao()

//StringUtils.getDate("")

//println(dao.getJsonStringById("users", "7b31ee90-7ed2-11e5-b402-ad35429551f9"))
JsonObject where = new JsonObject()
where.put("name", "user1")

List<DBObject> result = dao.getObjectsByParams("users", where)

result.each {
	println(it.getJsonObject().toString())
}

/*JsonObject datas = new JsonObject()
datas.put("name", "dao")
datas.put("mail", "dao@jianyi.org")
datas.put("password", BCrypt.hashpw("dao", BCrypt.gensalt()))
datas.put("logincount", 0)
datas.put("loginfaildcount", 0)


JsonObject map = new JsonObject()
map.put("string", "55")
map.put("int", 66)
datas.put("attributs", map)

DBObject dbo = new DBObject(dbstructure.getVertexs().get("users"),datas)
dao.insert(dbo)*/

/*JsonObject jsonObject = new JsonObject()
jsonObject.put("name", "users")
JsonObject datas = new JsonObject()
datas.put("name", "yi")
datas.put("mail", "yi@jianyi.org")
datas.put("password", BCrypt.hashpw("yi", BCrypt.gensalt()))
datas.put("logincount", 0)
datas.put("loginfaildcount", 0)


JsonObject map = new JsonObject()
map.put("string", "333")
map.put("int", 444)
datas.put("attributs", map)

jsonObject.put("data", datas)
ResultSet results = cassandraClient.insert(jsonObject)
println(results.toString())*/


/*JsonObject jsonObject = new JsonObject()
jsonObject.put("name", "users")
JsonObject datas = new JsonObject()
datas.put("id", "fc9a9b20-7ded-11e5-bccc-ad35429551f9")
datas.put("name", "jian")
datas.put("mail", "jian@jianyi.org")
datas.put("password", BCrypt.hashpw("jian1", BCrypt.gensalt()))
datas.put("logincount", 1)
datas.put("loginfaildcount", 1)


JsonObject map = new JsonObject()
map.put("string", "333")
map.put("int", 444)
datas.put("attributs", map)

jsonObject.put("data", datas)
ResultSet results = cassandraClient.update(jsonObject)*/


/*JsonObject jsonObject = new JsonObject()
jsonObject.put("name", "users")
JsonObject where = new JsonObject()
where.put("name", "jian")
//where.put("createtime", "2015-10-29 10:47:05")
jsonObject.put("where", where)
JsonArray resultObjects = cassandraClient.query(jsonObject)
println(resultObjects.getJsonObject(0).toString())*/
//println(BCrypt.checkpw("yi", resultObjects.getJsonObject("yi").getString("password")))


/*JsonObject jsonObject = new JsonObject()
jsonObject.put("name", "users")
JsonObject where = new JsonObject()
//where.put("name", "jian")
where.put("id", "56cc11c0-7de7-11e5-bbf5-ad35429551f9")

jsonObject.put("where", where)
ResultSet results = cassandraClient.delete(jsonObject)
println(results.toString())*/

cluster.close()

