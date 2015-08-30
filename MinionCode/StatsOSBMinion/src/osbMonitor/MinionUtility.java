/*
 * 
 * Author: Andrés Mariño
 * email:andresmarino87@gmail.com
 * date:28/4/2015
 * 
 */

package osbMonitor;

public class MinionUtility {
	
	//Builders for insert statements
	static public String getInsertServiceStatsQuery(
		String service_Name,
		String service_Full_Name,
		String message_Count,
		String error_Count,
		String min_Response_Time,
		String max_Response_Time,
		String avg_Response_Time,
		String throttling_Count,
		String throttling_Min_Response_Time,
		String throttling_Max_Response_Time,
		String throttling_AVG_Response_Time,
		String success_Rate,
		String failure_Rate,
		String failover_Count,
		String wss_Error,
		String node,
		String date_Collected){
		String query="{call insertSTATS_OSB_SERVICES('"+service_Name+"','"+service_Full_Name+"',"+message_Count+","+error_Count+","+min_Response_Time+
				","+max_Response_Time+","+avg_Response_Time+","+throttling_Count+","+throttling_Min_Response_Time+
				","+throttling_Max_Response_Time+","+throttling_AVG_Response_Time+","+success_Rate+","+failure_Rate+
				","+failover_Count+","+wss_Error+",'"+node+"')}";
		return query;
	}
	
	static public String getInsertUriStatsQuery(
			String uri_Name,
			String counts,
			String error_Count,
			String min_Response_Time,
			String max_Response_Time,
			String avg_Response_Time,
			String node,
			String date_Collected){
		String query="{call insertSTATS_OSB_URI('"+uri_Name+"',"+counts+","+error_Count+","
				+min_Response_Time+","+max_Response_Time+","+avg_Response_Time+",'"+node+"')}";
		return query;
	}
	
	static public String getInsertWSStatsQuery(
			String ws_Name,
			String ws_Count,
			String error_Count,
			String min_Response_Time,
			String max_Response_Time,
			String avg_Response_Time,
			String node,
			String date_Collected){
		String query="{call insertSTATS_OSB_WEBSERVICES"
				+ "('"+ws_Name+"',"+ws_Count+","+error_Count+","+min_Response_Time+","
				+max_Response_Time+","+avg_Response_Time+",'"+node+"')}";
		return query;
	}	
}