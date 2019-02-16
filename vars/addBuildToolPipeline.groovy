def call( environment, buildParameters )
{
	//portusApiData = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi.PortusApiData(environment, buildParameters)
	
	portus = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi()
	portus.init(environment, buildParameters)
	portusApi = portus.PortusData
	
	jenkinsOuterBuildApi = new at.pii.jenkins_cpsiot_2018.sandbox.JenkinsApi()
	jenkinsOuterBuildApi.init()
	jenkinsBuildApi = jenkinsOuterBuildApi.jenkinsApiInstance
	
	
	pipeline
	{
		agent any
		stages
		{
			stage('Init Env & Test Parameters')
			{
				steps
				{
					withFolderProperties
					{
						withCredentials([string(credentialsId: env.Portus_Token, variable: 'TOKEN2')])
						{
							script
							{
								jenkinsBuildApi.init( currentBuild )
								println jenkinsBuildApi.getBuildNumber()
								
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
			
			stage('Fetch Manifests from DockerHub')
			{
				steps
				{
					script
					{
						portusApi.getManifestsFromDockhub()
					}
				}
			}
			
			stage('Select Image')
			{
				steps
				{
					timeout(time: 5, unit: 'MINUTES')
					{
						script
						{	
							def choices = portusApi.getChoices()
							
							if( choices )
							{
								def userInput = input(id: "Digest", message: 'Please Select Image', ok: 'Select',
										parameters: [choice(name: 'SELECT_IMAGE', choices: choices, description: 'Select the image variant')])
								
								portusApi.setChoice(userInput)
							}
						}
					}
				}
			}
		}
	}
}
	
