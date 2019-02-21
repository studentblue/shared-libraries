package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*

class DockerHub implements Serializable
{
	def private manifests
	def private repoInput
	def private tagInput
	def private log 
	def private utils 
	
	def private final LOG
	def private final ERROR
	
	def private final ACTION_LOG_START
	def private final ACTION_CHECK
	def private final ACTION_EXCEPTION
	
	def ConstantsRef
	
	DockerHub(repo, tag, ConstantsRef)
	{
		this.ConstantsRef = ConstantsRef
		
		if( repo )
			repoInput = repo
		
		if( tag )
			tagInput = tag
		
		//~ log = new Log(ConstantsRef)
		//~ utils = new Utils()
		
		//~ log.addEntry(ConstantsRef.LOG, ConstantsRef.ACTION_LOG_START, "DockerHub init" )
		
		//~ if( ! repoInput )
			//~ log.addEntry(ConstantsRef.ERROR, ConstantsRef.ACTION_CHECK, "DockerHub repo init failed" )
	}
	/*
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
		
		try
		{
		
			def response = utils.httpRequestWithPlugin(login_template, Constants.HTTP_MODE_GET)
			def responseGroovy = ""
		
		
			if( response.status == Constants.HTTP_RESPONSE_OK )
			{
				responseGroovy =  new JsonSlurperClassic().parseText(response.content)
				
				def dockerHubToken = responseGroovy["token"]
				def headers = [[name: "Authorization", value: "Bearer ${dockerHubToken}"], [name: "accept", value: accept_types]]
				
				response = utils.httpRequestWithPlugin(get_manifest_template, Constants.HTTP_MODE_GET, headers)
				
				if( response.status == Constants.HTTP_RESPONSE_OK )
				{
					responseGroovy =  new JsonSlurperClassic().parseText(response.content)
					this.manifests = responseGroovy
				}
					
			}
		}
		catch(Exception e)
		{
			log.addEntry(Constants.ERROR, Constants.ACTION_EXCEPTION, e.message() )
		}
	}
	
	def getLog()
	{
		return log
	}
	*/
}
