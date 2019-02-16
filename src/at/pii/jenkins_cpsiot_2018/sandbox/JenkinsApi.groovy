package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
//import groovy.json.JsonSlurperClassic

class JenkinsApi
{	
	def currentBuild
	
	JenkinsApi(currentBuild)
	{		
		this.currentBuild = currentBuild
	}
	
	def getBuildNumber()
	{
		return this.currentBuild.number
	}
}
