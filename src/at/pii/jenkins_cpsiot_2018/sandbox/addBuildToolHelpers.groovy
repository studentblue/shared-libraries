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
	
	def JenkinsApi
	def utils
	
	def userChoice
	
	def digest
	
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
		{
			def code = PortusApi.validateTeam(team, teamDescription)
			if( code == -1 )
			{
				log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Validation of team \"" + team + "\" failed")
				return ""
			}
		}
		
		if( checkNamespace )
		{
			def code = PortusApi.validateNamespace(namespace, team, description)
		
			if( code == -1 )
			{
				log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Validation of namespace \"" + namespace + "\" failed")
				return ""
			}
		}
		
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
			if( DockerHub.getTag() )
				tag = DockerHub.getTag() + "-" + JenkinsApi.getBuildNumber()
			else
				tag = Constants.DEFAULT_TAG_PREFIX + "-" + JenkinsApi.getBuildNumber()
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
				
				tag = DockerHub.getTag() + "-" +JenkinsApi.getBuildNumber()
			}
		}
		
		//~ if( ! PortusApi.checkNamespaceRepoTag(namespace, repo, tag ) )
			//~ log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Image already exists in repo for namespace and tag")
		
		return ["namespace": namespace, "repo": repo, "tag": tag, "image": (namespace + "/" + repo), "portusRepo": PortusApi.getPortusRegistryName()]
	}
	
	def generateDefaultImageName(name, tag = "")
	{
		name = name.replaceAll("^[\\W]*", "")
		name = name.replaceAll("[\\W_]*\$", "")
		name = name.replaceAll("[^\\w_-]", "-")
		
		if( tag )
			return Constants.DEFAULT_IMAGE_PREFIX + name + "-" + tag
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
