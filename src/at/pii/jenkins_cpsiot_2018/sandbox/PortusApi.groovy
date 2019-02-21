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
	
	def init(PortusUrl, PortusUserName, portusToken, Constants)
	{	
		this.PortusUrl = PortusUrl
		this.PortusUserName = PortusUserName
		this.portusToken = portusToken
		
		this.Constants = Constants
		
		log = new Log()
		log.init(Constants)
		
		utils = new Utils()
		
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "PortusApi init" )
		
		if( ! ( PortusUrl || PortusUserName || portusToken ))
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
	
	def makeRequest(url, mode, headers = [], body = "")
	{
		return utils.httpRequestWithPlugin(url, mode, headers , body )
	}
	
	def getLog()
	{
		return log
	}
}
