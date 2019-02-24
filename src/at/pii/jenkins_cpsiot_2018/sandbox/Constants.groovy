package at.pii.jenkins_cpsiot_2018.sandbox

class Constants
{
	final String SPLITTER = " @::@ "
	final int ERROR = 1
	final int LOG = 2
	final int ERROR_PORTUS_UNHEALTHY = 2
	
	final String HTTP_ERROR = "HTTP Request Failed: "
	
	
	final String DEFAULT_IMAGE_PREFIX = "cpsiot-build-"
	final String DEFAULT_NAMESPACE_PREFIX = "cpsiot_build_"
	final String DEFAULT_TAG_PREFIX = "cpsiot"
	final String UNKNOWN_ARCH_OS = "unknown_arch_os"
	final String ACTION_CREATED = "CREATED: "
	final String ACTION_CHECK = "CHECK: "
	final String ACTION_SET = "SET: "
	final String ACTION_CHOICE = "CHOICE: "
	final String ACTION_LOG_START = "LOG_STARTED: "
	final String ACTION_EXCEPTION = "AN_EXCEPTION_OCCURED: "
	final String ACTION_ALERT = "ALERT: "
	final String ACTION_VALIDATE = "VALIDATE: "
	final String NAMESPACE_CREATED = "ADDED_NEW_NAMESPACE: "
	final String NAMESPACE_FOUND = "NAMESPACE_FOUND: "
	final String TEAM_FOUND = "TEAM_FOUND: "
	final String TEAM_CREATED = "ADDED_NEW_TEAM: "
	final String HTTP_MODE_GET = "GET"
	final String HTTP_MODE_POST = "POST"
	
	final int HTTP_RESPONSE_OK = 200
	final int HTTP_RESPONSE_CREATED = 201
}
