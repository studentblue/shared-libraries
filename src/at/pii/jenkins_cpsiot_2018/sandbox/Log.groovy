package at.pii.jenkins_cpsiot_2018.sandbox

class LogData
{
	def private log = []
	
	def private errors = 0
	
	def private final LOG = "log: "
	
	def private final ERROR = "error: "
	
	def outer
	
	//~ LogData(outerClass)
	//~ {
		//~ outer = outerClass
	//~ }
	
	LogData()
	{
	}
	
	def addEntry(scope, action, message )
	{
		if( scope == at.pii.jenkins_cpsiot_2018.sandbox.constants.ERROR )
		{
			errors ++
			log.add(ERROR + action + message )
		}
		
		if( scope == at.pii.jenkins_cpsiot_2018.sandbox.constants.LOG )
		{
			log.add(LOG + action + message )
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
