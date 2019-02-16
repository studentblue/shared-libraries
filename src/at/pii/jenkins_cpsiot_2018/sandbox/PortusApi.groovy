package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*

//import groovy.json.JsonSlurperClassic

class PortusApiData implements Serializable
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
	
	def outer
	
	def manifests
	
	def chosenImage = ""
	
	PortusApiData(environment, buildParameters, outerClass)
	{
		this.environment = environment
		this.buildParameters = buildParameters
		this.outer = outerClass
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
		
		if( this.inputDockerHubRepo =~ /[^\w_\-.~\/\%:]+/ )
			return "DockerHub repo name cannot be valid"
		
		def findcool = this.inputPortusNameSpace  =~ /[^\w_\-.~\/:]+/
		
		if( findcool )
		{
			def message = "PortusNameSpace contains unallowed characters: "
			findcool.each
			{
				match ->
					message + "\"match\" "
			}
			return message
		}
		def 
		def inputPortusNameSpaceDescription 
		def inputPortusImageName
		
		return true
	}
	
	def private getPortusAuthHeaders()
	{
		def headers = []
		
		headers.add([name: "Portus-Auth", value: this.PortusUserName + ":" + this.inputPortusToken])
		return headers
	}

	def isPortusHealthy()
	{	
		def url = this.PortusUrl + this.healthApi
		def mode = 'GET'
		def headers = getPortusAuthHeaders()
		
		def health = outer.httpRequestWithPlugin(url, mode, headers)
		
		if( health == false )
			return outer.constants.ERROR_PORTUS_UNHEALTHY
		else
			return true
	}
	
	def getManifestsFromDockhub()
	{				
		def image = this.inputDockerHubRepo
		def resolve = this.inputDockerHubRepo.split(':')
		
		def tag = "latest"
		
		if( resolve.length == 1 )
		{
			if( ! image.contains("/") )
				image = "library/" + image
			
			if( this.inputDockerHubTag )
				tag = this.inputDockerHubTag
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
				return outer.constants.ERROR
		}
	
		def login_template = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:${image}:pull"
		def get_manifest_template = "https://registry.hub.docker.com/v2/${image}/manifests/${tag}"
		def accept_types = "application/vnd.docker.distribution.manifest.list.v2+json,application/vnd.docker.distribution.manifestv2+json"
		
		def response = outer.httpRequestWithPlugin(login_template, 'GET')
		def responseGroovy = ""
		
		if( response.status == 200 )
		{
			responseGroovy =  new JsonSlurperClassic().parseText(response.content)
			
			def dockerHubToken = responseGroovy["token"]
			def headers = [[name: "Authorization", value: "Bearer ${dockerHubToken}"], [name: "accept", value: accept_types]]
			
			response = outer.httpRequestWithPlugin(get_manifest_template, 'GET', headers)
			
			if( response.status == 200 )
			{
				responseGroovy =  new JsonSlurperClassic().parseText(response.content)
				this.manifests = responseGroovy
				return true
			}
			else
				return outer.constants.ERROR
		}
		else
			return outer.constants.ERROR
	}
	
	def testConstants()
	{
		return outer.constants.ERROR
	}
	
	def getChoices()
	{
		def choices = []
	
		if( ! this.manifests["manifests"] )
			return choices
		
		this.manifests["manifests"].each
		{
			manifest ->
				
				def manifestDesc = ""
				
				manifest["platform"].keySet().each
				{
					detail ->
						manifestDesc += detail + ": " + manifest["platform"][detail] + ", "
				}
				
				def values = manifest["digest"].split(':')
				
				manifestDesc += outer.constants.SPLITTER + values[1].substring(0,10)
				
				choices.add(manifestDesc)
		}
		return choices
	}
	
	def setChoice(userInput)
	{
		this.chosenImage = userInput
	}
}

def PortusData

def init(environment, buildParameters)
{
	PortusData = new PortusApiData(environment, buildParameters, this)
}
	
def httpRequestWithPlugin(url, mode, headers = [])
{	
	def response = httpRequest httpMode: mode, url: url, acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', customHeaders: headers
	
	if( response.status == 200 )
	{
		return response
	}
	else
		return false
}

def getCall

return this
