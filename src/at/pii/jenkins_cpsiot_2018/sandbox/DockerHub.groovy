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
			fetchManifests()
		
		return true
	}

	def fetchManifests()
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
				log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "DockerHub repo not valid: " + image + "/" + tag )
				return false
			}
		}
		
		manifests = utils.getDockerManifests(image, tag)
			
		if( manifests )
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Manifests Fetched from DockerHub for " + image + ":" + tag )
		else
			log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Failed to fetch Manifests for "  + image + ":" + tag)
		
		return true
	}
	
	def getManifests()
	{
		return manifests
	}
	
	def getLog()
	{
		return log
	}
	
	def getDigestFromString(userChoice)
	{
	
		def digest = ""
		
		if( ! userChoice )
			return digest
		
		if( ! manifests["manifests"] )
			return digest
	
		def values = userChoice.split(Constants.SPLITTER)
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

}
