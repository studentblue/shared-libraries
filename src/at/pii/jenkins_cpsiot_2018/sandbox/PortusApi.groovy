package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
//import groovy.json.JsonSlurperClassic

class PortusApi
{
	def environment
	def currentBuild
	def buildParameters
	
	PortusApi(environment, currentBuild, buildParameters)
	{
		this.environment = environment
		this.currentBuild = currentBuild
		this.buildParameters = buildParameters
	}
		
	def checkInputParameters()
	{
		println this.environment.getClass()
		println this.buildParameters.getClass()
		
		this.environment.each
		{
			var1 ->
				println var1
		}
		
		this.buildParameters.each
		{
			var1 ->
				println var1
		}
	}
}
