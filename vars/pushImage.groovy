def call( Map input )
{	
	//echo "${input.portusCredentials}"
	
	//pull image docker hub
	
	sh "docker pull ${input.DockerHubImage} "
	
	//echo "${input.DockerHubImage}"

	docker.withRegistry("${input.portusRepo}", "${input.portusCredentials}")
	{
		//customImage = docker.build("${env.DOCKER_USER_NAMESPACE}/${MY_SQL_SERVER_IMAGE_NAME}-${params.Build}-${params.Architecture}", "$WORKSPACE/database_scripts")
		//customImage.push("latest")
		//customImage.push("${env.BUILD_NUMBER}")
		// sh " cd database_scripts && cat Dockerfile"
		//echo "Push ${input.imageName.image}:${input.imageName.tag}"
		
		sh "docker tag ${input.DockerHubImage} ${input.imageName.portusRepo}/${input.imageName.image}:${input.imageName.tag}"
		sh "docker push ${input.imageName.portusRepo}/${input.imageName.image}:${input.imageName.tag}"
		//echo "${input.imageName.portusRepo}/${input.imageName.image}:${input.imageName.tag}"
	}
	
	sh "docker rmi ${input.DockerHubImage} "
	sh "docker rmi ${input.imageName.portusRepo}/${input.imageName.image}:${input.imageName.tag} "
}
	
//println DockerHub.getLog().printLog()
