package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*

class DockerHub
{
	def private manifests
	def private repoInput
	def private tagInput
	def private log
	def private utils
	
	def Constants	
	
	def init(inputJson, Constants)
	{
		def input = new JsonSlurperClassic().parseText(inputJson)
		this.Constants = Constants		
		
		if( input.DockerHub.repo )
			repoInput = input.DockerHub.repo
		
		if( input.DockerHub.tag )
			tagInput = input.DockerHub.tag
		
		log = new Log()
		
		utils = new Utils()
		
		log.init(Constants)
		
		log.addEntry(Constants.LOG, Constants.ACTION_LOG_START, "DockerHub init" )
		
		if( ! repoInput )
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "DockerHub repo init failed" )
		else
			getManifests()
		
		return true
	}

	def getManifests()
	{
		def image = repoInput
		def resolve = repoInput.split(':')
		
		def tag = "latest"
		
		if( resolve.length == 1 )
		{
			if( ! image.contains("/") )
				image = "library/" + image
			
			if( tagInput )
				tag = tagInput
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
				log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "DockerHub repo not valid" )
		}
	
		def login_template = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:${image}:pull"
		def get_manifest_template = "https://registry.hub.docker.com/v2/${image}/manifests/${tag}"
		def accept_types = "application/vnd.docker.distribution.manifest.list.v2+json,application/vnd.docker.distribution.manifestv2+json"
		
		
		def headers = [[name: "Authorization", value: "test"], [name: "accept", value: accept_types]]
		utils.httpRequestWithPlugin("https://docker-registry-cpsiot-2018.pii.at/api/v1/_ping", Constants.HTTP_MODE_GET, headers)
		
		
		def test = "test"
		headers = [[name: "Authorization", value: "${test}"], [name: "accept", value: accept_types]]
		
		utils.httpRequestWithPlugin("https://docker-registry-cpsiot-2018.pii.at/api/v1/_ping", Constants.HTTP_MODE_GET, headers)
		//~ try
		//~ {
			//~ def utils = new Utils()
			//~ def response = utils.httpRequestWithPlugin(login_template, Constants.HTTP_MODE_GET)
			//~ def responseGroovy = ""
		
		
			//~ if( response.status == Constants.HTTP_RESPONSE_OK )
			//~ {
				//~ responseGroovy =  new JsonSlurperClassic().parseText(response.content)
				
				//~ def dockerHubToken = responseGroovy["token"]
				//~ def headers = [[name: "Authorization", value: "Bearer ${dockerHubToken}"], [name: "accept", value: accept_types]]
				
				//~ response = utils.httpRequestWithPlugin(get_manifest_template, Constants.HTTP_MODE_GET, headers)
				
				//~ if( response.status == Constants.HTTP_RESPONSE_OK )
				//~ {
					//~ responseGroovy =  new JsonSlurperClassic().parseText(response.content)
					//~ manifests = responseGroovy
					
					//~ if( this.manifests )
						//~ log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Manifests Fetched from DockerHub for " + image + ":" + tag )
					//~ else
						//~ log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Failed to fetch Manifests for "  + image + ":" + tag)
				//~ }
				
			//~ }
			
			//~ return true
		//~ }
		//~ catch(Exception e)
		//~ {
			//~ log.addEntry(Constants.ERROR, Constants.ACTION_EXCEPTION, e.getMessage() )
		//~ }
	}
	
	def getLog()
	{
		return log
	}

}
