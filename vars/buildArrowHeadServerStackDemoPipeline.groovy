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
						BuildArrowHeadServerStackHelpers.init(parameter, Constants)
						
						println BuildArrowHeadServerStackHelpers.getParameter()
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
