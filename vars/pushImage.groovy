def call( Map input = ["DockerHubImage": "", "imageName" : "", "portusCredentials" : "portus-user-creds"] )
{	
	echo input
	/*
	docker.withRegistry("https://${env.DOCKER_REPO}", "${input.portusCredentials}")
	{
		//customImage = docker.build("${env.DOCKER_USER_NAMESPACE}/${MY_SQL_SERVER_IMAGE_NAME}-${params.Build}-${params.Architecture}", "$WORKSPACE/database_scripts")
		//customImage.push("latest")
		//customImage.push("${env.BUILD_NUMBER}")
		// sh " cd database_scripts && cat Dockerfile"
		echo "Push ${input.imageName}"
	}
	*/
}
	
//println DockerHub.getLog().printLog()
