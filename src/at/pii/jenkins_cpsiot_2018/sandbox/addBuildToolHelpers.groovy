package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
import org.boon.Boon

//import groovy.json.JsonSlurperClassic

class addBuildToolHelpers
{

	def log
	
	def PortusApi
	
	def input 
	
	def Constants
	
	def DockerHub
	def utils
	
	def userChoice
	
	def digest
	
	def init( inputParameter, PortusApi, DockerHub, Constants  )
	{
		input = new JsonSlurperClassic().parseText(inputParameter)
		
		this.PortusApi = PortusApi
		this.Constants = Constants
		this.DockerHub = DockerHub
		
		log = new Log()
		log.init(Constants)
		
		utils = new Utils()
		
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "addBuildToolHelpers init" )
		
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
					teamDescription = input.Namespace.team.name
				
				checkTeam = true
			}
			else
			{
				team = input.Namespace.team.name
			}
			
			checkNamespace = true
		}
		
		
		if( checkTeam )
			validateTeam(team, teamDescription)
		
		if( checkNamespace )
			validateNamespace(namespace, team, description)
		
		
		
		return namespace + "/" + repo + ":" + tag
	}
	
	def validateNamespace(namespace, team, description)
	{
		def code = PortusApi.validateNamespace(namespace)
		
		if( code == -1 )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Validation of namespace \"" + namespace + "\" failed")
		else
		{
			return true
		}
	}
	
	def validateTeam(team, teamDescription)
	{
		def code = PortusApi.validateTeam(team, teamDescription)
		
		if( code == -1 )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Validation of team \"" + team + "\" failed")
		
		return
	}
	
	def generateDefaultNameSpace()
	{	
		def user = PortusApi.PortusUserName()
		
		user = user.replace(/^[\W_]*/, "")
		user = user.replace(/[\W_]*$/, "")
		
		if( ! digest )
			return Constants.DEFAULT_NAMESPACE_PREFIX + Constants.UNKNOWN_ARCH_OS + "-" + user
		
		//DEFAULT_NAMESPACE_PREFIX
		def defaultNameSpace = Constants.DEFAULT_NAMESPACE_PREFIX
		def platform = DockerHub.getPlatformFromDigest()
		
		def list = []
		platform.keySet().each
		{
			key ->
				list.add(platform[key])
		}
		
		return defaultNameSpace + list.join('_') + "-" + user
		
	}
	
	def generateDefaultDescription()
	{	
		def user = PortusApi.getPortusUserName()
		
		
		
		return "Namespace created by User " + user
		
	}
	
	def pushImage()
	{
	}
}
