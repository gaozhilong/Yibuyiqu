package org.jianyi.yibuyiqu.storage.model

import java.io.Serializable;

class CassandraTable implements Serializable {
	
	private String name
	
	private final Map<String, String> attributs
	
	private final List<String> pkeys
	
	private final List<String> keys
	
	private final List<String> indexs
	public CassandraTable(Map<String, String> attributs, List<String> pkeys, List<String> keys, List<String> indexs) {
		this.attributs = attributs
		this.pkeys = pkeys
		this.keys = keys
		this.indexs = indexs
	}
	
	def setName(name) {
		this.name = name
	}
	
	def getName() {
		return name
	}
	
	def getAttrbuts() {
		return attributs
	}
	
	def getKeys() {
		return keys
	}

	def getPkeys() {
		return pkeys
	}

	def getIndexs() {
		return indexs;
	}
	
}
