package at.pii.jenkins_cpsiot_2018.sandbox;

class Constants
{
	final String SPLITTER = " @::@ ";
	final int ERROR = 1;
	final int LOG = 2;
	final int ERROR_PORTUS_UNHEALTHY = 3;
	final int WARNING = 4;
	
	final String HTTP_ERROR = "HTTP Request Failed: ";
	final String REPOSITORIES_FETCH_ID_ERROR = "REPOSITORY_FETCH_ID_FAILED: ";
	
	final String DEFAULT_IMAGE_PREFIX = "image_";
	final String DEFAULT_NAMESPACE_PREFIX = "cpsiot-build-";
	final String DEFAULT_TAG_PREFIX = "version";
	final String DEFAULT_CLOUD_PREFIX = "cloud-";
	final String DEFAULT_CLOUD_TAG = "cl-ver-";
	final String DEFAULT_DEPLOY_TAG = "dpy-ver-";
	final String UNKNOWN_ARCH_OS = "unknown_arch_os";
	final String ACTION_CREATED = "CREATED: ";
	final String ACTION_CHECK = "CHECK: ";
	final String ACTION_SET = "SET: ";
	final String ACTION_CHOICE = "CHOICE: ";
	final String ACTION_LOG_START = "LOG_STARTED: ";
	final String ACTION_EXCEPTION = "AN_EXCEPTION_OCCURED: ";
	final String ACTION_ALERT = "ALERT: ";
	final String ACTION_VALIDATE = "VALIDATE: "	;
	final String ACTION_NAMESPACE_VALIDATION = "VALIDATE_NAMESPACE: ";
	final String ACTION_NAMESPACE_REPO_TAG_VALIDATION = "VALIDATE_NAMESPACE->REPO->TAG: ";
	final String ACTION_TEAM_VALIDATION = "VALIDATE_TEAM: ";
	final String ACTION_INFO = "INFO: ";
	final String ACTION_CONTAINER = "CONTAINER: ";
	final String ACTION_NETWORK = "NETWORK: ";
	final String NAMESPACE_CREATED = "ADDED_NEW_NAMESPACE: ";
	final String NAMESPACE_FOUND = "NAMESPACE_FOUND: ";
	final String NAMESPACES_FETCHED = "NAMESPACES_FETCHED: ";
	final String REPOSITORIES_FETCHED = "REPOSITORIES_FETCHED: ";
	final String TAGS_FETCHED = "TAGS_FETCHED: ";
	final String TEAM_FOUND = "TEAM_FOUND: ";
	final String TEAM_CREATED = "ADDED_NEW_TEAM: ";
	final String HTTP_MODE_GET = "GET";
	final String HTTP_MODE_POST = "POST";
	
	
	final int GET_DOCKERHUB_REPO_AND_TAG_MODE_URL = 12;
	final int GET_DOCKERHUB_REPO_AND_TAG_MODE_SIMPLE = 13;
	
	final int HTTP_RESPONSE_OK = 200;
	final int HTTP_RESPONSE_CREATED = 201;
	
	final String NAMESPACE_DEPLOY_PREFIX = "cpsiot-deploy-";
	
	final String DB_ADRESS_SUFFIX = "-db-server";
	final String SR_ADRESS_SUFFIX = "-service-registry";
	final String AUTH_ADRESS_SUFFIX = "-authorization";
	final String GATEWAY_ADRESS_SUFFIX = "-gateway";
	final String EH_ADRESS_SUFFIX = "-eventhandler";
	final String GK_ADRESS_SUFFIX = "-gatekeeper";
	final String ORCH_ADRESS_SUFFIX = "-orchestrator";
	
	final String DB_TAG_ID = "dbt";
	final String SR_TAG_ID = "srt";
	final String AUTH_TAG_ID = "aut";
	final String GATEWAY_TAG_ID = "gwt";
	final String EH_TAG_ID = "eht";
	final String GK_TAG_ID = "gkt";
	final String ORCH_TAG_ID = "ort";
	
	final String DEFAULT_CLOUD_NETWORK = "my-cloud";
	
	final int IS_DB = 11;
	final int IS_SR = 12;
	final int IS_AUTH = 13;
	final int IS_GK = 14;
	final int IS_GW = 15;
	final int IS_EH = 16;
	final int IS_ORCH = 17;
	
	
	final int IS_TYPE_JAVA = 18;
	final int IS_TYPE_MYSQL = 19;
	final int IS_TYPE_POSTGRES = 20;
	final int IS_TYPE_OTHER = 21;
	
	//~ [, , , ]
	final String BUILD_TOOL_TYPE_JAVA = "base-with-java";
	final String BUILD_TOOL_TYPE_MYSQL = "mysql-DB";
	final String BUILD_TOOL_TYPE_POSTGRES = "postgres-DB";
	final String BUILD_TOOL_TYPE_OTHER = "other";
	
	final String TYPE_JAVA_TAG_ID = "javbt";
	final String TYPE_MYSQL_TAG_ID = "mysqlbt";
	final String TYPE_POSTGRES_TAG_ID = "pgrsbt";
	final String TYPE_OTHER_TAG_ID = "otherbt";
	
}
