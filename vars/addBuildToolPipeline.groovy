def call( environment, currentBuild, buildParameters )
{
	portusApi = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi(environment, currentBuild, buildParameters)
	
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
							portusApi.checkInputParameters()
						}
					}
				}
			}			
		}
	}
}
	
