package at.pii.jenkins_cpsiot_2018.sandbox

class Constants
{
	def final String SPLITTER = " @::@ "
	def final int ERROR = 1
	def final int LOG = 2
	def final int ERROR_PORTUS_UNHEALTHY = 2
	def final DEFAULT_IMAGE_PREFIX = "cpsiot-build-"
	def final DEFAULT_NAMESPACE_PREFIX = "cpsiot_build_"
	def final UNKNOWN_ARCH_OS = "unknown_arch_os"
	def final ACTION_CREATED = "CREATED: "
	def final ACTION_CHECK = "CHECK: "
	def final ACTION_LOG_START = "LOG_STARTED: "
	final ACTION_EXCEPTION = "AN_EXCEPTION_OCCURED: "
	final HTTP_MODE_GET = "GET"
	final HTTP_MODE_POST = "POST"
	final HTTP_RESPONSE_OK = 200
	final HTTP_RESPONSE_CREATED = 201
}
