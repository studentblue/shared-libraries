def call( environment, currentBuild, buildParameters )
{
	//portusApiData = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi.PortusApiData(environment, buildParameters)
	
	portusApi = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi()
	portusApi.init(environment, buildParameters)
	
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
						withCredentials([string(credentialsId: env.Portus_Token, variable: 'TOKEN2')])
						{
							script
							{							
								//println jenkinsBuildApi.getBuildNumber()
								portusApi.initData()
								def message = portusApi.checkInputParameters()
								if( message != true )
									error(message)
								
								portusApi.isPortusHealthy()
								//portusApi.test()
							}
						}
					}
				}
			}			
		}
	}
}
	
