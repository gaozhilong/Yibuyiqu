package org.jianyi.yibuyiqu.utils

import java.text.ParseException
import java.text.SimpleDateFormat

class StringUtils {

	def static getDate(String date) {
		Date dt = null
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		try {
			dt = format.parse(date)
		} catch (ParseException e) {
			e.printStackTrace()
		}
		return dt
	}

	def static getDateString(Date date) {
		String dateStr = null
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		try {
			dateStr = format.format(date)
		} catch (ParseException e) {
			e.printStackTrace()
		}
		return dateStr
	}
}
