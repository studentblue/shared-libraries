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
	
	def environment
	
	def init( inputParameter, Constants, environment  )
	{
		input = new JsonSlurperClassic().parseText(inputParameter)
		
		this.Constants = Constants
		
		this.environment = environment
		
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
	
	def generateDBScript(image)
	{
		if( image.initDBScript == true )
			return image.initDBScript
		
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
		return input.ArrowHead.arrowHeadDB
	}
	
	def getArrowheadDBLog()
	{
		return input.ArrowHead.arrowHeadLogDB
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
}
