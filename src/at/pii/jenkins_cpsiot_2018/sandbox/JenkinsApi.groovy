package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
//import groovy.json.JsonSlurperClassic

class JenkinsApiClass
{	
	def currentBuild
	def outer
	
	JenkinsApiClass(outer)
	{
		this.outer = outer
	}
	
	def init(currentBuild)
	{
		this.currentBuild = currentBuild
	}
	
	def getBuildNumber()
	{
		return this.currentBuild.number
	}
}

def jenkinsApiInstance

def init()
{
	jenkinsApiInstance = new JenkinsApiClass(this)
}

return this
