package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.JsonSlurperClassic


def getManifestsForImage(repo, tagArg)
{
	def emptyMap = [:]
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
	
	//println "${image} ${tag} "
	

	def login_template = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:${image}:pull"
	def get_manifest_template = "https://registry.hub.docker.com/v2/${image}/manifests/${tag}"
	def accept_types = "application/vnd.docker.distribution.manifest.list.v2+json,application/vnd.docker.distribution.manifestv2+json"					
	
	def get = new URL(login_template).openConnection();
	def getRC = get.getResponseCode();
	
	def token = ""
	
	if(getRC.equals(200))
	{
		def response = new JsonSlurperClassic().parseText(get.getInputStream().getText())
		token = response["token"]
		
		println response.getClass()
	}
	else
		return test

	def get2 = new URL(get_manifest_template).openConnection();
	get2.setRequestProperty("Content-Type", "application/json")
	get2.setRequestProperty("Authorization", "Bearer ${token}")
	get2.setRequestProperty("accept", accept_types)
	
	
	def getRC2 = get.getResponseCode();
	
	if(getRC2.equals(200))
	{
		def response2 = new JsonSlurperClassic().parseText(get2.getInputStream().getText());
		
		for( e in response2 )
		{
			if( e.key.equals("manifests") )
			{
				def digest = e["digest"]
				for( manifests in e.value )
				{
					
					emptyMap.put(digest, manifests)
					
					if( manifests.keySet().contains("platform") )
					{
						def keys = manifests["platform"].keySet()
						
						if( keys.contains("os") )
							println manifests["platform"]["os"]
						
						if( keys.contains("architecture") )
							println manifests["platform"]["architecture"]
						
						if( keys.contains("os.version") )
							println manifests["platform"]["os.version"]
					} 
				}
			}
		}
	}
	else
	{
		println "Error ${getRC2}"
		return test
	}
	
	return emptyMap
}

return this

