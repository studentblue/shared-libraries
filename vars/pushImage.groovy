def call( Map input )
{	
	//echo "${input.portusCredentials}"
	
	

	docker.withRegistry("${input.portusRepo}", "${input.portusCredentials}")
	{
		//customImage = docker.build("${env.DOCKER_USER_NAMESPACE}/${MY_SQL_SERVER_IMAGE_NAME}-${params.Build}-${params.Architecture}", "$WORKSPACE/database_scripts")
		//customImage.push("latest")
		//customImage.push("${env.BUILD_NUMBER}")
		// sh " cd database_scripts && cat Dockerfile"
		echo "Push ${input.imageName}"
	}
}
	
//println DockerHub.getLog().printLog()