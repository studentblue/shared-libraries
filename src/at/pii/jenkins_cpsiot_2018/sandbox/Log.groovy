package at.pii.jenkins_cpsiot_2018.sandbox

class Log
{
	def private log
	
	def private errors = 0
	
	def private final LOG = "log: "
	
	def private final ERROR = "error: "
	
	def private final WARNING = "warning: "
	
	def Constants
	
	def init(Constants)
	{
		this.Constants = Constants
		log = []
	}
	
	def addEntry(scope, action, message )
	{
		if( scope == Constants.ERROR )
		{
			errors ++
			log.add(ERROR + action + message )
		}
		
		if( scope == Constants.LOG )
		{
			log.add(LOG + action + message )
		}
		
		if( scope == Constants.WARNING )
		{
			log.add(WARNING + action + message )
		}
	}
	
	def errorsOccured()
	{
		if( errors > 0 )
			return true
		else
			return false
	}
	
	def getLog()
	{
		return log
	}
	
	def printLog()
	{
		def logAsString = ""
		
		log.each
		{
			entry ->
				logAsString += entry + "\n"
		}
		
		return logAsString
	}
}

//~ def Data

//~ def init()
//~ {
	//~ Data = new LogData( this )
//~ }

//~ return this
