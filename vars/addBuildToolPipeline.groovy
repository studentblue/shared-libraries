def call( environment, currentBuild )
{
	Constants = new at.pii.jenkins_cpsiot_2018.sandbox.Constants()	
	DockerHub = new at.pii.jenkins_cpsiot_2018.sandbox.DockerHub()
	PortusApi = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi()
	AddBuildToolHelpers = new at.pii.jenkins_cpsiot_2018.sandbox.addBuildToolHelpers()
	
	JenkinsApi = new at.pii.jenkins_cpsiot_2018.sandbox.JenkinsApi()
	
	
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
						withCredentials([usernamePassword(credentialsId: environment.PORTUS_USER_TOKEN_API, usernameVariable: 'USER', passwordVariable: 'TOKEN2')])
						{
							script
							{
								DockerHub.init(environment.AddBuildTool, Constants)
								
								if( DockerHub.getLog().errorsOccured() )
								{
									error("Failed")
								}
								
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
								
								AddBuildToolHelpers.init( environment.AddBuildTool, PortusApi, DockerHub, JenkinsApi, Constants  )
								
								if( AddBuildToolHelpers.getLog().errorsOccured() )
								{
									error("Failed")
								}								
							}
						}
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
							def choices = AddBuildToolHelpers.getChoices()
							
							if( choices )
							{
								def userInput = input(id: "Digest", message: 'Please Select Image', ok: 'Select',
										parameters: [choice(name: 'SELECT_IMAGE', choices: choices, description: 'Select the image variant')])
								
								AddBuildToolHelpers.setChoice(userInput)
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
					withFolderProperties
					{
						script
						{
							
							def portus = AddBuildToolHelpers.generatePortus()
							
							if( AddBuildToolHelpers.getLog().errorsOccured() )
							{
								error("Failed")
							}
							
							timeout(time: 5, unit: 'MINUTES')
							{								
								input(id: "Push_Image", message: "Push as \""+portus.namespace.name+"/"+portus.repo.name+":"+portus.repo.tag+"\"", ok: 'PUSH')
							}
							
							
							PortusApi.validateImage(portus)
							
							if( PortusApi.getLog().errorsOccured() )
							{
								error("Validate Image failed")
							}
							
							
							pushImage DockerHubImage: DockerHub.getImage(), portus: portus, portusCredentials: environment.PORTUS_CREDS_STD, portusRepo: environment.REPO_URL
							
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
					if( DockerHub.getLog() )
						println DockerHub.getLog().printLog()
					if( PortusApi.getLog() ) 
						println PortusApi.getLog().printLog()
					if( JenkinsApi.getLog() ) 
						println JenkinsApi.getLog().printLog()
					if( AddBuildToolHelpers.getLog() )
						println AddBuildToolHelpers.getLog().printLog()
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
