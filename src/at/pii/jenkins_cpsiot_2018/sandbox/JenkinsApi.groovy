package at.pii.jenkins_cpsiot_2018.sandbox;

import groovy.json.*;
//import groovy.json.JsonSlurperClassic

class JenkinsApi
{	
	def currentBuild;
	def Constants;
	def log;
	def environment;
	
	def init(currentBuild, environment, Constants)
	{
		this.currentBuild = currentBuild
		this.Constants = Constants
		this.environment = environment
		
		log = new Log()
		log.init(Constants)
		
		checkApi()
	}
	
	def checkApi()
	{
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "JenkinsApi init" )
		
		if( currentBuild.number )
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Current Build Number is " +  currentBuild.number)
		else
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Current Build Number not found " )
	}
	
	def getBuildNumber()
	{
		return currentBuild.number
	}
	
	def getLog()
	{
		return log
	}
	
	def getBuildTimestamp()
	{
		return environment.PORTUS_TAG_TIMESTAMP
	}
}
