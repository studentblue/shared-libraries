package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*

class DockerHubData
{
	def private outer
	def private manifests
	def private repo
	def private tag
	def private log 
	def private utils 
	
	def private final LOG
	def private final ERROR
	
	def private final ACTION_LOG_START
	def private final ACTION_CHECK
	def private final ACTION_EXCEPTION
	
	
	
	DockerHubData(outerClass, repo, tag, log, utils)
	{		
		this.outer = outerClass
		
		this.log = log
		this.utils = utils
		
		this.LOG = this.outer.constants.LOG
		this.ERROR = this.outer.constants.ERROR
		this.ACTION_LOG_START = this.outer.constants.ACTION_LOG_START
		this.ACTION_CHECK = this.outer.constants.ACTION_CHECK
		this.ACTION_EXCEPTION = this.outer.constants.ACTION_EXCEPTION
		this.repo = repo
		this.tag = tag
		
		this.log = new at.pii.jenkins_cpsiot_2018.sandbox.Log()
		
		this.log.addEntry(this.LOG, this.ACTION_LOG_START, "DockerHub init" )
		
		if( ! this.repo )
			this.log.addEntry(this.ERROR, this.ACTION_CHECK, "DockerHub repo init failed" )
	}
	
	def getManifests()
	{
		def image = this.repo
		def resolve = this.repo.split(':')
		
		def tag = "latest"
		
		if( resolve.length == 1 )
		{
			if( ! image.contains("/") )
				image = "library/" + image
			
			if( input.DockerHub.tag )
				tag = input.DockerHub.tag
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
				log.addEntry(this.ERROR, this.ACTION_CHECK, "DockerHub repo not valid" )
		}
	
		def login_template = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:${image}:pull"
		def get_manifest_template = "https://registry.hub.docker.com/v2/${image}/manifests/${tag}"
		def accept_types = "application/vnd.docker.distribution.manifest.list.v2+json,application/vnd.docker.distribution.manifestv2+json"
		
		try
		{
		
			def response = this.utils.httpRequestWithPlugin(login_template, this.outer.constants.HTTP_MODE_GET)
			def responseGroovy = ""
		
		
			if( response.status == this.outer.constants.HTTP_RESPONSE_OK )
			{
				responseGroovy =  new JsonSlurperClassic().parseText(response.content)
				
				def dockerHubToken = responseGroovy["token"]
				def headers = [[name: "Authorization", value: "Bearer ${dockerHubToken}"], [name: "accept", value: accept_types]]
				
				response = this.utils.httpRequestWithPlugin(get_manifest_template, this.outer.constants.HTTP_MODE_GET, headers)
				
				if( response.status == this.outer.constants.HTTP_RESPONSE_OK )
				{
					responseGroovy =  new JsonSlurperClassic().parseText(response.content)
					this.manifests = responseGroovy
				}
					
			}
		}
		catch(Exception e)
		{
			log.addEntry(this.ERROR, this.ACTION_EXCEPTION, e.message() )
		}
	}
	
	def getLog()
	{
		return this.log
	}
}


def Data

def init(name, tag)
{
	def log = new at.pii.jenkins_cpsiot_2018.sandbox.Log()
	def utils = new at.pii.jenkins_cpsiot_2018.sandbox.Utils()
	
	Data = new DockerHubData(this, name, tag, log, utils)
	Data.getManifests()
}

//~ def print()
//~ {
	//~ println "Test"
//~ }

def getLog()
{
	return Data.getLog()
}

return this
