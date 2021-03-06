package at.pii.jenkins_cpsiot_2018.sandbox;

import groovy.json.*;
//import org.boon.Boon;

//import groovy.json.JsonSlurperClassic

class addBuildToolHelpers
{

	def log;
	
	def PortusApi;
	
	def input;
	
	def Constants;
	
	def DockerHub;
	
	def JenkinsApi;
	def utils;
	
	def userChoice;
	
	def digest;
	
	def init( inputParameter, PortusApi, DockerHub, JenkinsApi,  Constants  )
	{
		input = new JsonSlurperClassic().parseText(inputParameter)
		
		this.PortusApi = PortusApi
		this.Constants = Constants
		this.DockerHub = DockerHub
		this.JenkinsApi = JenkinsApi
		
		log = new Log()
		log.init(Constants)
		
		utils = new Utils()
		
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "addBuildToolHelpers init" )
		
		if( ! ( PortusApi || DockerHub || JenkinsApi || utils) )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Tools init failed" )
		
		checkInput()
	}
	
	def checkInput()
	{
		
		if(! input.DockerHub )		
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Object DockerHub not found in Json" )
		else
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Object DockerHub found in Json" )
		
		
		if(! input.Namespace )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Object Namespace not found in Json" )
		else
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Object Namespace found in Json" )
		
		if(! input.Repo )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Object Repo not found in Json" )
		else
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Object Repo found in Json" )
	}
	
	def getLog()
	{
		return log
	}
	
	def getChoices()
	{
		def choices = []
	
		if( ! DockerHub.getManifests()["manifests"] )
		{
			log.addEntry(Constants.LOG, Constants.ACTION_ALERT, "Manifests not defined for this repo" )
			return choices
		}
		
		DockerHub.getManifests()["manifests"].each
		{
			manifest ->
				
				def manifestDesc = ""
				
				manifest["platform"].keySet().each
				{
					detail ->
						manifestDesc += detail + ": " + manifest["platform"][detail] + ", "
				}
				
				def values = manifest["digest"].split(':')
				
				manifestDesc += Constants.SPLITTER + values[1].substring(0,10)
				
				choices.add(manifestDesc)
		}
		return choices
	}
	
	def setChoice(userInput)
	{
		userChoice = userInput
		
		digest = DockerHub.getDigestFromString(userChoice)
		
		log.addEntry(Constants.LOG, Constants.ACTION_CHOICE, "User selected " + userChoice )
		log.addEntry(Constants.LOG, Constants.ACTION_SET, "Digest set to " + digest )
		
		//~ this.defaultImageName = generateDefaultImageName()
		//~ this.defaultNameSpace = generateDefaultNameSpace()
		
		//~ def PortusNameSpace = ""
		//~ def PortusImageName = ""
		
		//~ if( ! this.inputPortusNameSpace )
			//~ this.PortusNameSpace = this.defaultNameSpace
		//~ else
			//~ this.PortusNameSpace = this.inputPortusNameSpace
		
		//~ if( ! this.inputPortusImageName )
			//~ this.PortusImageName = this.defaultImageName
		//~ else
			//~ this.PortusImageName = this.inputPortusImageName
	}
	
	def generatePortus()
	{
		def namespace = ""
		def repo = ""
		def tag = ""
		def team = ""
		def description = ""
		def teamDescription = ""
		
		def checkNamespace = true
		def checkTeam = true
		
		//existing namespace , team from namespace
		if( input.Namespace.teamFromNamespace == true && input.Namespace.name.name )
		{
			namespace = input.Namespace.name.name
			team = input.Namespace.name.team
			
			checkNamespace = false
			checkTeam = false
		}
		else
		{
		
			//generate namespace
			if( input.Namespace.newName == true )
				namespace = generateDefaultNameSpace()
			else
			{
				if( !input.Namespace.newName )
					namespace = generateDefaultNameSpace()
				else
					namespace = input.Namespace.newName
			}

			//generate description
			if( input.Namespace.description == true )
				description = generateDefaultDescription()
			else
			{
				if( input.Namespace.description )
					description = input.Namespace.description
				else
					description = generateDefaultDescription()
			}
			
			//team
			if( input.Namespace.team.new == true )
			{
				team = input.Namespace.team.name
				if( input.Namespace.team.description )
					teamDescription = input.Namespace.team.description
				else
					teamDescription = "My new Team"
				
				checkTeam = true
			}
			else
			{
				team = input.Namespace.team.name
			}
			
			checkNamespace = true
		}
		
		
		//~ if( checkTeam )
		//~ {
			//~ def code = PortusApi.validateTeam(team, teamDescription)
			//~ if( code == -1 )
			//~ {
				//~ log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Validation of team \"" + team + "\" failed")
				//~ return ""
			//~ }
		//~ }
		
		//~ if( checkNamespace )
		//~ {
			//~ def code = PortusApi.validateNamespace(namespace, team, description)
		
			//~ if( code == -1 )
			//~ {
				//~ log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Validation of namespace \"" + namespace + "\" failed")
				//~ return ""
			//~ }
		//~ }
		
		//~ def repo = ""
		//~ def tag = ""
		
		def dockerhubRepo = DockerHub.getRepoName()
		def dockerTag = DockerHub.getTag()
		
		if( input.Repo.name == true )
		{
			//generate repo name
			
			
			if( ! dockerhubRepo )
			{
				log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Dockerhub Repo Name not found.")
			}
			
			repo = generateDefaultImageName(dockerhubRepo, dockerTag)
		}
		else
		{
			if( input.Repo.name )
			{
				repo = input.Repo.name.replaceAll("^[\\W-_]*", "")
				repo = repo.replaceAll("[\\W_]*\$", "")
				repo = repo.replaceAll("[^\\w-]*", "")
				
			}
			else
				repo = generateDefaultImageName(dockerhubRepo, dockerTag)
		}
		
		if( input.Repo.tag == true )
		{
			//generate tag
			//~ if( DockerHub.getTag() )
				//~ tag = DockerHub.getTag() + "-" + JenkinsApi.getBuildNumber()
			//~ else
				//~ tag = Constants.DEFAULT_TAG_PREFIX + "-" + JenkinsApi.getBuildNumber()
			
			tag = generateDefaultTagName()
		}
		else
		{
			if( input.Repo.tag )
			{
				tag = input.Repo.tag.replaceAll("^[\\W-_]*", "")
				tag = tag.replaceAll("[\\W_]*\$", "")
				tag = tag.replaceAll("[^\\w-]*", "")
			}
			else
			{
				
				tag = generateDefaultTagName()
			}
		}
		
		//~ if( ! PortusApi.checkNamespaceRepoTag(namespace, repo, tag ) )
			//~ log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Image already exists in repo for namespace and tag")
		
		//~ def namespace = ""
		//~ def repo = ""
		//~ def tag = ""
		//~ def team = ""
		//~ def description = ""
		//~ def teamDescription = ""
		
		
		return [
					"namespace": [name: namespace, description: description],
					"team": [name: team, description: teamDescription],
					"repo": [ name: repo, tag: tag ], 
					"image": [ name: (PortusApi.getPortusRegistryName() + "/" + namespace + "/" + repo) ],
					checkTeam: checkTeam, checkNamespace: checkNamespace
		]
	}
	
	def generateDefaultTagName()
	{
		switch( getBuildToolType() )
		{
			case Constants.IS_TYPE_JAVA:
				return JenkinsApi.getBuildTimestamp() + "-" + Constants.TYPE_JAVA_TAG_ID + "-" + Constants.DEFAULT_TAG_PREFIX + "-" + JenkinsApi.getBuildNumber()
				break
			
			case Constants.IS_TYPE_MYSQL:
				return JenkinsApi.getBuildTimestamp() + "-" + Constants.TYPE_MYSQL_TAG_ID + "-" + Constants.DEFAULT_TAG_PREFIX + "-" + JenkinsApi.getBuildNumber()
				break
			
			case Constants.IS_TYPE_POSTGRES:
				return JenkinsApi.getBuildTimestamp() + "-" + Constants.TYPE_POSTGRES_TAG_ID + "-" + Constants.DEFAULT_TAG_PREFIX + "-" + JenkinsApi.getBuildNumber()
				break
			
			case Constants.IS_TYPE_OTHER:
				return JenkinsApi.getBuildTimestamp() + "-" + Constants.TYPE_OTHER_TAG_ID + "-" + Constants.DEFAULT_TAG_PREFIX + "-" + JenkinsApi.getBuildNumber()
				break
			
			case Constants.IS_TYPE_MAVEN:
				return JenkinsApi.getBuildTimestamp() + "-" + Constants.TYPE_MAVEN_TAG_ID + "-" + Constants.DEFAULT_TAG_PREFIX + "-" + JenkinsApi.getBuildNumber()
				break
			
			default:
				break
		}
		return JenkinsApi.getBuildTimestamp() + "-" + Constants.DEFAULT_TAG_PREFIX + "-" + JenkinsApi.getBuildNumber()
	}
	
	def getBuildToolType()
	{
		def type = input.Repo.type.trim()
		
		if( input.Repo.type.equals(Constants.BUILD_TOOL_TYPE_JAVA) )
			return Constants.IS_TYPE_JAVA
		
		if( input.Repo.type.equals(Constants.BUILD_TOOL_TYPE_MYSQL) )
			return Constants.IS_TYPE_MYSQL
		
		if( input.Repo.type.equals(Constants.BUILD_TOOL_TYPE_POSTGRES) )
			return Constants.IS_TYPE_POSTGRES
		
		if( input.Repo.type.equals(Constants.BUILD_TOOL_TYPE_OTHER) )
			return Constants.IS_TYPE_OTHER
		
		if( input.Repo.type.equals(Constants.BUILD_TOOL_TYPE_MAVEN) )
			return Constants.IS_TYPE_MAVEN
		
		
		return 0
	}
	
	def generateDefaultImageName(name, tag = "")
	{
		name = name.replaceAll("^[\\W]*", "")
		name = name.replaceAll("[\\W_]*\$", "")
		name = name.replaceAll("[^\\w_-]", "-")
		
		if( tag )
		{
			def tagSani = tag
			tagSani = tag.replaceAll("^[\\W]*", "")
			tagSani = tagSani.replaceAll("[\\W_]*\$", "")
			tagSani = tagSani.replaceAll("[^\\w_-]", "-")
			return Constants.DEFAULT_IMAGE_PREFIX + name + "-" + tagSani
		}
		else
			return Constants.DEFAULT_IMAGE_PREFIX + name
	}
	
	def generateDefaultNameSpace()
	{	
		def user = PortusApi.getPortusUserName()
		
		
		
		//~ if( ! digest )
		//~ {
			//~ def manifest = DockerHub.getManifests()
			
			//~ if( manifest.schemaVersion == 1)
				//~ return Constants.DEFAULT_NAMESPACE_PREFIX + manifest.architecture + "-" + user
			//~ else
				//~ return Constants.DEFAULT_NAMESPACE_PREFIX + Constants.UNKNOWN_ARCH_OS + "-" + user
		//~ }
		
		//~ //DEFAULT_NAMESPACE_PREFIX
		//~ def defaultNameSpace = Constants.DEFAULT_NAMESPACE_PREFIX
		//~ def platform = DockerHub.getPlatformFromDigest(digest)
		
		//~ def list = []
		//~ platform.keySet().each
		//~ {
			//~ key ->
				//~ list.add(platform[key])
		//~ }
		
		//~ return defaultNameSpace + list.join('_') + "-" + user
		
		user = user.replace(/^[\W_]*/, "")
		user = user.replace(/[\W_]*$/, "")
		
		return Constants.DEFAULT_NAMESPACE_PREFIX + DockerHub.getPlatform(digest) + "-" + user
		
	}
	
	def generateDefaultDescription()
	{	
		def user = PortusApi.getPortusUserName()
		
		
		
		return "Namespace created by User " + user
		
	}
}
