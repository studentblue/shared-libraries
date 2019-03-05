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
	
	def init( inputParameter, Constants  )
	{
		input = new JsonSlurperClassic().parseText(inputParameter)
		
		this.Constants = Constants		
		
		log = new Log()
		log.init(Constants)
		
		utils = new Utils()
		
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "buildArrowHeadServerStackHelpers init" )
		
		checkInput()
	}
	
	def checkInput()
	{
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
}
