package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*

def httpRequestWithPlugin(url, mode, headers = [], body = "")
{
	def Constants = new Constants()
	
	def response = httpRequest httpMode: mode, url: url, acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', customHeaders: headers, requestBody: body
	
	if( mode.equals(Constants.HTTP_MODE_GET) && response.status == Constants.HTTP_RESPONSE_OK )
		return response
	
	if( mode.equals(Constants.HTTP_MODE_POST) && response.status == Constants.HTTP_RESPONSE_CREATED )
		return response


	return false
}

def getDockerManifests(repo, tag)
{
	def Constants = new Constants()
	
	def login_template = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:"+repo+":pull"
	def get_manifest_template = "https://registry.hub.docker.com/v2/"+repo+"/manifests/"+tag
	def accept_types = "application/vnd.docker.distribution.manifest.list.v2+json,application/vnd.docker.distribution.manifestv2+json"
	
	
	
	def mode = Constants.HTTP_MODE_GET
	

	def response = httpRequest httpMode: mode, url: login_template, acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON'
	def responseGroovy = ""


	if( response.status == Constants.HTTP_RESPONSE_OK )
	{
		responseGroovy =  new JsonSlurperClassic().parseText(response.content)
		
		def dockerHubToken = responseGroovy["token"]
		def headers = [[name: "Authorization", value: "Bearer "+dockerHubToken], [name: "accept", value: accept_types]]
		//~ response = utils.httpRequestWithPlugin(get_manifest_template, Constants.HTTP_MODE_GET, headers)
		response = httpRequest httpMode: mode, url: url, acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', customHeaders: headers, requestBody: body
		
		if( response.status == Constants.HTTP_RESPONSE_OK )
		{
			responseGroovy =  new JsonSlurperClassic().parseText(response.content)
			return responseGroovy
		}
		
		return false
		
}

return this
