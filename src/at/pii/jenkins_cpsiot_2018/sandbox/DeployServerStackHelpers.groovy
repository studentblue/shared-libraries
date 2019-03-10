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
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "No Node to Deploy the Stack on is Online" )
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
			
			def DB_ARROWHEAD = getArrowheadDB()
			def DB_ARROWHEAD_LOG = getArrowheadDBLog()
			
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
	
	def getNodeName()
	{
		return input.Node.name
	}
	
	def getCloudNetwork()
	{
		return input.Docker.cloud
	}
	
	def getPortusImageName(image)
	{
		def registry = environment.REPO_URL.split('//')[1]
		
		return registry + "/" + image.deployImage
	}
	
	def initNetwork()
	{
		if( input.Node.networks.contains(getCloudNetwork()) )
			return false
		else
			return true
	}
	
	def getImageDockerName(image)
	{
		if( image.database == true )
			return input.ArrowHead.DB.arrowHeadDBAdress
		else
			return image.Settings.Network.address		
		
	}
	
	def checkImageContainer(image)
	{
		if( containerIsRunning(image) )
			return "docker stop " + getImageDockerName(image)
		else
			return ""
	}
	
	def containerIsExited(image)
	{
	}
	
	def containerIsRunning(image)
	{		
		if( input.Node.containers.contains(getImageDockerName(image)) )
			return true
		else
			return false
	}
	
	def startDBContainer(image, script, DEFAULT_DB_ROOT_PSW)
	{
		def dockerRun = []
		dockerRun.add("docker")
		dockerRun.add("run -d --rm")
		dockerRun.add("--name " + getImageDockerName(image))
		dockerRun.add("--network " + getCloudNetwork())
		dockerRun.add("-e MYSQL_ROOT_PASSWORD=" + DEFAULT_DB_ROOT_PSW)
		dockerRun.add("-v "+script.path+"/"+script.script+":/docker-entrypoint-initdb.d/" + script.script + ":ro")
		dockerRun.add(getPortusImageName(image))
		
		
		return dockerRun.join(" ")
		//~ create network for arrowhead cloud
		//~ sh " docker network create -d bridge ${NETWORK} "
		
		//~ start DB
		//~ docker run -d --name ${MY_SQL_SERVICE} \
		//~ --network ${NETWORK} -p ${MY_SQL_SERVER_PUBLISHED_PORT}:${MY_SQL_SERVER_TARGET_PORT} \
		//~ -e MYSQL_ROOT_PASSWORD=${DEFAULT_DB_ROOT_PSW} \
		//~ ${MY_SQL_SERVER_IMAGE_REPO}/${MY_SQL_SERVER_IMAGE_NAMESPACE}/${MY_SQL_SERVER_IMAGE_NAME}-${params.Build}-${env.ARCHITECTURE}:${params.My_SQL_Server_Version}

	}
	
	def startArrowHeadContainer(image, script)
	{
		def dockerRun = []
		dockerRun.add("docker")
		dockerRun.add("run -d --rm")
		dockerRun.add("--name " + getImageDockerName(image))
		dockerRun.add("--network " + getCloudNetwork())
		dockerRun.add("-v "+script.path+"/"+script.file.config+":"+image.workdir+"/"+ script.file.config + ":ro")
		dockerRun.add("-v "+script.path+"/"+script.file.log+":"+image.workdir+"/config/"+ script.file.log + ":ro")
		dockerRun.add(getPortusImageName(image))
		
		
		return dockerRun.join(" ")
	}
	
	def generateAppProperties(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
	{
		def lines = []
		//logger
		for( setting in image.Settings )
		{
			//~ lines.add( setting.getValue().getClass() )
			setting.getValue().each
			{
				key, value ->
			
					if( ! value && ( value != false ) )
						return
					
					if( key.equals("db_user") )
					{
						lines.add(key + "=" + DEFAULT_DB_ARROWHEAD_USR)
						return
					}
					
					if( key.equals("db_password") )
					{
						lines.add(key + "=" + DEFAULT_DB_ARROWHEAD_PSW)
						return
					}
					
					if( key.equals("db_address") )
					{
						lines.add(key + "=" + "jdbc:mysql://" + getDBAdress() + ":3306/"+input.ArrowHead.DB.arrowHeadDB)
						return
					}
					
					//~ if( key.equals("sr_address") )
					//~ {
						//~ lines.add(key + "=" + getSRAdress() )
						//~ return
					//~ }
					
					lines.add(key + "=" + value)
			}
		}
		
		return lines.join("\n")	
	}
	
	def generateLogProperties(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
	{
		def lines = []
		//logger
		input.Logger.DB.each
		{
			key, value ->
				
				if( key.equals("log4j.appender.DB.user") )
				{
					lines.add(key + "=" + DEFAULT_DB_ARROWHEAD_USR)
					return
				}
				
				if( key.equals("log4j.appender.DB.password") )
				{
					lines.add(key + "=" + DEFAULT_DB_ARROWHEAD_PSW)
					return
				}
				
				if( key.equals("log4j.appender.DB.driver") )
					return
				
				lines.add(key + "=" + value)
				
		}
		
		input.Logger.File.each
		{
			key, value ->
			
				lines.add(key + "=" + value)
				
		}
		
		input.Logger.General.each
		{
			key, value ->
			
				lines.add(key + "=" + value)
				
		}
		
		return lines.join("\n")
	}
	
	def getDBAdress()
	{
		return input.ArrowHead.DB.arrowHeadDBAdress
	}
	
	def getSRAdress()
	{
		def adress = ""
		input.Images.each
		{
			image ->
				if( image.registry == true )
					adress = image.Settings.Network.address
		}
		
		if( ! adress )
		{
			log.addEntry(Constants.WARNING, Constants.ACTION_CHECK, "No Service Registry found using defaults" )
			return getCloudNetwork() + Constants.SR_ADRESS_SUFFIX
			//my-cloud-service-registry
		}
		
		return adress
	}
	
	def removeNetwork()
	{
		def commands = []
		if( input.Node.networks.removeNetwork == true )
		{
			if( ! input.Node.networks.name )
			{
				return commands
			}
			else
			{
				
				if( input.Node.networks.containers )
				{
					def keys = []
					
					input.Node.networks.containers.each
					{
						key, container ->
							
							if( container.running == true )
								commands.add("docker stop " + container.name)
							else
								commands.add("docker rm " + container.name)
							
							keys.add(key)
							
					}
					
					keys.each
					{
						key ->
							input.Node.networks.containers[key].put("removed", true)
					}
				}
				
				commands.add("docker network rm " + input.Node.networks.name)
				
				input.Node.networks.put("removed", true)
				
				
			}
		}
		
		return commands
	}
	
	def getInput()
	{
		return input
	}
}
