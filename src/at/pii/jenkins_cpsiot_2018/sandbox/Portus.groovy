package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.JsonSlurperClassic

splitter = " @::@ "

def getDigestFromString(manifests, input)
{
	def values = input.split(splitter)
	def pattern = values[1].trim()
	
	def digest = ""
	manifests.each
	{
		manifest ->
			// "digest": "sha256:3be17715f14ac6f0834554ab4fc7a7440449690e58d45291dfae420c8d3422f1",
			def values2 = manifest["digest"].split(':')
			
			
			def match = values2[1].substring(0,10).trim()
			if( match.equals(pattern) )
			{
				digest = manifest["digest"]
				return true 
			}
	}
	
	return digest
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
			return test
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
		return test
	
	def dockerHubToken = response2Groovy["token"]
	
	def headers = [[name: "Authorization", value: "Bearer ${dockerHubToken}"], [name: "accept", value: accept_types]]
	
	def response3 = httpRequest httpMode: 'GET', url: get_manifest_template, contentType: 'APPLICATION_JSON', customHeaders: headers
	def response3Groovy = ""
	
	if( response3.status == 200 )
	{
		response3Groovy =  new JsonSlurperClassic().parseText(response3.content)
		return response3Groovy
	}
	
	return test
	
}

def generateManifestChoices(manifests)
{
	def choices = []
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
			
			manifestDesc += splitter + values[1].substring(0,10)
			
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
		return 1
}

return this

