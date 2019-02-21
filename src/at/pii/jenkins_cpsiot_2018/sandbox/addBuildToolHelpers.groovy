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
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Manifests not defined for this repo" )
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
}
