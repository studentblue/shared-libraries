package at.pii.jenkins_cpsiot_2018.sandbox

class Log
{
	def private log = []
	
	def private errors = 0
	
	def private final LOG = "log: "
	
	def private final ERROR = "error: "
	
	def addEntry(scope, action, message )
	{
		if( scope == at.pii.jenkins_cpsiot_2018.sandbox.constants.ERROR )
		{
			this.errors ++
			this.log.add(this.ERROR + action + message )
		}
		
		if( scope == at.pii.jenkins_cpsiot_2018.sandbox.constants.LOG )
		{
			this.log.add(this.LOG + action + message )
		}
	}
	
	def errorsOccured()
	{
		if( this.errors > 0 )
			return true
		else
			return false
	}
	
	def getLog()
	{
		return this.log
	}
	
	def printLog()
	{
		def logAsString = ""
		
		this.log.each
		{
			entry ->
				logAsString += entry + "\n"
		}
		
		return logAsString
	}
}
