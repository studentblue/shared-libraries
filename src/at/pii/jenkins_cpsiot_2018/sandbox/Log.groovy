package at.pii.jenkins_cpsiot_2018.sandbox

class Log
{
	def private log;
	def private warningsLog;
	
	def private errors = 0;
	
	def private warnings = 0;
	
	def private final LOG = "log: ";
	
	def private final ERROR = "error: ";
	
	def private final WARNING = "warning: ";
	
	def Constants;
	
	def init(Constants)
	{
		this.Constants = Constants
		log = []
		warningsLog = []
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
			warningsLog.add(WARNING + action + message )
			warnings++
		}
	}
	
	def errorsOccured()
	{
		if( errors > 0 )
			return true
		else
			return false
	}
	
	def warningsOccured()
	{
		if( warnings > 0 )
			return true
		else
			return false
	}
	
	def getLog()
	{
		return log
	}
	
	def getWarningsLog()
	{
		return warningsLog
	}
	
	def getWarningsStringLog()
	{
		return warningsLog.join(", ")
	}
	
	def resetWarningsLog()
	{
		warningsLog = []
		warnings = 0
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
