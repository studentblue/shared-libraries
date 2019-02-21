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
	
	
	Constants = new at.pii.jenkins_cpsiot_2018.sandbox.Constants()	
	DockerHub = new at.pii.jenkins_cpsiot_2018.sandbox.DockerHub()
	DockerHub.init(environment.AddBuildTool, Constants)
	
	PortusApi = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi()
	
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
								//println environment.AddBuildTool
								//def var = DockerHub.init(environment.AddBuildTool, Constants)
								
								//println var.DockerHub.repo
								//DockerHub.init(environment.AddBuildTool, Constants)
								//DockerHub.getManifests()
								//println DockerHub.getLog().printLog()
								
								if( DockerHub.getLog().errorsOccured() )
								{
									error("Failed")
								}
								
								PortusApi.init(environment.REPO_URL, environment.PORTUS_USER, environment.TOKEN2, Constants)
								
								if( PortusApi.getLog().errorsOccured() )
								{
									error("Failed")
								}
								
								//println "Test"
								
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
		
		
		post
		{
			always
			{
				echo 'Runs always'
				script
				{
					println DockerHub.getLog().printLog()
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
	
//println DockerHub.getLog().printLog()
