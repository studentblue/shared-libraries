def call( environment, currentBuild, parameter )
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
							
								DeployServerStackHelpers.init(parameter, Constants, environment, PortusApi, JenkinsApi)
								
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
