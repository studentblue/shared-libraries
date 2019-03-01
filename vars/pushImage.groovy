def call( Map input )
{	
	
	
	//sh "docker pull ${input.DockerHubImage} "
	
	echo "${input.DockerHubImage}"
	echo "${input.portus.image.name}:${input.portus.repo.tag}"

	docker.withRegistry("${input.portusRepo}", "${input.portusCredentials}")
	{
		
		
		sh "docker tag ${input.DockerHubImage} "${input.portus.image.name}:${input.portus.repo.tag}""
		sh "docker push "${input.portus.image.name}:${input.portus.repo.tag}""
		
	}
	
	sh "docker rmi ${input.DockerHubImage} "
	sh "docker rmi "${input.portus.image.name}:${input.portus.repo.tag}"

}
	
//println DockerHub.getLog().printLog()
