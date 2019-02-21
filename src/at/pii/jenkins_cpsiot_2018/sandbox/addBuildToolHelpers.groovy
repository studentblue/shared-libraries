package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*
import org.boon.Boon

//import groovy.json.JsonSlurperClassic

def log = new at.pii.jenkins_cpsiot_2018.sandbox.Log()

def PortusApi

def input 

def init( inputParameter, url, user, token  )
{
	input = new JsonSlurperClassic().parseText(inputParameter)
	
	PortusApi = new at.pii.jenkins_cpsiot_2018.sandbox.PortusApi(url, user, token)
	
}

def checkInput()
{
	
	if(! input.DockerHub )		
		log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Object DockerHub not found in Json" )
	else
		log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Object DockerHub found in Json" )
	
	
	if(! input.Namespace )
		log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Object Namespace not found in Json" )
	else
		log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Object Namespace found in Json" )
	
	if(! input.Repo )
		log.addEntry(Constants.ERROR, Constants.ACTION_CHECK, "Object Repo not found in Json" )
	else
		log.addEntry(Constants.LOG, Constants.ACTION_CHECK, "Object Repo found in Json" )
}

return this
