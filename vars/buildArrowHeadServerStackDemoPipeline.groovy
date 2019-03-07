def call( environment, currentBuild, parameter )
{
	
	Constants = new at.pii.jenkins_cpsiot_2018.sandbox.Constants()
	BuildArrowHeadServerStackHelpers = new at.pii.jenkins_cpsiot_2018.sandbox.buildArrowHeadServerStackHelpers()
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
							withCredentials([string(credentialsId: env.PORTUS_USER_TOKEN_API, variable: 'TOKEN2')])
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
					
					/*
					stash name: "auth-artifacts", includes: "authorization/target/**"
					stash name: "serv-artifacts", includes: "serviceregistry_sql/target/**"
					stash name: "event-artifacts", includes: "eventhandler/target/**"
					stash name: "keeper-artifacts", includes: "gatekeeper/target/**"
					stash name: "way-artifacts", includes: "gateway/target/**"
					stash name: "orch-artifacts", includes: "orchestrator/target/**"
					*/
					
					script
					{
						BuildArrowHeadServerStackHelpers.getImages().each
						{
							image ->
								if( BuildArrowHeadServerStackHelpers.isDB(image ) )
								{
									if( BuildArrowHeadServerStackHelpers.checkScriptPathDBScript(image) )
									{
										stash name: "scriptPath-${BuildArrowHeadServerStackHelpers.getImageName(image)}", includes: "${BuildArrowHeadServerStackHelpers.getDBScriptPath(image)}"
									}
								}
								else
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
						withFolderProperties
						{
							BuildArrowHeadServerStackHelpers.getImages().each
							{
								image ->
								
									if( BuildArrowHeadServerStackHelpers.isDB(image ) )
									{
										//generate dockerfile
										withCredentials(
										[
											string(credentialsId: env.DB_Root_PWD, variable: 'DB_ROOT_PWD'),
											usernamePassword(credentialsId: env.ArrowHead_User_Pwd, usernameVariable: 'DEFAULT_DB_ARROWHEAD_USR', passwordVariable: 'DEFAULT_DB_ARROWHEAD_PSW')
										])
										{
											def DB_ARROWHEAD = BuildArrowHeadServerStackHelpers.getArrowheadDB()
											def DB_ARROWHEAD_LOG = BuildArrowHeadServerStackHelpers.getArrowheadDBLog()
											if( BuildArrowHeadServerStackHelpers.checkGenerateDBScript(image) )
											{
												sh "rm -rf database_scripts_cpsiot"
												sh "mkdir database_scripts_cpsiot"
											
												dir( "database_scripts_cpsiot" )
												{
													writeFile file: 'initDB.sql', text: BuildArrowHeadServerStackHelpers.generateDBScript(DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW, DB_ARROWHEAD, DB_ARROWHEAD_LOG)
													
													writeFile file: 'Dockerfile', text: BuildArrowHeadServerStackHelpers.generateDockerFileDB(image, 'initDB.sql', DB_ROOT_PWD)
													
												}
											}
											else
											{
												if( BuildArrowHeadServerStackHelpers.checkInputDBScript(image) )
												{
													sh "rm -rf database_scripts_cpsiot"
													sh "mkdir database_scripts_cpsiot"
												
													dir( "database_scripts_cpsiot" )
													{
														writeFile file: 'initDB.sql', text: BuildArrowHeadServerStackHelpers.getDBScript(image)
														
														writeFile file: 'Dockerfile', text: BuildArrowHeadServerStackHelpers.generateDockerFileDB(image, 'initDB.sql', DB_ROOT_PWD)
														
													}
												}
												if( BuildArrowHeadServerStackHelpers.checkScriptPathDBScript(image) )
												{
													sh "rm -rf database_scripts_cpsiot"
													sh "mkdir database_scripts_cpsiot"
													
													unstash "scriptPath-${BuildArrowHeadServerStackHelpers.getImageName(image)}"
													

													sh "cp ${BuildArrowHeadServerStackHelpers.getDBScriptPath(image)} database_scripts_cpsiot/initDB.sql "
													dir( "database_scripts_cpsiot" )
													{
														writeFile file: 'Dockerfile', text: BuildArrowHeadServerStackHelpers.generateDockerFileDB(image, 'initDB.sql', DB_ROOT_PWD)
														
													}
												}
												
											}
										}
										
										def portusImageName = BuildArrowHeadServerStackHelpers.getPortusImageName(image)
										
										def portusTag = BuildArrowHeadServerStackHelpers.getPortusTag(image)
										
										if( BuildArrowHeadServerStackHelpers.getLog().errorsOccured() )
										{
											error("Failed")
										}
										
										println( portusImageName + ":" + portusTag )
										

										dir( "database_scripts_cpsiot" )
										{
											docker.withRegistry("${environment.REPO_URL}", "${environment.PORTUS_CREDS_STD}")
											{
												customImage = docker.build(portusImageName)
												customImage.push(portusTag)
											}
										}
									}
									else
									{
										withCredentials(
										[
											string(credentialsId: env.DB_Root_PWD, variable: 'DB_ROOT_PWD'),
											usernamePassword(credentialsId: env.ArrowHead_User_Pwd, usernameVariable: 'DEFAULT_DB_ARROWHEAD_USR', passwordVariable: 'DEFAULT_DB_ARROWHEAD_PSW')
										])
										{
											unstash "artifacts-${BuildArrowHeadServerStackHelpers.getImageName(image)}"
											
											dir( "${BuildArrowHeadServerStackHelpers.getArtifactsPath(image)}/.." )
											{
												if( BuildArrowHeadServerStackHelpers.isArrowHead3() )
												{
													writeFile file: 'target/config/app.properties', text: BuildArrowHeadServerStackHelpers.generateAppProperties(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
													writeFile file: 'target/config/log4j.properties', text: BuildArrowHeadServerStackHelpers.generateLogProperties(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
													
												}
												
												if( BuildArrowHeadServerStackHelpers.isArrowHead4() )
												{
													writeFile file: 'target/default.conf', text: BuildArrowHeadServerStackHelpers.generateAppProperties(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
													writeFile file: 'target/config/log4j.properties', text: BuildArrowHeadServerStackHelpers.generateLogProperties(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
												}
												
												writeFile file: 'Dockerfile', text: BuildArrowHeadServerStackHelpers.generateDockerFileArrowHeadService(image)
												
												//sh "cat target/config/app.properties"
												//sh "cat target/config/log4j.properties"
												//sh "cat Dockerfile"
												
												def portusImageName = BuildArrowHeadServerStackHelpers.getPortusImageName(image)
										
												def portusTag = BuildArrowHeadServerStackHelpers.getPortusTag(image)
												
												if( BuildArrowHeadServerStackHelpers.getLog().errorsOccured() )
												{
													error("Failed")
												}
												
												println( portusImageName + ":" + portusTag )
												
												docker.withRegistry("${environment.REPO_URL}", "${environment.PORTUS_CREDS_STD}")
												{
													customImage = docker.build(portusImageName)
													customImage.push(portusTag)
												}
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
