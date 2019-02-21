package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
import org.boon.Boon

//import groovy.json.JsonSlurperClassic

class PortusApi
{
	//Folder Properties
	def PortusUrl
	def PortusUserName
	def portusToken
	def PortusUserId 
		
	def healthApi = "/api/v1/health"
	def usersApi = "/api/v1/users"
	def pingApi = "/api/v1/_ping"
	def namespacesApi = "/api/v1/namespaces"
	def repositoryApi = "/api/v1/repositories"
	def tagsApi = "/api/v1/tags"
	def teamsApi = "/api/v1/teams"
	
	def utils
	def Constants
	def log
	
	def init(PortusUrl, PortusUserName, portusToken, PortusUserId, Constants)
	{	
		this.PortusUrl = PortusUrl
		this.PortusUserName = PortusUserName
		this.portusToken = portusToken
		this.PortusUserId = PortusUserId
		
		this.Constants = Constants
		
		log = new Log()
		log.init(Constants)
		
		utils = new Utils()
		
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "PortusApi init" )
		
		if( ! ( PortusUrl || PortusUserName || portusToken || PortusUserId))
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "PortusApi init failed" )
		
		isPortusHealthy()
	}
	
	def private getPortusAuthHeaders()
	{
		def headers = []
		
		headers.add([name: "Portus-Auth", value: this.PortusUserName + ":" + this.portusToken])
		return headers
	}

	def isPortusHealthy()
	{	
		def url = PortusUrl + healthApi
		def mode = Constants.HTTP_MODE_GET
		
		def health = makeRequest(url, mode)
		
		if( health.status != Constants.HTTP_RESPONSE_OK )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Portus is Unhealthy: " + health.content)
		
		log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Portus is healthy: " + health.content)
		
		return true
			
	}
	
	def validateNamespace(namespace)
	{
		def url = PortusUrl + namespacesApi + "/validate?name=" + namespace
		def mode = Constants.HTTP_MODE_GET
		
		def response = makeRequest(url, mode, getPortusAuthHeaders())
		
		if( response.status == Constants.HTTP_RESPONSE_OK )
		{
			def content = new JsonSlurperClassic().parseText(response.content)
			log.addEntry(Constants.LOG, Constants.ACTION_VALIDATE, "Namespace: " + namespace + " = " + content.valid)
			return content.valid
		}
		else
		{
			log.addEntry(Constants.ERROR, Constants.HTTP_ERROR, "Tried to validate namespace \""+namespace+"\" got Code " + response.status)
			return -1
		}
	}
	
	def validateTeam(teamToFind, teamDescription)
	{
		def url = PortusUrl + teamsApi + "?all=true"
		def mode = Constants.HTTP_MODE_GET
		
		def response = makeRequest(url, mode, getPortusAuthHeaders())
		
		if( response.status == Constants.HTTP_RESPONSE_OK )
		{
			def content = new JsonSlurperClassic().parseText(response.content)
			def found = false
			for( team in content )
			{
					if( team.name.equals( teamToFind ) )
					{
						found = true
						break
					}
			}
			
			if( found )
			{
				log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Team: " + teamToFind + " found must not be created ")
				return true
			}
			else
			{
				def body = JsonOutput.toJson([name: teamToFind, description: teamDescription, owner_id: PortusUserId])
				
				response = makeRequest(PortusUrl + teamsApi, Constants.HTTP_MODE_POST, getPortusAuthHeaders(), body)
				
				if( response.status == Constants.HTTP_RESPONSE_CREATED )
				{
					content = new JsonSlurperClassic().parseText(response.content)
					log.addEntry(Constants.LOG, Constants.TEAM_CREATED, "Team: " + teamToFind + " = " + content)
					return true
				}
				else
				{
					log.addEntry(Constants.ERROR, Constants.HTTP_ERROR, "Tried to create team \""+teamToFind+"\" got Code " + response.status)
					return -1
				}
			}
		}
		else
		{
			log.addEntry(Constants.ERROR, Constants.HTTP_ERROR, "Tried to validate team \""+teamToFind+"\" got Code " + response.status)
			return -1
		}
	}
	
	def postNamespace(namespace, team, description)
	{
		def url = PortusUrl + namespacesApi
		def mode = Constants.HTTP_MODE_POST
		def body = JsonOutput.toJson([name: namespace, team: team, description: description])
		
		
		def response = makeRequest(url, mode, getPortusAuthHeaders(), body)
		def content = new JsonSlurperClassic().parseText(response.content)
		
		if( response.status == Constants.HTTP_RESPONSE_CREATED )
		{
			log.addEntry(Constants.LOG, Constants.NAMESPACE_CREATED, "Namespace: " + namespace + " = " + content)
			return true
		}
		else
		{
			log.addEntry(Constants.ERROR, Constants.HTTP_ERROR, "Tried to create namespace \""+namespace+"\" got Code " + response.status)
			return -1
		}
	}
	
	def makeRequest(url, mode, headers = [], body = "")
	{
		return utils.httpRequestWithPlugin(url, mode, headers , body )
	}
	
	def getLog()
	{
		return log
	}
	
	def getPortusUserName()
	{
		return PortusUserName
	}
}
