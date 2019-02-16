def call( environment, currentBuild, buildParameters )
{
	portusApi = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi(environment, buildParameters)
	jenkinsBuildApi = new at.pii.jenkins_cpsiot_2018.sandbox.JenkinsApi(currentBuild)
	
	pipeline
	{
		agent any
		stages
		{
			stage('Test Parameters')
			{
				steps
				{
					withFolderProperties
					{
						script
						{							
							//println jenkinsBuildApi.getBuildNumber()
							portusApi.init()
							println portusApi.getVars()
						}
					}
				}
			}			
		}
	}
}
	
