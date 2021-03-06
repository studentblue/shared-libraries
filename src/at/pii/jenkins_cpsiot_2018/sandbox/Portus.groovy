package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
//import groovy.json.JsonSlurperClassic

def getDigestFromString(manifests, input)
{

	def digest = ""
	
	if( ! input )
		return digest
	
	if( ! manifests["manifests"] )
		return digest

	def values = input.split(constants.SPLITTER)
	def pattern = values[1].trim()
	
	manifests["manifests"].each
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

def getPlatformFromDigest(manifests, digest)
{
	
	def platform = []
	
	manifests["manifests"].each
	{
		manifest ->
			
			if( manifest["digest"].equals(digest) )
			{
				platform = manifest["platform"]
				return true 
			}
	}
	
	return platform
}

def getID(url, username, password, match, health = false, tags = false)
{
	def get = new URL(url).openConnection();

	get.setRequestProperty("Accept", "application/json")
	get.setRequestProperty("Portus-Auth", "${username}:${password}")
	
	def responseCode = get.getResponseCode();
	
	if (responseCode == 200) 
	{
		if( health )
			return true
		
		def response = new JsonSlurperClassic().parseText(get.getInputStream().getText());
	    
	    for(item in response)
	    {
			if( tags )
			{
				artifacts.add(item.name)
				
			}
			else
			{
				if( match.equals(item.name ))
				{
					return item.id
				}
			}
		}
		
		if( tags )
			return true
	}

	return false
	
}

def getManifestsFromDockhub(repo, tagArg)
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

def generateManifestChoices(manifests)
{
	def choices = []
	
	if( ! manifests["manifests"] )
		return choices
	
	manifests["manifests"].each
	{
		manifest ->
			
			def manifestDesc = ""
			
			manifest["platform"].keySet().each
			{
				detail ->
					manifestDesc += detail + ": " + manifest["platform"][detail] + ", "
			}
			
			def values = manifest["digest"].split(':')
			
			manifestDesc += constants.SPLITTER + values[1].substring(0,10)
			
			choices.add(manifestDesc)
	}
	return choices
}

def isPortusHealthy(repo_url, portus_user, token)
{
	health_api = "/api/v1/health"
	
	def portusAuthToken = portus_user + ":" + token
	def headers = [[name: "Portus-Auth", value: portusAuthToken]]
	
	def response = httpRequest httpMode: 'GET', url: "${repo_url}${health_api}", customHeaders: headers
	
	if( response.status == 200 )
	{
		return true
	}
	else
		return false
}

def getPortusNamespaces(repo_url, portus_user, token)
{
	def portus_api = "/api/v1/namespaces?all=true"
	
	def portusAuthToken = portus_user + ":" + token
	def headers = [[name: "Portus-Auth", value: portusAuthToken]]
	
	def response = httpRequest httpMode: 'GET', url: "${repo_url}${portus_api}", acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', customHeaders: headers
	
	if( response.status == 200 )
	{
		responseGroovy =  new JsonSlurperClassic().parseText(response.content)
		return responseGroovy
	}
	else
		return constants.ERROR
}

def checkNameSpaceExistsForTeam(repo_url, portus_user, token, name, teamID)
{
	def nameSpaces = getPortusNamespaces(repo_url, portus_user, token)
	
	def found = false
	
	if( ! nameSpaces )
		return found
	else
	{
		nameSpaces.each
		{
			nameSpace ->
				
				if( nameSpace["name"].trim().equals(name) && nameSpace["team"]["id"] == teamID )
				{
					found = true
					return true
				}
		}
	}
	
	
	return found
}

def getTeamID(team)
{
	def values = team.split(constants.SPLITTER)
	def value = values[1].trim() as int
	return value
}

def generateDefaultImageName(name)
{
	def image = name
	def resolve = image.split(':')
	
	if( resolve.length == 1 )
	{
		return constants.DEFAULT_IMAGE_PREFIX + image.replaceAll("[\\W]", '_')
	}
	else
	{
		return constants.DEFAULT_IMAGE_PREFIX + resolve[0].replaceAll("[\\W]", '_')
	}
}

def generateDefaultNameSpace(manifests, digest)
{	
	
	if( ! digest )
		return constants.DEFAULT_NAMESPACE_PREFIX + constants.UNKNOWN_ARCH_OS
	
	//DEFAULT_NAMESPACE_PREFIX
	defaultNameSpace = constants.DEFAULT_NAMESPACE_PREFIX
	def platform = getPlatformFromDigest(manifests, digest)
	
	list = []
	platform.keySet().each
	{
		key ->
			list.add(platform[key])
	}
	
	return defaultNameSpace + list.join('_')
	
}

def sanitizeNameSpace(NameSpace)
{
	return NameSpace.replaceAll("[\\W]", '_')
}

def sanitizeImageName(Image_Name)
{
	return Image_Name.replaceAll("[^\\w-_]", '_')
}

def createNameSpaceForTeam(repo_url, portus_user, token, name, teamID, teamDescription)
{
	def portus_api = "/api/v1/namespaces"
	
	def portusAuthToken = portus_user + ":" + token
	def headers = [[name: "Portus-Auth", value: portusAuthToken]]
	
	def jsonForm = ""
	
	def teamName = ""
	
	def teamFull = getTeamWithID(repo_url, portus_user, token, teamID )
	
	teamName = teamFull["name"]
	
	if( teamDescription )
		jsonForm = JsonOutput.toJson([name: name, team: teamName, description: teamDescription])
	else
		jsonForm = JsonOutput.toJson([name: name, team: teamName])
	
	def response = httpRequest httpMode: 'POST', url: "${repo_url}${portus_api}", acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', customHeaders: headers, requestBody: jsonForm
	
	if( response.status == 200 )
	{
		responseGroovy =  new JsonSlurperClassic().parseText(response.content)
		return responseGroovy
	}
	else
		return constants.ERROR
}

def getTeamWithID(repo_url, portus_user, token, teamID )
{
	def portus_api = "/api/v1/teams/" + teamID
	
	def portusAuthToken = portus_user + ":" + token
	def headers = [[name: "Portus-Auth", value: portusAuthToken]]
	
	def response = httpRequest httpMode: 'GET', url: "${repo_url}${portus_api}", acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', customHeaders: headers
	
	if( response.status == 200 )
	{
		responseGroovy =  new JsonSlurperClassic().parseText(response.content)
		return responseGroovy
	}
	else
		return constants.ERROR
}

return this
