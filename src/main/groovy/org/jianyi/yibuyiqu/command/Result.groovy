package org.jianyi.yibuyiqu.command

import io.vertx.core.json.JsonObject

import org.jianyi.yibuyiqu.servers.command.CommandService.CommandAttribute

class Result {

	public enum ResultType{ SUCCESS, ERROR }

	private String sessionID
	private String message
	private String result
	private String scoap
	private boolean immediately

	Result(sessionID, message, result) {
		this.sessionID = sessionID
		this.message = message
		this.result = result
		immediately = true
	}

	Result(sessionID, scoap, message, result) {
		this.sessionID = sessionID
		this.scoap = scoap
		this.message = message
		this.result = result
	}

	Result(sessionID, message, result, scoap, immediately) {
		this.sessionID = sessionID
		this.message = message
		this.result = result
		this.scoap = scoap
		this.immediately = immediately
	}

	def toJsonObject() {
		JsonObject jsonObject = new JsonObject()
		jsonObject.put(CommandAttribute.sessionid.toString(), sessionID)
		jsonObject.put(CommandAttribute.result.toString(),result)
		jsonObject.put(CommandAttribute.message.toString(),message)
		return jsonObject
	}
	
	public String getSessionID() {
		return sessionID
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID
	}

	public String getMessage() {
		return message
	}

	public void setMessage(String message) {
		this.message = message
	}

	public String getResult() {
		return result
	}

	public void setResult(String result) {
		this.result = result
	}

	public String getScoap() {
		return scoap
	}

	public void setScoap(String scoap) {
		this.scoap = scoap
	}

	public boolean isImmediately() {
		return immediately
	}

	public void setImmediately(boolean immediately) {
		this.immediately = immediately
	}
	
}
