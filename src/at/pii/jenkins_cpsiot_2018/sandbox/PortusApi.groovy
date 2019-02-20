package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
import org.boon.Boon

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
	def inputCustomTeamName
	
	def environment
	def buildParameters
	
	def healthApi = "/api/v1/health"
	
	def outer
	
	def manifests
	
	def chosenImage = ""
	
	def defaultImageName = ""
	def defaultNameSpace = ""
	
	def PortusNameSpace = ""
	def PortusImageName = ""
	
	def digest = ""
	
	def 
	
	PortusApiData(outerClass)
	{		
		this.outer = outerClass
	}
	
	def init(environment)
	{
		def userInput = Boon.fromJson(environment.AddBuildTool)
		this.PortusUrl = environment.REPO_URL
		this.PortusUserName = environment.PORTUS_USER
		
		
		this.inputDockerHubRepo = environment.userInput.DockerHub.repo
		this.inputDockerHubTag = environment.userInput.DockerHub.tag
		
		this.inputPortusToken = environment.TOKEN2
		this.inputPortusTeam = environment.userInput.Portus.team
		this.inputPortusNameSpace = environment.userInput.Portus.namespace
		this.inputPortusNameSpaceDescription = environment.userInput.Portus.description
		this.inputPortusImageName = environment.userInput.Portus.repo
		
		//~ this.inputDockerHubRepo = environment.DockerHub_Repo_Name
		//~ this.inputDockerHubTag = environment.Tag_Name
		
		//~ this.inputPortusToken = environment.TOKEN2
		//~ this.inputPortusTeam = environment.Teams
		//~ this.inputPortusNameSpace = environment.NameSpace
		//~ this.inputPortusNameSpaceDescription = environment.NameSpace_Description
		//~ this.inputPortusImageName = environment.Image_Name
		//~ this.inputCustomTeamName = environment.Custom_Team_Name
		return userInput
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
		
		def findcool = this.inputPortusNameSpace  =~ /[^\w_.~]+/
		
		if( findcool )
		{
			def message = "Portus NameSpace contains unallowed characters: "
			findcool.each
			{
				match ->
					message += "\"${match}\" "
			}
			return message
		}
		
		findcool = this.inputPortusImageName =~ /[^\w_\-.~]+/
		
		if( findcool )
		{
			def message = "Portus Image Name contains unallowed characters: "
			findcool.each
			{
				match ->
					message += "\"${match}\" "
			}
			return message
		}
		
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
		
		this.digest = getDigestFromString()
		
		this.defaultImageName = generateDefaultImageName()
		this.defaultNameSpace = generateDefaultNameSpace()
		
		//def PortusNameSpace = ""
		//def PortusImageName = ""
		
		if( ! this.inputPortusNameSpace )
			this.PortusNameSpace = this.defaultNameSpace
		else
			this.PortusNameSpace = this.inputPortusNameSpace
		
		if( ! this.inputPortusImageName )
			this.PortusImageName = this.defaultImageName
		else
			this.PortusImageName = this.inputPortusImageName
	}
	
	def pushImage()
	{
	}
	
	def getDigestFromString()
	{
	
		def digest = ""
		
		if( ! this.chosenImage )
			return digest
		
		if( ! this.manifests["manifests"] )
			return digest
	
		def values = this.chosenImage.split(outer.constants.SPLITTER)
		def pattern = values[1].trim()
		
		this.manifests["manifests"].each
		{
			manifest ->
				// "digest": "sha256:3be17715f14ac6f0834554ab4fc7a7440449690e58d45291dfae420c8d3422f1",
				
				println manifest
				def temp = manifest["digest"]
				
				def values2 = temp.split(':')
				
				
				def match = values2[1].substring(0,10).trim()
				if( match.equals(pattern) )
				{
					digest = manifest["digest"]
					return true 
				}
		}
		
		return digest
	}
	
	def generateDefaultImageName()
	{
		def image = this.inputDockerHubRepo
		def resolve = image.split(':')
		
		if( resolve.length == 1 )
		{
			return outer.constants.DEFAULT_IMAGE_PREFIX + image.replaceAll("[\\W]", '_')
		}
		else
		{
			return outer.constants.DEFAULT_IMAGE_PREFIX + resolve[0].replaceAll("[\\W]", '_')
		}
	}
	
	def generateDefaultNameSpace()
	{	
		
		if( ! this.digest )
			return outer.constants.DEFAULT_NAMESPACE_PREFIX + constants.UNKNOWN_ARCH_OS
		
		//DEFAULT_NAMESPACE_PREFIX
		def defaultNameSpace = outer.constants.DEFAULT_NAMESPACE_PREFIX
		def platform = getPlatformFromDigest()
		
		def list = []
		platform.keySet().each
		{
			key ->
				list.add(platform[key])
		}
		
		return defaultNameSpace + list.join('_')
		
	}
	
	def getPlatformFromDigest()
	{
		
		def platform = []
		
		this.manifests["manifests"].each
		{
			manifest ->
				
				if( manifest["digest"].equals(this.digest) )
				{
					platform = manifest["platform"]
					return true 
				}
		}
		
		return platform
	}
	
	
}

def PortusData

def init()
{
	PortusData = new PortusApiData(this)
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
