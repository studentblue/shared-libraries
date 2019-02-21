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
			{
				log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "DockerHub repo not valid" )
				return false
			}
		}
		
		manifests = utils.getDockerManifests(image, tag)
			
		if( manifests )
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Manifests Fetched from DockerHub for " + image + ":" + tag )
		else
			log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Failed to fetch Manifests for "  + image + ":" + tag)
		
		return true
	}
	
	def getLog()
	{
		return log
	}

}
