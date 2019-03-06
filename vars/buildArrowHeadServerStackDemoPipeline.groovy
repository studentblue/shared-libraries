def call( environment, currentBuild, parameter )
{
	
	Constants = new at.pii.jenkins_cpsiot_2018.sandbox.Constants()
	BuildArrowHeadServerStackHelpers = new at.pii.jenkins_cpsiot_2018.sandbox.buildArrowHeadServerStackHelpers()

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
							BuildArrowHeadServerStackHelpers.init(parameter, Constants, environment)
						
							println BuildArrowHeadServerStackHelpers.getParameter()
						}
					}
				}
			}
			
			stage("Create Maven Cache")
			{
				agent{ label "master"}
				steps
				{
					sh " docker volume create maven-repo "
				}
			}
			
			
			
			stage( "Build" )
			{
				agent{ label "master"}
				
				steps
				{
					checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/arrowhead-f/core-java.git']]]
					
					script
					{
						if( ! BuildArrowHeadServerStackHelpers.checkCompileDockerHub() )
						{
							withFolderProperties
							{							
								docker.withRegistry("${environment.REPO_URL}", "${environment.PORTUS_CREDS_STD}")
								{
									withDockerContainer(args: "${BuildArrowHeadServerStackHelpers.getContainerCompileArgs()}", image: "${BuildArrowHeadServerStackHelpers.getDockerCompileImage()}")
									{
										sh "${BuildArrowHeadServerStackHelpers.getCompileCommand()}"
									}
								}
							}
						}
						else
						{
							withDockerContainer(args: "${BuildArrowHeadServerStackHelpers.getContainerCompileArgs()}", image: "${BuildArrowHeadServerStackHelpers.getDockerCompileImage()}")
							{
								sh "${BuildArrowHeadServerStackHelpers.getCompileCommand()}"
							}
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
					if( BuildArrowHeadServerStackHelpers.getLog() )
						println BuildArrowHeadServerStackHelpers.getLog().printLog()
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
