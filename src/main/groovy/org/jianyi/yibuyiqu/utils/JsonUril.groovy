package org.jianyi.yibuyiqu.utils

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import io.vertx.core.json.JsonObject

class JsonUril {

	def static objectToJson(Object obj) {
		ObjectWriter ow = new ObjectMapper().writer()
				.withDefaultPrettyPrinter()
		JsonObject json =  new JsonObject(ow.writeValueAsString(obj))
		return json
	}

	def static objectToJsonStr(Object obj) {
		ObjectWriter ow = new ObjectMapper().writer()
				.withDefaultPrettyPrinter()
		String jsonStr = ow.writeValueAsString(obj)
		return jsonStr
	}


	def static jsonToMapObject(String json) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonFactory factory = new JsonFactory()
		ObjectMapper mapper = new ObjectMapper(factory)
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {	}
		map = mapper.readValue(json, typeRef)
		return map
	}

	def static jsonToMapString(String json) {
		Map<String, String> map = new HashMap<String, String>()
		JsonFactory factory = new JsonFactory()
		ObjectMapper mapper = new ObjectMapper(factory)
		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {	}
		map = mapper.readValue(json, typeRef)
		return map
	}
}
