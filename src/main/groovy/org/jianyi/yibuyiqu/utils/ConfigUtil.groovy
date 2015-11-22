package org.jianyi.yibuyiqu.utils

class ConfigUtil {
	
	static final String CONFIGFILE="config.json"
	
	public enum ApplicationCfg {
		id,config,name,type,port,prefix,verticlefile,instances,worker,address,author,error,result
	}
	
	
	public enum Session {
		sessionVal,createtime,duration,value,loaduseraddress,unloaduseraddress,password
	}
	
	
	public enum Database {
		postgres,host,db,username,password,max,dbname
	}
	
	public enum Mongodb {
		mongodb,log,runtimelog,autoincrement,backupcount,sessiontimeout
	}
	
	public enum ShareMap {
		user,alluser,commandMap
	}
	
}
