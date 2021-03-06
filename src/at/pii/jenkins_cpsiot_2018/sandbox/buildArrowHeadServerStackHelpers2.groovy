package at.pii.jenkins_cpsiot_2018.sandbox;

import groovy.json.*;
import org.boon.Boon;

//import groovy.json.JsonSlurperClassic

class buildArrowHeadServerStackHelpers2
{

	def log;
	
	def input;
	
	def Constants;
	
	def utils;
	
	def environment
	
	def PortusApi
	
	def stdCloudTeam
	
	def JenkinsApi
	
	def defaultNamespace
	
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
		
		defaultNamespace = generateDefaultNamespace()
	}
	
	def checkInput()
	{
		//~ if( ! input.NameSpace.name )
			//~ log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "NameSpace Name not found" )
		
		log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Input Check ran" )
	}
	
	def nothingToDo()
	{
		if( ! input.Images )
			return true
		
		return false
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
	
	def checkScriptPathDBScript(image)
	{
		if( image.initDBScript && ( image.initDBScript != true ) && image.initDBScript.initDBScriptPath )
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
		return input.ArrowHead.DB.arrowHeadDB
	}
	
	def getArrowheadDBLog()
	{
		return input.ArrowHead.DB.arrowHeadLogDB
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
	
	def getDBScriptPath(image)
	{
		return image.initDBScript.initDBScriptPath
	}
	
	def generateDockerFileDB(image)
	{
		def lines = []
		
		if( ! image )
		{
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "DB Image not found" )
			return ""
		}
		lines.add("FROM " + getDBImageName(image))		
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
		
		
		def arch = ""
		def a = image.buildImage.split("/")
		
		log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "After Split: " + a + ", from: " + image.buildImage)
		
		if( a )
		{
			def c = a[0].split("-")
			
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "To Search: " + c)

			def found = c.find{ it.contains("arm") || it.contains("amd64") || it.contains("x86") }
			
			if( found )
			{
				arch = "-" + found
				log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Architecture Found" )
			}
			else
				log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Architecture Not Found" )	
		}
		
		return registry + "/" + getDeloyNameSpace() + "/" + image.repo + arch
	}
	
	def getPortusTag(image)
	{
		def tag = ""
		
		tag += JenkinsApi.getBuildTimestamp()
		
		//~ tag += "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
		
		switch( getImageType(image) )
		{
			case Constants.IS_DB:
				tag += "-" + Constants.DB_TAG_ID + "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
				break;
			
			case Constants.IS_SR:
				tag += "-" + Constants.SR_TAG_ID + "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
				break;
		
			case Constants.IS_AUTH:
				tag += "-" + Constants.AUTH_TAG_ID + "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
				break;
		
			case Constants.IS_GW:
				tag += "-" + Constants.GATEWAY_TAG_ID + "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
				break;
		
			case Constants.IS_EH:
				tag += "-" + Constants.EH_TAG_ID + "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
				break;
		
			case Constants.IS_GK:
				tag += "-" + Constants.GK_TAG_ID + "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
				break;
		
			case Constants.IS_ORCH:
				tag += "-" + Constants.ORCH_TAG_ID + "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
				break;
			
			default:
				tag += "-" + Constants.DEFAULT_DEPLOY_TAG + JenkinsApi.getBuildNumber()
		}
		
		return tag
	}
	
	def getDeloyNameSpace()
	{
		def name = input.NameSpace.name
		
		if( input.NameSpace.new && input.NameSpace.new == true )
		{
			if( name == true )
				name = Constants.NAMESPACE_DEPLOY_PREFIX + defaultNamespace + "-" + PortusApi.getPortusUserName()
			else
				name = Constants.NAMESPACE_DEPLOY_PREFIX + name + "-" + PortusApi.getPortusUserName()
				
			if( PortusApi.validateNamespace( name ) )
			{
				if( input.NameSpace.team.new && input.NameSpace.team.new == true )
				{
					if( PortusApi.validateTeam(input.NameSpace.team.name, input.NameSpace.team.description) == -1 )
					{
						log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Team error " + input.NameSpace.team.name)
						return ""
					}
				}
				def code = PortusApi.validateNamespace(name , input.NameSpace.team.name, "Deploy Namespace for User " + PortusApi.getPortusUserName())
				
				if( ! code )
				{
					log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Failed to create cloud " + cloud)
					return ""
				}
			}
		}
		
		return name
	}
	
	def generateDefaultNamespace()
	{
		return "my-namespace-" + getTimeStamp()
	}
	
	def getTimeStamp()
	{
		return ( new Date().getTime() % 10000 )
	}
	
	def getImageName(image)
	{
		return image.repo
	}
	
	def getArtifactsPath(image)
	{
		return image.artifacts_path
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
				
				if( isArrowHead4() && key.equals("log4j.appender.DB.driver") )
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
						lines.add(key + "=" + "jdbc:mysql://" + input.NameSpace.cloud + Constants.DB_ADRESS_SUFFIX + ":3306/"+input.ArrowHead.DB.arrowHeadDB)
						return
					}
					
					if( key.equals("sr_address") )
					{
						lines.add(key + "=" + input.NameSpace.cloud + Constants.SR_ADRESS_SUFFIX )
						return
					}
					
					lines.add(key + "=" + value)
			}
		}
		
		return lines.join("\n")
	}
	
	def generateDockerFileArrowHeadService(image)
	{
		def lines = []
		
		
		lines.add("FROM " + getDBImageName(image))
		lines.add("COPY ./target/ " + image.workdir)
		lines.add("WORKDIR " + image.workdir)
		if( image.entry_point )
			lines.add("ENTRYPOINT [\"" + image.entry_point.join("\", \"") + "\" ]")
		
		return lines.join("\n")
		
	}
	
	def checkArrowServiceImageExists()
	{
		def found = false
		
		input.Images.each
		{
			image ->
				if( image.database == true)
				{
					return 
				}
				
				if( image.registry && image.registry == true )
					found = true
				
				if( image.authorization && image.authorization == true )
					found = true
				
				if( image.gateway && image.gateway == true )
					found = true
				
				if( image.eventhandler && image.eventhandler == true )
					found = true
				
				if( image.gatekeeper && image.gatekeeper == true )
					found = true
				
				if( image.orchestrator && image.orchestrator == true )
					found = true
				
				
		}
		
		return found
		
	}
	
	def getArrowHeadRepo()
	{
		return input.ArrowHead.Repo.git
	}
	
	def generateAppProperties4(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
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
		
		for( setting in image.Settings )
		{
			//~ lines.add( setting.getValue().getClass() )
			setting.getValue().each
			{
				key, value ->
			
		
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
				
				
				lines.add(key + "=" + value)
			}
		}
		
		return lines.join("\n")
	}	
	
	def isArrowHead3()
	{
		if( input.ArrowHead.Conf.arrowHead3 == true )
			return true
		else
			return false
	}
	
	def isArrowHead4()
	{
		if( input.ArrowHead.Conf.arrowHead4 == true )
			return true
		else
			return false
	}
	
	def getImageType(image)
	{
		if( image.database == true )
			return Constants.IS_DB;
		
		if( image.registry == true )
			return Constants.IS_SR;
		
		if( image.authorization == true )
			return Constants.IS_AUTH
		
		if( image.gateway == true )
			return Constants.IS_GW
		
		if( image.eventhandler == true )
			return Constants.IS_EH
		
		if( image.gatekeeper == true )
			return Constants.IS_GK
		
		if( image.orchestrator == true )
			return Constants.IS_ORCH
	}
}
