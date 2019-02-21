package at.pii.jenkins_cpsiot_2018.sandbox

class LogData
{
	def private log = []
	
	def private errors = 0
	
	def private final LOG = "log: "
	
	def private final ERROR = "error: "
	
	def outer
	
	LogData(outerClass)
	{
		this.outer = outerClass
	}
	
	def addEntry(scope, action, message )
	{
		if( scope == outer.constants.ERROR )
		{
			this.errors ++
			this.log.add(this.ERROR + action + message )
		}
		
		if( scope == outer.constants.LOG )
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
		return log
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

def Data

def init()
{
	Data = new LogData( this )
}

return this
