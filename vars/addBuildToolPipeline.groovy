def call( environment, currentBuild, buildParameters )
{
	//portusApiData = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi.PortusApiData(environment, buildParameters)
	
	portus = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi()
	portus.init(environment, buildParameters)
	portusApi = portus.PortusData
	
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
								portusApi.init()
								def message = portusApi.checkInputParameters()
								if( message != true )
									error(message)
								
								println portusApi.isPortusHealthy()
								//println portusApi.PortusData.test()
								//portusApi.test()
							}
						}
					}
				}
			}			
		}
	}
}
	
