package at.pii.jenkins_cpsiot_2018.sandbox;

import groovy.json.*;
import org.boon.Boon;

//import groovy.json.JsonSlurperClassic

class buildArrowHeadServerStackHelpers
{

	def log;
	
	def input;
	
	def Constants;
	
	def utils;
	
	def environment
	
	def PortusApi
	
	def stdCloudTeam
	
	def JenkinsApi
	
	def init( inputParameter, Constants, environment, PortusApi, JenkinsApi  )
	{
		input = new JsonSlurperClassic().parseText(inputParameter)
		
		this.Constants = Constants
		
		this.environment = environment
		
		this.PortusApi = PortusApi
		
		this.JenkinsApi = JenkinsApi
		
		stdCloudTeam = environment.PORTUS_STD_CLOUD_TEAM
		
		log = new Log()
		log.init(Constants)
		
		utils = new Utils()
		
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "buildArrowHeadServerStackHelpers init" )
		
		checkInput()
	}
	
	def checkInput()
	{
		if( ! input.NameSpace.cloud )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Cloud name not found" )
		
		log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Input Check ran" )
	}
	
	def getParameter()
	{
		return input
	}
	
	def checkCompileDockerHub()
	{
		return input.Compile.image.fromDockerHub
	}
	
	def getDockerCompileImage()
	{
		if( ! checkCompileDockerHub() )
		{			
			def registry = environment.REPO_URL.split('//')[1]
			
			return registry +"/"+input.Compile.image.name
		}
		else
		{
			def image = ""
			
			if( input.Compile.image.namespace )
				image += input.Compile.image.namespace
			
			if( input.Compile.image.repo && image)
				image += "/" + input.Compile.image.namespace
			
			if( input.Compile.image.tag )
			{
				if( input.Compile.image.tag.contains(":") )
					image += input.Compile.image.tag
				else
					image += ":" + input.Compile.image.tag
			}
			
			return image
		}
	}
	
	def getContainerCompileArgs()
	{
		return input.Compile.imageArgs
	}
	
	def getCompileCommand()
	{
		return input.Compile.command + " " + input.Compile.args
	}
	
	def checkGenerateDBScript(image)
	{
		if( image.initDBScript == true )
			return image.initDBScript
		
		return false
	}
	
	def checkInputDBScript(image)
	{
		if( image.initDBScript.initDBScriptInput )
			return true
		else
			return false
	}
	
	def checkIfDB()
	{
		def db = false
		input.Images.each
		{
			image ->
				if( image.database == true)
				{
					log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Database Image found will be created " )
					db = true
				}
		}
		
		return db
	}
	
	def getArrowheadDB()
	{
		return input.ArrowHead.arrowHeadDB
	}
	
	def getArrowheadDBLog()
	{
		return input.ArrowHead.arrowHeadLogDB
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
	
	def generateDBScript(DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW, DB_ARROWHEAD, DB_ARROWHEAD_LOG)
	{
		def script = []
		
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
	
	def getDBScript(image)
	{
		return image.initDBScript.initDBScriptInput
	}
	
	def generateDockerFileDB(image, scriptName, ROOT_PSW)
	{
		def lines = []
		
		if( ! image )
		{
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "DB Image not found" )
			return ""
		}
		lines.add("FROM " + getDBImageName(image))
		lines.add("COPY ./"+scriptName+" /docker-entrypoint-initdb.d/")
		lines.add("ENV MYSQL_ROOT_PASSWORD=${ROOT_PSW}")
		return lines.join("\n")
	}
	
	def getDBImageName(image)
	{
		
		def registry = environment.REPO_URL.split('//')[1]
		
		return registry +"/"+ image.buildImage

	}
	
	def getPortusImageName(image)
	{
		def registry = environment.REPO_URL.split('//')[1]
		
		return registry + "/" + getCloudName() + "/" + image.repo
	}
	
	def getPortusTag(image)
	{
		def tag = ""
		
		tag += JenkinsApi.getBuildTimestamp()
		
		tag += "-" + Constants.DEFAULT_CLOUD_TAG + JenkinsApi.getBuildNumber()
		
		return tag
	}
	
	def getCloudName()
	{
		def cloud = input.NameSpace.cloud
		
		if( input.NameSpace.new && input.NameSpace.new == true )
		{
			cloud = Constants.DEFAULT_CLOUD_PREFIX + cloud + "-" + PortusApi.getPortusUserName()
			if( PortusApi.validateNamespace(cloud ) )
			{
				def code = PortusApi.validateNamespace(cloud , stdCloudTeam, "Cloud Namespace for User " + PortusApi.getPortusUserName())
				
				if( ! code )
				{
					log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Failed to create cloud " + cloud)
					return ""
				}
			}
		}
		
		return cloud
	}
}
