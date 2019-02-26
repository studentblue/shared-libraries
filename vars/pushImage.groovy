def call( Map input )
{	
	
	
	//sh "docker pull ${input.DockerHubImage} "
	
	echo "${input.DockerHubImage}"
	echo "${input.imageName.portusRepo}/${input.imageName.image}:${input.imageName.tag}"
	/*
	docker.withRegistry("${input.portusRepo}", "${input.portusCredentials}")
	{
		
		
		sh "docker tag ${input.DockerHubImage} ${input.imageName.portusRepo}/${input.imageName.image}:${input.imageName.tag}"
		sh "docker push ${input.imageName.portusRepo}/${input.imageName.image}:${input.imageName.tag}"
		
	}
	
	sh "docker rmi ${input.DockerHubImage} "
	sh "docker rmi ${input.imageName.portusRepo}/${input.imageName.image}:${input.imageName.tag} "
	*/
}
	
//println DockerHub.getLog().printLog()
