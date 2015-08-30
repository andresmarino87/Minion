/*
 * 
 * Author: Andrés Mariño
 * email:andresmarino87@gmail.com
 * date:28/4/2015
 * 
 */

package osbMonitor;

import com.bea.wli.config.Ref;
import com.bea.wli.monitoring.*;

import weblogic.management.jmx.MBeanServerInvocationHandler;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class ServiceStatisticsRetriever {
	private ServiceDomainMBean serviceDomainMbean = null;
	private String serverName = null;
	private final static Properties config=new Properties();
	private SimpleDateFormat dateF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private ArrayList<Ref> refToReset=null;
     /**
       * Retrieve statistics for all business or proxy services being monitored in the
      * domain and reset statistics for the same.
      * @throws Exception
      */

	void getAndResetStatsForAllMonitoredServices(boolean reset, boolean isBusiness) throws Exception {
		Ref[] serviceRefs = null;
		if(isBusiness){
			serviceRefs = serviceDomainMbean.getMonitoredBusinessServiceRefs();
		}else{
			serviceRefs = serviceDomainMbean.getMonitoredProxyServiceRefs();
		}
		refToReset=new ArrayList<Ref>();		
		// Create a bitwise map for desired resource types.
		int typeFlag = 0;
		typeFlag = typeFlag | ResourceType.SERVICE.value();
		typeFlag = typeFlag | ResourceType.WEBSERVICE_OPERATION.value();
		typeFlag = typeFlag | ResourceType.URI.value();
		
		HashMap<Ref, ServiceResourceStatistic> resourcesMap = null;
		// Get cluster-level statistics.
		try {
			// Get statistics.
			if(isBusiness){
				System.out.println("Now trying to get statistics for -" +serviceRefs.length + " business services...");
			}else{
					System.out.println("Now trying to get statistics for -" +serviceRefs.length + " Proxy services...");
			}
			for(Ref aux :serviceRefs){
				try{
					Ref serviceRefsAux[]=new Ref[1];
					serviceRefsAux[0]=aux;
					resourcesMap = serviceDomainMbean.getBusinessServiceStatistics(serviceRefsAux, typeFlag, serverName);
					// Save retrieved statistics.
					saveStatistics(resourcesMap, 0,aux.getLocalName(),aux.getFullName());
					if(reset){
						refToReset.add(aux);
					}
				}catch (DomainMonitoringDisabledException dmde) {
					/** Statistics not available as monitoring is turned off at
	                 domain level.
					 */
					System.out.println("==================================\n");
					System.out.println("Statistics not available as monitoring is turned off at domain level.");
					System.out.println("==============================\n");
				}catch (MonitoringException me) {
					// Internal problem... May be aggregation server is crashed...
					System.out.println("================================\n");
					System.out.println("ERROR: Statistics is not available... Check if aggregation server is crashed...");
					System.out.println("=================================\n");
				}catch(Exception e){
					System.out.println("MORI! "+aux.getLocalName()+" "+e);
				}
			}
			if(reset){
				serviceDomainMbean.resetStatistics(serviceRefs);
				System.out.println("Reset succesfull");
			}
		}catch (IllegalArgumentException iae) {
			System.out.println("===============================\n");
			System.out.println("Encountered IllegalArgumentException...Details:");
			System.out.println(iae.getMessage());
			System.out.println("Check if proxy ref was passed OR flowComp resource was passed OR bitmap is invalid...\nIf so correct it and try again!!!");
			System.out.println("==================================\n");
		}catch (Exception e){
			System.out.println("ERROR: MORIiiii111111. "+e);
		}
	}

     /**
      * Saves statistics of all services from the specified map.
      * @param statsMap Map containing statistics for one or more services
      * of the same type; i.e., business or proxy.
      * @param resetReqTime Reset request time. This information will be
      * written at the end of the file, provided it is not zero.
      * @param fileName Statistics will be saved in a file with this name.
      * @throws Exception
      */
	private void saveStatistics(HashMap<Ref, ServiceResourceStatistic> statsMap,long resetReqTime, String Name, String FullName) throws Exception {
		ArrayList<String> servicesQuerys=new ArrayList<String>();
		ArrayList<String> urisQuerys=new ArrayList<String>();
		ArrayList<String> wsQuerys=new ArrayList<String>();

		
		if (statsMap == null) {
			System.out.println("\nService statistics map is null...Nothing to save.\n");
		}
		if (statsMap.size() == 0) {
			System.out.println("\nService statistics map is empty... Nothing to save.\n");
		}
		Set<Map.Entry<Ref, ServiceResourceStatistic>> set = statsMap.entrySet();
		// Print statistical information of each service
		for (Map.Entry<Ref, ServiceResourceStatistic> mapEntry : set) {
			ServiceResourceStatistic serviceStats = mapEntry.getValue();
			ResourceStatistic[] resStatsArray = null;
			try {
				resStatsArray = serviceStats.getAllResourceStatistics();
			}catch (MonitoringNotEnabledException mnee) {
				mnee.printStackTrace();
				continue;
			}catch (InvalidServiceRefException isre) {
				isre.printStackTrace();
				continue;
			}catch (MonitoringException me) {
				me.printStackTrace();
				continue;
			}catch (Exception e){
				System.out.println("ERROR: MORIii333333333333333333333333333333333.");
				continue;
			}
			for (ResourceStatistic resStats : resStatsArray) {
				StatisticValue[] statValues = resStats.getStatistics();
				
				if(resStats.getResourceType().toString().equalsIgnoreCase(Constant.RT_SERVICE)){
					String message_Count="''";
					String error_Count="''";
					String min_Response_Time="''";
					String max_Response_Time="''";
					String avg_Response_Time="''";
					String throttling_Count="''";
					String throttling_Min_Response_Time="''";
					String throttling_Max_Response_Time="''";
					String throttling_AVG_Response_Time="''";
					String success_Rate="''";
					String failure_Rate="''";
					String failover_Count="''"; 
					String wss_Error="''";
					String date_Collected=dateF.format(new Date(serviceStats.getCollectionTimestamp()));

					for (StatisticValue value : statValues) {
						if(value.getName().equalsIgnoreCase(Constant.SVS_MESSAGE_COUNT)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								message_Count= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_ERROR_COUNT)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								error_Count= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_RESPONSE_TIME)){
							if ( value.getType() == StatisticType.INTERVAL ) {
								StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic)value;
								min_Response_Time=String.valueOf(is.getMin());
								max_Response_Time=String.valueOf(is.getMax());
								avg_Response_Time=String.valueOf(is.getAverage());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_THROTTLING_TIME)){
							if ( value.getType() == StatisticType.INTERVAL ) {
								StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic)value;
								throttling_Count=String.valueOf(is.getCount());
								throttling_Min_Response_Time=String.valueOf(is.getMin());
								throttling_Max_Response_Time=String.valueOf(is.getMax());
								throttling_AVG_Response_Time=String.valueOf(is.getAverage());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_SUCCES_RATE)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								success_Rate= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_FAILURE_RATE)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								failure_Rate= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_FAILOVER_COUNT)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								failover_Count= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_WSS_ERROR)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								wss_Error= String.valueOf(cs.getCount());
							}
						}
					}
					servicesQuerys.add(MinionUtility.getInsertServiceStatsQuery(
							Name,FullName, message_Count,
							error_Count, min_Response_Time,
							max_Response_Time, avg_Response_Time, throttling_Count,
							throttling_Min_Response_Time, throttling_Max_Response_Time, throttling_AVG_Response_Time,
							success_Rate, failure_Rate, failover_Count, wss_Error, config.getProperty(Constant.SERVER_NAME), 
							date_Collected));
				}else if(resStats.getResourceType().toString().equalsIgnoreCase(Constant.RT_URI)){
					resStats.getName();
					String counts="''";
					String error_Count="''";
					String min_Response_Time="''";
					String max_Response_Time="''";
					String avg_Response_Time="''";
					String date_Collected=dateF.format(new Date(serviceStats.getCollectionTimestamp()));
					for (StatisticValue value : statValues) {
						if(value.getName().equalsIgnoreCase(Constant.SVS_MESSAGE_COUNT)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								counts= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_ERROR_COUNT)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								error_Count= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_RESPONSE_TIME)){
							if ( value.getType() == StatisticType.INTERVAL ) {
								StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic)value;
								min_Response_Time=String.valueOf(is.getMin());
								max_Response_Time=String.valueOf(is.getMax());
								avg_Response_Time=String.valueOf(is.getAverage());
							}
						}
					}
					urisQuerys.add(MinionUtility.getInsertUriStatsQuery(
						resStats.getName(), counts, 
						error_Count, min_Response_Time, max_Response_Time, avg_Response_Time, 
						config.getProperty(Constant.SERVER_NAME), date_Collected));
				}else if(resStats.getResourceType().toString().equalsIgnoreCase(Constant.RT_WEBSERVICE)){
					statValues = resStats.getStatistics();
					resStats.getName();
					String counts="''";
					String error_Count="''";
					String min_Response_Time="''";
					String max_Response_Time="''";
					String avg_Response_Time="''";
					String date_Collected=dateF.format(new Date(serviceStats.getCollectionTimestamp()));
					for (StatisticValue value : statValues) {
						if(value.getName().equalsIgnoreCase(Constant.SVS_MESSAGE_COUNT)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								counts= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_ERROR_COUNT)){
							if ( value.getType() == StatisticType.COUNT ) {
								StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic)value;
								error_Count= String.valueOf(cs.getCount());
							}
						}else if(value.getName().equalsIgnoreCase(Constant.SVS_ELAPSED_TIME)){
							if ( value.getType() == StatisticType.INTERVAL ) {
								StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic)value;
								min_Response_Time=String.valueOf(is.getMin());
								max_Response_Time=String.valueOf(is.getMax());
								avg_Response_Time=String.valueOf(is.getAverage());
							}
						}
					}
					wsQuerys.add(MinionUtility.getInsertWSStatsQuery(
						resStats.getName(), counts, 
						error_Count, min_Response_Time, max_Response_Time, avg_Response_Time, 
						config.getProperty(Constant.SERVER_NAME), date_Collected));
				}
			}
		}
		for(String aux:urisQuerys){
			servicesQuerys.add(aux);
		}
		for(String aux:wsQuerys){
			servicesQuerys.add(aux);
		}
		insertQuerysOnDataBase(servicesQuerys);
	}

     /**
      * Init method.
      *
      * @param props Properties required for initialization.
      * @throws Exception
      */
	private void init(HashMap props) throws Exception {
		Properties properties = new Properties();
		properties.putAll(props);
		getServiceDomainMBean(properties.getProperty("HOSTNAME"),
			Integer.parseInt(config.getProperty(Constant.HOST_PORT)),
			properties.getProperty("USERNAME"),
            properties.getProperty("PASSWORD"));
		serverName = properties.getProperty("SERVER_NAME");
	}

	/**
	 * Gets an instance of ServiceDomainMBean from the weblogic server.
	 *
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	private void getServiceDomainMBean(String host, int port, String username, String password) throws Exception {
		InvocationHandler handler = new ServiceDomainMBeanInvocationHandler(host, port,username,password);
		Object proxy = Proxy.newProxyInstance(ServiceDomainMBean.class.getClassLoader(),new Class[]{ServiceDomainMBean.class}, handler);
		serviceDomainMbean = (ServiceDomainMBean) proxy;
	}

     /**
      * Invocation handler class for ServiceDomainMBean class.
      */
	public static class ServiceDomainMBeanInvocationHandler implements InvocationHandler {
		private String jndiURL = "weblogic.management.mbeanservers.domainruntime";
		private String mbeanName = ServiceDomainMBean.NAME;
		private String type = ServiceDomainMBean.TYPE;
     
		private String protocol = config.getProperty(Constant.PROTOCOL);
		private String hostname = config.getProperty(Constant.HOST);
		private int port = Integer.parseInt(config.getProperty(Constant.HOST_PORT));
		private String jndiRoot = "/jndi/";

		private String username = config.getProperty(Constant.USERNAME);
		private String password = config.getProperty(Constant.PWD);

		private JMXConnector conn = null;
		private Object actualMBean = null;

		public ServiceDomainMBeanInvocationHandler(String hostName, int port, String userName, String password) {
			this.hostname = hostName;
			this.port = port;
			this.username = userName;
			this.password = password;
		}

		/**
		 * Gets JMX connection
		 * @return JMX connection
		 * @throws IOException
		 * @throws MalformedURLException
		 */
		public JMXConnector initConnection() throws IOException, MalformedURLException {
			JMXServiceURL serviceURL = new JMXServiceURL(protocol, hostname, port, jndiRoot + jndiURL);
			Hashtable<String, String> h = new Hashtable<String, String>();
			if (username != null)
				h.put(Context.SECURITY_PRINCIPAL, username);
			if (password != null)
				h.put(Context.SECURITY_CREDENTIALS, password);
				h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,"weblogic.management.remote");
				return JMXConnectorFactory.connect(serviceURL, h);
		}

		/**
		 * Invokes specified method with specified params on specified
		 * object.
		 * @param proxy
		 * @param method
		 * @param args
		 * @return
		 * @throws Throwable
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				if (conn == null) {
					conn = initConnection();
				}
				if (actualMBean == null) {
					actualMBean = findServiceDomain(conn.getMBeanServerConnection(),mbeanName, type, null);
				}
				Object returnValue = method.invoke(actualMBean, args);
				return returnValue;
			}catch (Exception e) {
				System.out.println(e);
				throw e;
			}
		}
		/**
		 * Finds the specified MBean object
		 *
		 * @param connection - A connection to the MBeanServer.
		 * @param mbeanName - The name of the MBean instance.
		 * @param mbeanType - The type of the MBean.
		 * @param parent - The name of the parent Service. Can be NULL.
		 * @return Object - The MBean or null if the MBean was not found.
		 */

		public Object findServiceDomain(MBeanServerConnection connection,
			String mbeanName,
			String mbeanType,
			String parent) {
			ServiceDomainMBean serviceDomainbean = null;
			try {
				ObjectName on = new ObjectName(ServiceDomainMBean.OBJECT_NAME);
				serviceDomainbean = (ServiceDomainMBean)MBeanServerInvocationHandler.newProxyInstance(connection, on);
			}catch (MalformedObjectNameException e) {
				e.printStackTrace();
				return null;
			}
			return serviceDomainbean;
		}
	}
	
    static class MinionThread extends Thread {
		private ServiceStatisticsRetriever collector;
		boolean isToReset;
		
		MinionThread(ServiceStatisticsRetriever col,boolean reset) {
			collector = col;
			isToReset = reset;
        }

        public void run() {
			System.out.println("\n**********************************");
			System.out.println("Retrieving statistics for all monitored business services.");
			try {
				collector.getAndResetStatsForAllMonitoredServices(isToReset,true);
				System.out.println("\n**********************************");
				System.out.println("Retrieving statistics for all monitored proxy services.");
				collector.getAndResetStatsForAllMonitoredServices(isToReset,false);
				System.out.println("Successfully retrieved and reset statistics for all monitored \n business services at " + 
				new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(System.currentTimeMillis())));
			} catch (Exception e) {
				System.out.println("Failed to retrieve and reset statistics for all monitored business service...");
				e.printStackTrace();
			}
			System.out.println("**********************************\n");
        }
    }

	/*
	 * 
	 * Connect and insert in oracle data base
	 * 
	 */
	
	static private boolean insertQuerysOnDataBase(ArrayList<String> querys){
		boolean res=false;
		String url = "jdbc:oracle:thin:@"+config.getProperty(Constant.DB_HOST)+":"+config.getProperty(Constant.DB_PORT)+":"+config.getProperty(Constant.DB_NAME);
		//properties for creating connection to Oracle database
		Properties props = new Properties();
		props.setProperty("user", config.getProperty(Constant.DB_USER));
		props.setProperty("password", config.getProperty(Constant.DB_PWD));

		//creating connection to Oracle database using JDBC
		Connection conn=null;
		try {
			conn = DriverManager.getConnection(url,props);
			//creating PreparedStatement object to execute query
			PreparedStatement preStatement;
			for(String aux:querys){
				preStatement = conn.prepareStatement(aux);
				preStatement.executeQuery();
			}
		} catch (SQLException e) {
			System.out.println("ERROR E "+e);
			e.printStackTrace();
		}finally{
			if(conn!=null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}

     /**
        * The main method to start the timer task to extract, save, and reset
       * statistics for all monitored business and proxy services. It uses
       * the following system properties.
       * 1. hostname - Hostname of admin server
       * 2. port - Listening port of admin server
       * 3. username - Login username
       * 4. password - Login password
       * 5. period - Frequency in hours. This will be used by the timer
       * to determine the time gap between two executions.
       *
       * @param args Not used.
       */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		try {
			if(args.length == 4){
				boolean isToReset=false;
				String host=args[0];
				String port=args[1];
				String serverName=args[2];
				String ResetHour=args[3];
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat localHour = new SimpleDateFormat("HH");
				SimpleDateFormat localmin = new SimpleDateFormat("mm");
				ClassLoader loader = Thread.currentThread().getContextClassLoader();           
				InputStream stream = loader.getResourceAsStream("config.properties");
				config.load(stream);
				config.setProperty(Constant.HOST, host);
				config.setProperty(Constant.HOST_PORT, port);
				config.setProperty(Constant.SERVER_NAME, serverName);
				HashMap map = new HashMap();
				map.put("HOSTNAME", config.getProperty(Constant.HOST));
				map.put("PORT", config.getProperty(Constant.HOST_PORT));
				map.put("USERNAME", config.getProperty(Constant.USERNAME));
				map.put("PASSWORD", config.getProperty(Constant.PWD));
				//set a server name if you want to get the uri status statistics in a cluster
				map.put("SERVER_NAME", config.getProperty(Constant.SERVER_NAME));

				isToReset = (localHour.format(cal.getTime()).equals(ResetHour))?true:false;
				isToReset = (isToReset && 0 <= Integer.parseInt(localmin.format(cal.getTime())) && Integer.parseInt(localmin.format(cal.getTime())) <= 30)?true:false;
				
				ServiceStatisticsRetriever collector = new ServiceStatisticsRetriever();
	
				collector.init(map);
				MinionThread minion=new MinionThread(collector,isToReset);
				minion.start();
			}else{
				System.out.println(Constant.MISSING_PARAMETERS);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}