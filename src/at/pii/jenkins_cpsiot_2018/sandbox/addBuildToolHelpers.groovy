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
}
