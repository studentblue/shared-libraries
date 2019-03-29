def call( environment, currentBuild, parameter )
{
	
	Constants = new at.pii.jenkins_cpsiot_2018.sandbox.Constants()
	BuildArrowHeadServerStackHelpers = new at.pii.jenkins_cpsiot_2018.sandbox.buildArrowHeadServerStackHelpers2()
	JenkinsApi = new at.pii.jenkins_cpsiot_2018.sandbox.JenkinsApi()
	PortusApi = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi()

	pipeline
	{
		agent any
		stages
		{
		
			stage("init")
			{
				steps
				{
					script
					{
						withFolderProperties
						{
							withCredentials([usernamePassword(credentialsId: environment.PORTUS_USER_TOKEN_API, usernameVariable: 'USER', passwordVariable: 'TOKEN2')])
							{
								PortusApi.init(environment.REPO_URL, environment.PORTUS_USER, environment.TOKEN2, environment.PORTUS_USER_ID, Constants)
								
								if( PortusApi.getLog().errorsOccured() )
								{
									error("Failed")
								}
								
								JenkinsApi.init(currentBuild, environment, Constants)
								
								if( JenkinsApi.getLog().errorsOccured() )
								{
									error("Failed")
								}
							
								BuildArrowHeadServerStackHelpers.init(parameter, Constants, environment, PortusApi, JenkinsApi)
								
								if( BuildArrowHeadServerStackHelpers.getLog().errorsOccured() )
								{
									error("Failed")
								}
							}
						
							//println BuildArrowHeadServerStackHelpers.getParameter()
						}
					}
				}
			}
			
			stage("Create Maven Cache")
			{
				agent{ label "master"}
				steps
				{
					sh " docker volume create maven-repo "
				}
			}
			
			

			stage( "Build" )
			{
				agent{ label "master"}
				
				steps
				{
					script
					{
						if( BuildArrowHeadServerStackHelpers.nothingToDo() )
							exit 0
					}
					checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: "${BuildArrowHeadServerStackHelpers.getArrowHeadRepo()}"]]]
					
					
					script
					{
						if( BuildArrowHeadServerStackHelpers.checkArrowServiceImageExists() )
						{
							if( ! BuildArrowHeadServerStackHelpers.checkCompileDockerHub() )
							{
								withFolderProperties
								{							
									docker.withRegistry("${environment.REPO_URL}", "${environment.PORTUS_CREDS_STD}")
									{
										withDockerContainer(args: "${BuildArrowHeadServerStackHelpers.getContainerCompileArgs()}", image: "${BuildArrowHeadServerStackHelpers.getDockerCompileImage()}")
										{
											sh "${BuildArrowHeadServerStackHelpers.getCompileCommand()}"
										}
									}
								}
							}
							else
							{
								withDockerContainer(args: "${BuildArrowHeadServerStackHelpers.getContainerCompileArgs()}", image: "${BuildArrowHeadServerStackHelpers.getDockerCompileImage()}")
								{
									sh "${BuildArrowHeadServerStackHelpers.getCompileCommand()}"
								}
							}
						}
					}
					
					script
					{
						BuildArrowHeadServerStackHelpers.getImages().each
						{
							image ->
							
								if( !BuildArrowHeadServerStackHelpers.isDB(image ) )
								{
									stash name: "artifacts-${BuildArrowHeadServerStackHelpers.getImageName(image)}", includes: "${BuildArrowHeadServerStackHelpers.getArtifactsPath(image)}**"
								}								
						}
					}
				}
			}


			stage( "Dockerize Selected Images" )
			{
				steps
				{
					script
					{
						if( BuildArrowHeadServerStackHelpers.nothingToDo() )
							exit 0
					}
					
					script
					{
						withFolderProperties
						{
							BuildArrowHeadServerStackHelpers.getImages().each
							{
								image ->
								
									if( BuildArrowHeadServerStackHelpers.isDB(image ) )
									{
										//generate dockerfile
										
										def portusImageName = BuildArrowHeadServerStackHelpers.getPortusImageName(image)
										
										def portusTag = BuildArrowHeadServerStackHelpers.getPortusTag(image)
										
										if( BuildArrowHeadServerStackHelpers.getLog().errorsOccured() )
										{
											error("Failed")
										}
										
										println( portusImageName + ":" + portusTag )
										

										dir( "database_cpsiot" )
										{
											writeFile file: 'Dockerfile', text: BuildArrowHeadServerStackHelpers.generateDockerFileDB(image)
											docker.withRegistry("${environment.REPO_URL}", "${environment.PORTUS_CREDS_STD}")
											{
												customImage = docker.build(portusImageName)
												customImage.push(portusTag)
											}
										}
									}
									else
									{										
										unstash "artifacts-${BuildArrowHeadServerStackHelpers.getImageName(image)}"
										
										dir( "${BuildArrowHeadServerStackHelpers.getArtifactsPath(image)}/.." )
										{												
											writeFile file: 'Dockerfile', text: BuildArrowHeadServerStackHelpers.generateDockerFileArrowHeadService(image)
																							
											def portusImageName = BuildArrowHeadServerStackHelpers.getPortusImageName(image)
									
											def portusTag = BuildArrowHeadServerStackHelpers.getPortusTag(image)
											
											if( BuildArrowHeadServerStackHelpers.getLog().errorsOccured() )
											{
												error("Failed")
											}
											
											
											docker.withRegistry("${environment.REPO_URL}", "${environment.PORTUS_CREDS_STD}")
											{
												customImage = docker.build(portusImageName)
												customImage.push(portusTag)
											}
										}
										
									}
									
									if( BuildArrowHeadServerStackHelpers.getLog().errorsOccured() )
									{
										error("Failed")
									}
							}
						}
					}
				}
			}
		}
		
		post
		{
			always
			{
				echo 'Runs always'
				script
				{
					if( BuildArrowHeadServerStackHelpers.getLog() )
						println BuildArrowHeadServerStackHelpers.getLog().printLog()
					
					if( PortusApi.getLog() ) 
						println PortusApi.getLog().printLog()
				}
			}
	
			success
			{
				echo 'This will run only if successful'
			}
			
			failure
			{
				echo 'This will run only if failed'
			}
			
			unstable
			{
				echo 'This will run only if the run was marked as unstable'
			}
			
			changed
			{
				echo 'This will run only if the state of the Pipeline has changed'
				echo 'For example, the Pipeline was previously failing but is now successful'
			}
		}
	}
}
