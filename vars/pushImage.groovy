def call(String DockerHubImage = "", String imageName = "", String portusCredentials = "portus-user-creds" )
{	
	docker.withRegistry("https://${env.DOCKER_REPO}", "${portusCredentials}")
	{
		//customImage = docker.build("${env.DOCKER_USER_NAMESPACE}/${MY_SQL_SERVER_IMAGE_NAME}-${params.Build}-${params.Architecture}", "$WORKSPACE/database_scripts")
		//customImage.push("latest")
		//customImage.push("${env.BUILD_NUMBER}")
		// sh " cd database_scripts && cat Dockerfile"
		echo "Push ${imageName}"
	}
}
	
//println DockerHub.getLog().printLog()
