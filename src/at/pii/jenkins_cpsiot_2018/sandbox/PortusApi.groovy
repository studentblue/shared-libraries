package at.pii.jenkins_cpsiot_2018.sandbox;

import groovy.json.*;
import org.boon.Boon;

class PortusApi
{
	//Folder Properties
	def PortusUrl;
	def PortusUserName;
	def portusToken;
	def PortusUserId; 
		
	def healthApi = "/api/v1/health";
	def usersApi = "/api/v1/users";
	def pingApi = "/api/v1/_ping";
	def namespacesApi = "/api/v1/namespaces";
	def repositoryApi = "/api/v1/repositories";
	def tagsApi = "/api/v1/tags";
	def teamsApi = "/api/v1/teams";
	
	def utils;
	def Constants;
	def log;
	
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
		else
			log.addEntry(Constants.LOG, Constants.ACTION_INFO, "User: "+PortusUserName+", ID: " +  PortusUserId)
		
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
	
	def validateNamespace(namespace, team, description)
	{
		def url = PortusUrl + namespacesApi + "/validate?name=" + namespace
		def mode = Constants.HTTP_MODE_GET
		
		def response = makeRequest(url, mode, getPortusAuthHeaders())
		
		if( response.status == Constants.HTTP_RESPONSE_OK )
		{
			def content = new JsonSlurperClassic().parseText(response.content)
			log.addEntry(Constants.LOG, Constants.ACTION_VALIDATE, "Namespace: " + namespace + " = " + content.valid)
			
			if( content.valid )
			{
				return postNamespace(namespace, team, description)
			}
			else
				log.addEntry(Constants.LOG, Constants.NAMESPACE_FOUND, "Namespace: " + namespace + " not created" )
		}
		else
		{
			log.addEntry(Constants.ERROR, Constants.HTTP_ERROR, "Tried to validate namespace \""+namespace+"\" got Code " + response.status)
			return -1
		}
		
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
				log.addEntry(Constants.LOG, Constants.TEAM_FOUND, "Team: " + teamToFind + " found must not be created ")
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
		
		def response = ""
		def content = ""
		try
		{
			response = makeRequest(url, mode, getPortusAuthHeaders(), body)
			content = new JsonSlurperClassic().parseText(response.content)
			
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
		catch( Exception e )
		{
			if( response )
				log.addEntry(Constants.ERROR, Constants.HTTP_ERROR, "Tried to create namespace \""+namespace+"\" got Code " + response.status)
			
			log.addEntry(Constants.ERROR, Constants.ACTION_EXCEPTION, "Tried to create namespace \""+namespace+"\" got " + e.getMessage())
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
	
	def validateImage(image)
	{
		def code = ""
		
		if( image.checkTeam )
		{
			code = validateTeam(image.team.name, image.team.teamDescription)
			if(!code)
			{
				log.addEntry(Constants.ERROR, Constants.ACTION_TEAM_VALIDATION, "Validation of team \"" + image.team.name + "\" failed")
				return
			}
		}
		
		if( image.checkNamespace )
		{
			code = validateNamespace(image.namespace.name, image.team.name, image.namespace.description)
		
			if(!code)
			{
				log.addEntry(Constants.ERROR, Constants.ACTION_NAMESPACE_VALIDATION, "Validation of namespace \"" + image.namespace.name + "\" failed")
				return
			}
		}
		
		code = checkNamespaceRepoTag(image.namespace.name, image.repo.name, image.repo.tag )
		
		if(!code)
		{
			log.addEntry(
				Constants.ERROR, 
				Constants.ACTION_NAMESPACE_REPO_TAG_VALIDATION, 
				"Validation of " + image.namespace.name + "->" +image.repo.name+ "->" + image.repo.tag + " failed")
			return
		}
	}
	
	def checkNamespaceRepoTag(namespace, repoName, tagName )
	{
		if( validateNamespace(namespace) == -1 )
			return false
		
		if( validateNamespace(namespace) )
			return true
		else
		{
			def nameSpaceId = getNameSpaceId( namespace )
			
			if( nameSpaceId == -1 )
			{
				log.addEntry(Constants.ERROR, Constants.ACTION_EXCEPTION, "Namespace \""+namespace+"\" was valid but not found for user " + PortusUserName )
				return false
			}
			
			//get tags for repo
			def repos = getRepositoriesForNameSpace(nameSpaceId)
			
			if( ! repos )
				return true
			
			def repoId = -1
			
			repos.each
			{
				repo ->
					
					if( repo.name.equals(repoName) )
					{
						repoId = repo.id
					}
			}
			
			if( repoId == -1 )
				return true
			
			def tags = getTagsFromRepo(repoId)
			
			if( ! tags )
				return true
			
			def tagFound = false
			
			tags.each
			{
				tag ->
					
					if( tag.name.equals(tagName) )
						tagFound = true
			}
			
			if( tagFound )
				return false
			else
				return true
		}
	}
	
	def getRepositoriesForNameSpace(nameSpaceId)
	{
		
		if( ! ( nameSpaceId >= 0 ))
			return []
		
		def url = PortusUrl + namespacesApi + "/"+nameSpaceId+"/repositories"
		def mode = Constants.HTTP_MODE_GET
		
		def response = makeRequest(url, mode, getPortusAuthHeaders())
		
		if( response.status == Constants.HTTP_RESPONSE_OK )
		{
			def content = new JsonSlurperClassic().parseText(response.content)
			log.addEntry(Constants.LOG, Constants.REPOSITORIES_FETCHED, "Loaded  Repos for NameSpace ID " + nameSpaceId)
			
			return content
		}
		
		return []
	}
	
	def getRepoId(name)
	{
		def repos = getAllRepositories()
		
		def repoId = -1
		
		if( !repos)
		{
			log.addEntry(Constants.LOG, Constants.REPOSITORIES_FETCH_ID_ERROR, "Repo Name: " + name + " ID not found ")
			return -1
		}
		
		repos.each
		{
			repo ->
				
				if( repo.name.equals(name) )
					repoId = repo.id
		}
		
		return repoId
		
	}
	
	def getNameSpaceId( name )
	{
		def url = PortusUrl + namespacesApi + "?all=true"
		def mode = Constants.HTTP_MODE_GET
		
		def response = makeRequest(url, mode, getPortusAuthHeaders())
		
		def id = -1
		
		if( response.status == Constants.HTTP_RESPONSE_OK )
		{
			def content = new JsonSlurperClassic().parseText(response.content)
			log.addEntry(Constants.LOG, Constants.NAMESPACES_FETCHED, "Loaded  Namespaces for user " + PortusUserName)
			
			content.each
			{
				namespace ->
					
					if( namespace.name.equals(name) )
					{
						id = namespace.id
					}
			}
			
			if( id )
				log.addEntry(Constants.LOG, Constants.NAMESPACE_FOUND, "Namespace \""+name+"\" found ")
		}
		
		return id
		
	}
	
	def getAllRepositories()
	{
		def url = PortusUrl + repositoryApi + "?all=true"
		def mode = Constants.HTTP_MODE_GET
		
		def response = makeRequest(url, mode, getPortusAuthHeaders())
		
		if( response.status == Constants.HTTP_RESPONSE_OK )
		{
			def content = new JsonSlurperClassic().parseText(response.content)
			log.addEntry(Constants.LOG, Constants.REPOSITORIES_FETCHED, "Loaded  Repositories for user " + PortusUserName)
			
			return content
		}
		
		return []
	}
	
	def getTagsFromRepo(repoId)
	{
		def url = PortusUrl + repositoryApi + "/"+repoId+"/tags"
		def mode = Constants.HTTP_MODE_GET
		
		def response = makeRequest(url, mode, getPortusAuthHeaders())
		
		if( response.status == Constants.HTTP_RESPONSE_OK )
		{
			def content = new JsonSlurperClassic().parseText(response.content)
			log.addEntry(Constants.LOG, Constants.TAGS_FETCHED, "Loaded  Tags for Repo ID " + repoId)
			
			return content
		}
		
		return []
	}
	
	def getPortusRegistryName()
	{
		def resolve = PortusUrl.split("//")
		
		return resolve[1]
		
	}
}
