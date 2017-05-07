package cmu.isr.arunkaly.configs;

public class Configurations {

	
	public static String CURRENT_DB_NAME = "UpstreamDownstream_1";
	public static String DB_SERVER_HOST = "localhost";
	public static int DB_SERVER_PORT = 27017;
	
	//Index0 is arunkaly ID, index1 is arunkiiitb
	public static String[] CLIENT_IDS = {"YOUR_CLIENT_ID","YOUR_CLIENT_ID"};
	public static String[] CLIENT_SECRETS = {"YOUR_CLIENT_SECRET","YOUR_CLIENT_SECRET"};
	public static int CURRENT_CLIENT_INDEX = 0;
	
	//TimeStamp since we want to collect data => Applicable for all miners that extend TimeBasedMiners. The API should support the since param
	public static  double NUM_YEARS_OF_DATA = -1;
	public static int DAYS_INCREMENT = 90;

	public static int getNextClientIndex() {
		return (CURRENT_CLIENT_INDEX+1)%CLIENT_IDS.length;
	}
}
