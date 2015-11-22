package org.jianyi.yibuyiqu

import groovy.json.JsonSlurper
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.shareddata.LocalMap

import org.jianyi.yibuyiqu.cache.Cache
import org.jianyi.yibuyiqu.cache.Cache.DataCache
import org.jianyi.yibuyiqu.utils.ConfigUtil
import org.jianyi.yibuyiqu.utils.JsonUril
import org.jianyi.yibuyiqu.utils.ConfigUtil.ApplicationCfg
import org.jianyi.yibuyiqu.utils.ConfigUtil.ShareMap

import com.hazelcast.core.IMap

def logger = LoggerFactory.getLogger("Applaction")

JsonObject  httpserverCfg, sockJsCfg, proxyCfg, serverCfg, commandCfg

hazelcastInstance = Cache.getHazelcastInstance()

def cfg = this.getClass().getResource(ConfigUtil.CONFIGFILE).text
def appCfg = new JsonSlurper().parseText(cfg)

// TODO Auto-generated method stub
vertx = Vertx.vertx()
IMap<String,Object> map = hazelcastInstance.getMap(DataCache.CONFIG.toString())
Map<String, Object> maps = JsonUril.jsonToMapObject(cfg)
maps.each { k, v ->
	map.put(k, v)
}

appCfg.server.each {
	def  vcfg = JsonUril.objectToJson(it)
	def config = vcfg.getMap()
	def options = [ 
		"config" : config,
		"worker" : vcfg.getBoolean(ApplicationCfg.worker.toString()),
		"instances" : vcfg.getInteger(ApplicationCfg.instances.toString())
	]
	
	vertx.deployVerticle(vcfg.getString(ApplicationCfg.verticlefile.toString()), options, { asyncResult ->
		if (asyncResult.succeeded()) {
			logger.info("The "+vcfg.getString(ApplicationCfg.verticlefile.toString())+" Server verticle has been deployed, deployment ID is "
					+ asyncResult.result());
		} else {
			asyncResult.cause().printStackTrace();
		}
	})
}


appCfg.proxyserver.each {
	def vcfg = JsonUril.objectToJson(it)
	def config = vcfg.getMap()
	def options = [
		"config" : config,
		"worker" : false,
		"instances" : 1
	]
	
	vertx.deployVerticle("org.jianyi.yibuyiqu.servers.socket.SockJSServer", options, { asyncResult ->
		if (asyncResult.succeeded()) {
			logger.info("The Socket verticle has been deployed, deployment ID is "
					+ asyncResult.result());
		} else {
			asyncResult.cause().printStackTrace();
		}
	})
}

appCfg.groups.each {
	def vcfg = JsonUril.objectToJson(it)
	def config = vcfg.getMap()
	def options = [
		"config" : config,
		"worker" : false,
		"instances" : vcfg.getInteger(ApplicationCfg.instances.toString())
	]
	
	vertx.deployVerticle(appCfg.groupserver, options, { asyncResult ->
		if (asyncResult.succeeded()) {
			logger.info("The "+appCfg.groupserver+":"+vcfg.getString(ApplicationCfg.name.toString())+" Group verticle has been deployed, deployment ID is "
					+ asyncResult.result());
		} else {
			asyncResult.cause().printStackTrace();
		}
	})
}

final LocalMap<String, String> commands = vertx.sharedData().getLocalMap(ShareMap.commandMap.toString())

appCfg.commands.each {
	def vcfg = JsonUril.objectToJson(it)
	def config = vcfg.getMap()
	def options = [
		"config" : config,
		"worker" : false,
		"instances" : vcfg.getInteger(ApplicationCfg.instances.toString())
	]
	
	vertx.deployVerticle(vcfg.getString(ApplicationCfg.verticlefile.toString()), options, { asyncResult ->
		if (asyncResult.succeeded()) {
			logger.info("The "+vcfg.getString(ApplicationCfg.verticlefile.toString())+" Command verticle has been deployed, deployment ID is "
					+ asyncResult.result());
				commands.put(vcfg.getString(ApplicationCfg.name.toString()), vcfg.getString(ApplicationCfg.address.toString()))
		} else {
			asyncResult.cause().printStackTrace();
		}
	})
	
}

def vcfg = JsonUril.objectToJson(appCfg.admin)
def config = vcfg.getMap()
def options = [
	"config" : config,
	"worker" : false,
	"instances" : 1
]

vertx.deployVerticle(vcfg.getString(ApplicationCfg.verticlefile.toString()), options, { asyncResult ->
	if (asyncResult.succeeded()) {
		logger.info("The "+vcfg.getString(ApplicationCfg.verticlefile.toString())+" verticle has been deployed, deployment ID is "
				+ asyncResult.result());
	} else {
		asyncResult.cause().printStackTrace();
	}
})

def eb = vertx.eventBus()
eb.consumer("server.deploy", { message -> 
	logger.info("deploy new service:"+message.body().name)
	config = message.body()
	def opt = [
		"config" : config,
		"worker" : false,
		"instances" : Integer.parseInt(message.body().instances)
	]
	
	vertx.deployVerticle(message.body().verticlefile, opt, { asyncResult ->
		if (asyncResult.succeeded()) {
			logger.info("The "+message.body().verticlefile+" verticle has been deployed, deployment ID is "
					+ asyncResult.result());
		} else {
			asyncResult.cause().printStackTrace();
		}
	})
	commands.put(message.body().name, message.body().address)
})

eb.consumer("server.changecommandrount", { message ->
	logger.info("change command:"+message.body().name+" rount:"+message.body().oldaddress+"  to "+message.body().newaddress)
	commands.put(message.body().name, message.body().newaddress)
})



