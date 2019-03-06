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
					
					stash name: "auth-artifacts", includes: "authorization/target/**"
					stash name: "serv-artifacts", includes: "serviceregistry_sql/target/**"
					stash name: "event-artifacts", includes: "eventhandler/target/**"
					stash name: "keeper-artifacts", includes: "gatekeeper/target/**"
					stash name: "way-artifacts", includes: "gateway/target/**"
					stash name: "orch-artifacts", includes: "orchestrator/target/**"
				}
			}
			
			stage( "Dockerize Selected Images" )
			{
				steps
				{
					script
					{
						BuildArrowHeadServerStackHelpers.getImages().each
						{
							image ->
							
								if( BuildArrowHeadServerStackHelpers.isDB(image ) )
								{
									//generate dockerfile
									withCredentials(
									[
										string(credentialsId: env.DB_Root_PWD, variable: 'DB_ROOT_PWD'),
										usernamePassword(credentialsId: env.ArrowHead_User_Pwd, usernameVariable: 'DEFAULT_DB_ARROWHEAD_USR', passwordVariable: 'DEFAULT_DB_ARROWHEAD_PSW')
									])
									{
										def DB_ARROWHEAD = BuildArrowHeadServerStackHelpers.getArrowheadDB()
										def DB_ARROWHEAD_LOG = BuildArrowHeadServerStackHelpers.getArrowheadDBLog()
										if( BuildArrowHeadServerStackHelpers.generateDBScript(image) )
										{
											sh "rm -rf database_scripts_cpsiot"
											sh "mkdir database_scripts_cpsiot"

											if( BuildArrowHeadServerStackHelpers.checkGenerateDBScript(image) )
											{
											
												dir( "database_scripts_cpsiot" )
												{
													writeFile file: 'initDB.sql', text: BuildArrowHeadServerStackHelpers.generateDBScript(DB_ROOT_PWD, DEFAULT_DB_ARROWHEAD_USR, DEFAULT_DB_ARROWHEAD_PSW, DB_ARROWHEAD, DB_ARROWHEAD_LOG)
												
													sh "cat initDB.sql"
												}
											}
										}
									}
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
