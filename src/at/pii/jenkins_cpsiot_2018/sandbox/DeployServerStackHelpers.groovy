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
	}
	
	def getParameter()
	{
		return input
	}
}