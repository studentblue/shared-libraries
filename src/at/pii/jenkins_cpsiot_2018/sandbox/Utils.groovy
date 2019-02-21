package at.pii.jenkins_cpsiot_2018.sandbox

import groovy.json.*

def httpRequestWithPlugin(url, mode, headers = [], body = "")
{	
	def response = httpRequest httpMode: mode, url: url, acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', customHeaders: headers, requestBody: body
	
	if( mode.equals(constants.HTTP_MODE_GET) && response.status == constants.HTTP_RESPONSE_OK )
		return response
	
	if( mode.equals(constants.HTTP_MODE_POST) && response.status == constants.HTTP_RESPONSE_CREATED )
		return response


	return false
}

return this
