def call( environment, currentBuild, buildParameters )
{
	println environment.getClass()
	println currentBuild.getClass()
	println buildParameters.getClass()
	
	portusApi = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi(environment, currentBuild, buildParameters)
	
	portusApi.checkInputParameters()
	
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
							portusApi.getInputParameters().each
							{
								test1 ->
									println test1
							}
						}
					}
				}
			}			
		}
	}
}
	
