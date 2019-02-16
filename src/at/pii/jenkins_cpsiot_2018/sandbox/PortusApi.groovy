package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
//import groovy.json.JsonSlurperClassic

class PortusApi implements Serializable
{
	//Folder Properties
	def PortusUrl
	def PortusUserName
	
	//Build Parameters from Input
	//Docker
	def inputDockerHubRepo
	def inputDockerHubTag
	//Portus
	def inputPortusToken
	def inputPortusTeam
	def inputPortusNameSpace 
	def inputPortusNameSpaceDescription 
	def inputPortusImageName
	
	def environment
	def buildParameters
	
	def healthApi = "/api/v1/health"
	
	PortusApi(environment, buildParameters)
	{
		this.environment = environment
		this.buildParameters = buildParameters
	}
	
	def init()
	{
		this.PortusUrl = environment.REPO_URL
		this.PortusUserName = environment.PORTUS_USER
		
		this.inputDockerHubRepo = environment.DockerHub_Repo_Name
		this.inputDockerHubTag = environment.Tag_Name
		
		this.inputPortusToken = environment.TOKEN2
		this.inputPortusTeam = environment.Teams
		this.inputPortusNameSpace = environment.NameSpace
		this.inputPortusNameSpaceDescription = environment.NameSpace_Description
		this.inputPortusImageName = environment.Image_Name
	}
	
	def getVars()
	{
		return	this.PortusUrl + "\n" +
				this.PortusUserName + "\n" +
				this.inputDockerHubRepo + "\n" +
				this.inputDockerHubTag + "\n" +
				this.inputPortusToken + "\n" +
				this.inputPortusTeam + "\n" +
				this.inputPortusNameSpace + "\n" +
				this.inputPortusNameSpaceDescription + "\n" +
				this.inputPortusImageName
	}
		
	def checkInputParameters()
	{
		if ( ! this.PortusUrl )
			return "Portus Url not found"
		
		if ( ! this.PortusUserName )
			return "Portus User Name not found"
		
		if ( ! this.inputDockerHubRepo )
			return "DockerHub repo name is empty"
		
		if ( ! this.inputPortusToken )
			return "Portus Token is empty"
		
		if ( ! this.inputPortusTeam )
			return "Portus Team is empty"
		
		return true
	}
	

	def isPortusHealthy()
	{
		def health = this.portusApiGetCall(this.PortusUrl, this.PortusUserName, this.inputPortusToken, this.healthApi)
		
		if( health == false )
			return constants.ERROR_PORTUS_UNHEALTHY
		else
			return true
	}
	
	def portusApiGetCall(url, user, token, api)
	{
		def portusAuthToken = user + ":" + token
		def headers = [[name: "Portus-Auth", value: portusAuthToken]]
		

		def response = httpRequest httpMode: 'GET', url: url + api, customHeaders: headers
		
		if( response.status == 200 )
		{
			responseGroovy =  new JsonSlurperClassic().parseText(response.content)
			return responseGroovy
		}
		else
			return false
	}
	
	def getManifestsFromDockhub()
	{
		def test = 1
		def image = repo
		def resolve = repo.split(':')
		
		def tag = "latest"
		
		if( resolve.length == 1 )
		{
			if( ! image.contains("/") )
				image = "library/" + image
			
			if( tagArg )
				tag = tagArg
		}
		else
		{
			if( resolve.length == 2 )
			{
				image = resolve[0]
				
				if( ! image.contains("/") )
					image = "library/" + image
				
				tag = resolve[1]
			}
			else
				return constants.ERROR
		}
	
		def login_template = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:${image}:pull"
		def get_manifest_template = "https://registry.hub.docker.com/v2/${image}/manifests/${tag}"
		def accept_types = "application/vnd.docker.distribution.manifest.list.v2+json,application/vnd.docker.distribution.manifestv2+json"
		
		def response2 = httpRequest httpMode: 'GET', url: login_template
		def response2Groovy = ""
		
		if( response2.status == 200 )
		{
			response2Groovy =  new JsonSlurperClassic().parseText(response2.content)
		}
		else
			return constants.ERROR
		
		def dockerHubToken = response2Groovy["token"]
		
		def headers = [[name: "Authorization", value: "Bearer ${dockerHubToken}"], [name: "accept", value: accept_types]]
		
		def response3 = httpRequest httpMode: 'GET', url: get_manifest_template, contentType: 'APPLICATION_JSON', customHeaders: headers
		def response3Groovy = ""
		
		if( response3.status == 200 )
		{
			response3Groovy =  new JsonSlurperClassic().parseText(response3.content)
			return response3Groovy
		}
		
		return constants.ERROR
		
	}
}
