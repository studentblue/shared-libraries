def call( environment, currentBuild )
{
	//portusApiData = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi.PortusApiData(environment, buildParameters)
	/*
	portus = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi()
	portus.init()
	portusApi = portus.PortusData
	
	jenkinsOuterBuildApi = new at.pii.jenkins_cpsiot_2018.sandbox.JenkinsApi()
	jenkinsOuterBuildApi.init()
	jenkinsBuildApi = jenkinsOuterBuildApi.jenkinsApiInstance
	*/
	
	//DockerHub = new at.pii.jenkins_cpsiot_2018.sandbox.DockerHub()	
	Constants = new at.pii.jenkins_cpsiot_2018.sandbox.Constants()
	Log = new at.pii.jenkins_cpsiot_2018.sandbox.Log(Constants)
	
	//Log.init()
	//Log = Log.Data
	
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
								Log.addEntry(1, " Test: ", "Test" )
								println Log.getLog()
								println Log.printLog()
								//DockerHub.init(environment.AddBuildTool.DockerHub.repo, environment.AddBuildTool.DockerHub.tag)
								//dockerHub.print()
								//jenkinsBuildApi.init( currentBuild )
								//println jenkinsBuildApi.getBuildNumber()
								//println env.AddBuildTool
								//println portusApi.init(environment)
								//def message = portusApi.checkInputParameters()
								//if( message != true )
								//	error(message)
								
								//println portusApi.isPortusHealthy()
								//println portusApi.PortusData.test()
								//portusApi.test()
								
								//println portusApi.dockerHubRepo
							}
						}
					}
				}
			}
			/*
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
								println "Chosen: " + "\"${userInput}\""
							}
						}
					}
				}
			}
			
			stage("Push Selected Image")
			{
				steps
				{
					script
					{
						if ( ! portusApi.generatePortus() )
							error("could not validate Portus settings")
						//input(id: "Push_Image", message: "Push as \"${portusApi.namespace}/${portusApi.repoName}:${portusApi.repoTag}\"", ok: 'PUSH')
						//portusApi.pushImage()
						
					}
				}
			}
			*/
		}
	}
}
	
