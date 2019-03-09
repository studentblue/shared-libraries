def call( environment, currentBuild, parameter, ArrowHeadCreds, DBRootPsw )
{
	
	Constants = new at.pii.jenkins_cpsiot_2018.sandbox.Constants()
	DeployServerStackHelpers = new at.pii.jenkins_cpsiot_2018.sandbox.DeployServerStackHelpers()
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
							
								DeployServerStackHelpers.init(parameter, Constants, environment, PortusApi, JenkinsApi)
								
								if( DeployServerStackHelpers.getLog().errorsOccured() )
								{
									error("Failed")
								}
							}
						
							//println BuildArrowHeadServerStackHelpers.getParameter()
						}
					}
				}
			}
			
			stage("Generate Init Values ")
			{
				agent{ label "${DeployServerStackHelpers.getNodeName()}" }
				steps
				{
					script
					{
					
						//init network
						
						if( DeployServerStackHelpers.initNetwork() )
							sh " docker network create -d bridge ${DeployServerStackHelpers.getCloudNetwork()} "
						
						withFolderProperties
						{
							withCredentials(
							[
								string(credentialsId: DBRootPsw, variable: 'DB_ROOT_PWD'),
								usernamePassword(credentialsId: ArrowHeadCreds, usernameVariable: 'DEFAULT_DB_ARROWHEAD_USR', passwordVariable: 'DEFAULT_DB_ARROWHEAD_PSW')
							])
							{
								DeployServerStackHelpers.getImages().each
								{
									image ->
										
										sh( script: DeployServerStackHelpers.checkImageContainer(image), wait: true)
										
										if( DeployServerStackHelpers.isDB(image ) )
										{
											
											
											dir( "database_scripts_cpsiot" )
											{
												writeFile file: 
													'initDB.sql', 
													text: DeployServerStackHelpers.generateDBScript(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
												docker.withRegistry("${environment.REPO_URL}", "${environment.PORTUS_CREDS_STD}")
												{
													def scriptFullPath = sh(returnStdout: true, script: 'pwd').trim()
													sh "${DeployServerStackHelpers.startDBContainer(image, ["path": scriptFullPath, "script": "initDB.sql"], DB_ROOT_PWD)}"
												}
											}
										}
										else
										{
											dir( "${image.workdir}" )
											{
												
												writeFile file: 
													'default.conf', 
													text: DeployServerStackHelpers.generateAppProperties(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
												
												writeFile file: 
													'log4j.properties',
													text: DeployServerStackHelpers.generateLogProperties(image, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW)
											}
										}
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
					if( DeployServerStackHelpers.getLog() )
						println DeployServerStackHelpers.getLog().printLog()
					
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
