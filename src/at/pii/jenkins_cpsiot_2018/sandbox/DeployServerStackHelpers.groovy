package at.pii.jenkins_cpsiot_2018.sandbox;

import groovy.json.*;
import org.boon.Boon;

//import groovy.json.JsonSlurperClassic

class DeployServerStackHelpers
{

	def log;
	
	def input;
	
	def Constants;
	
	def utils;
	
	def environment
	
	def PortusApi
	
	def JenkinsApi
	
	def init( inputParameter, Constants, environment, PortusApi, JenkinsApi  )
	{
		input = new JsonSlurperClassic().parseText(inputParameter)
		
		this.Constants = Constants
		
		this.environment = environment
		
		this.PortusApi = PortusApi
		
		this.JenkinsApi = JenkinsApi
		
		log = new Log()
		log.init(Constants)
		
		utils = new Utils()
		
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "DeployServerStackHelpers init" )
		
		checkInput()
	}
	
	def checkInput()
	{
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "Input check ran" )
		
		if( input.Node.noNode && input.Node.noNode == true )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "No Node to Deploy the Stack is Online" )
	}
	
	def getParameter()
	{
		return input
	}
	
	def getImages()
	{
		return input.Images
	}
	
	def isDB(image )
	{
		if( image.database == true )
			return true
		else
			return false
	}
	
	def generateDBScript(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW )
	{
		
		
		if( image.initDBScript == true )
		{
			def script = []
			
			def DB_ARROWHEAD = DeployServerStackHelpers.getArrowheadDB()
			def DB_ARROWHEAD_LOG = DeployServerStackHelpers.getArrowheadDBLog()
			
			script.add("CREATE DATABASE IF NOT EXISTS ${DB_ARROWHEAD_LOG};")
			script.add("CREATE DATABASE IF NOT EXISTS ${DB_ARROWHEAD};")
			script.add("/* test comment */")
			script.add("CREATE USER '${DEFAULT_DB_ARROWHEAD_USR}'@'localhost' IDENTIFIED BY '${DEFAULT_DB_ARROWHEAD_PSW}';")
			script.add("CREATE USER '${DEFAULT_DB_ARROWHEAD_USR}'@'%' IDENTIFIED BY '${DEFAULT_DB_ARROWHEAD_PSW}';")
	
			script.add("USE ${DB_ARROWHEAD_LOG};")
			script.add("CREATE TABLE logs ( id int(10) unsigned NOT NULL AUTO_INCREMENT, date datetime NOT NULL, origin varchar(255) COLLATE utf8_unicode_ci NOT NULL, level varchar(10) COLLATE utf8_unicode_ci NOT NULL, message varchar(1000) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (id) ) ENGINE=InnoDB AUTO_INCREMENT=1557 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;")
	
			script.add("GRANT ALL PRIVILEGES ON ${DB_ARROWHEAD_LOG}.* TO '${DEFAULT_DB_ARROWHEAD_USR}'@'%';")
			script.add("GRANT ALL PRIVILEGES ON ${DB_ARROWHEAD_LOG}.* TO '${DEFAULT_DB_ARROWHEAD_USR}'@'localhost';")
	
			script.add("GRANT ALL PRIVILEGES ON ${DB_ARROWHEAD}.* TO '${DEFAULT_DB_ARROWHEAD_USR}'@'%';")
			script.add("GRANT ALL PRIVILEGES ON ${DB_ARROWHEAD}.* TO '${DEFAULT_DB_ARROWHEAD_USR}'@'localhost';")
			
			return script.join("\n")
		}
		
		if( image.initDBScript.initDBScriptInput )
			return image.initDBScript.initDBScriptInput
	}
	
	def getArrowheadDB()
	{
		return input.ArrowHead.DB.arrowHeadDB
	}
	
	def getArrowheadDBLog()
	{
		return input.ArrowHead.DB.arrowHeadLogDB
	}
}
