--TABLES
CREATE TABLE STATS_OSB_SERVICES(
	Id NUMBER(15) NOT NULL,
	Service_Name VARCHAR(255) NOT NULL,
	Service_Full_Name VARCHAR(4000) NOT NULL,
	Message_Count NUMBER(13), 
	Error_Count NUMBER(13), 
	Min_Response_Time NUMBER(7), 
	Max_Response_Time NUMBER(7), 
	Avg_Response_Time NUMBER(7), 
	Throttling_Count NUMBER(7), 
	Throttling_Min_Response_Time NUMBER(7), 
	Throttling_Max_Response_Time NUMBER(7),
	Throttling_AVG_Response_Time NUMBER(7),
	Success_Rate NUMBER(7),
	Failure_Rate NUMBER(7),
	Failover_Count NUMBER(7),
	Wss_Error NUMBER(7),
	Node VARCHAR(255) NOT NULL,
	Date_Collected DATE
)PARTITION BY RANGE (Date_Collected)(
	PARTITION "20150616" VALUES LESS THAN(TO_DATE('20150616','YYYYMMDD'))
)
TABLESPACE STATS_OSB_SERVICES;

CREATE TABLE STATS_OSB_URI(
	Id NUMBER(15) NOT NULL,
	Id_Service_Stats NUMBER(15) NOT NULL,	
	Uri_Name VARCHAR(255) NOT NULL,
	Uri_Counts NUMBER(13), 
	Error_Count NUMBER(13), 
	Min_Response_Time NUMBER(7), 
	Max_Response_Time NUMBER(7), 
	Avg_Response_Time NUMBER(7), 
	Node VARCHAR(255) NOT NULL,
 	Date_Collected DATE
)PARTITION BY RANGE (Date_Collected)(
	PARTITION "20150616" VALUES LESS THAN(TO_DATE('20150616','YYYYMMDD'))
)TABLESPACE STATS_OSB_SERVICES;

CREATE TABLE STATS_OSB_WEBSERVICES(
	Id NUMBER(15) NOT NULL,
	Id_Service_Stats NUMBER(15) NOT NULL,	
	WS_Name VARCHAR(255) NOT NULL,
	WS_Count NUMBER(13), 
	Error_Count NUMBER(13), 
	Min_Response_Time NUMBER(7), 
	Max_Response_Time NUMBER(7), 
	Avg_Response_Time NUMBER(7), 
	Node VARCHAR(255) NOT NULL,
	Date_Collected DATE
)PARTITION BY RANGE (Date_Collected)(
	PARTITION "20150616" VALUES LESS THAN(TO_DATE('20150616','YYYYMMDD'))
)TABLESPACE STATS_OSB_SERVICES;

--SEQUENCES
CREATE SEQUENCE sou_seq;
CREATE SEQUENCE sows_seq;
CREATE SEQUENCE sos_seq;

--INDEX
CREATE INDEX idx_sos_dc ON STATS_OSB_SERVICES (Date_Collected) LOCAL;
CREATE INDEX idx_sou_dc ON STATS_OSB_URI (Date_Collected) LOCAL;
CREATE INDEX idx_sows_dc ON STATS_OSB_WEBSERVICES (Date_Collected) LOCAL;

--INSERT PROCEDURES 
CREATE OR REPLACE PROCEDURE insertSTATS_OSB_SERVICES(
	p_service_name IN STATS_OSB_SERVICES.Service_Name%TYPE,	
	p_service_full_name IN STATS_OSB_SERVICES.Service_Full_Name%TYPE,	
	p_message_count IN STATS_OSB_SERVICES.Message_Count%TYPE,
	p_error_count IN STATS_OSB_SERVICES.Error_Count%TYPE,
	p_min_response_time IN STATS_OSB_SERVICES.Min_Response_Time%TYPE,
	p_max_response_time IN STATS_OSB_SERVICES.Max_Response_Time%TYPE,
	p_avg_response_time IN STATS_OSB_SERVICES.Avg_Response_Time%TYPE,
	p_throttling_count IN STATS_OSB_SERVICES.Throttling_Count%TYPE,
	p_throttling_min_response_time IN STATS_OSB_SERVICES.Throttling_Min_Response_Time%TYPE,
	p_Throttling_max_response_time IN STATS_OSB_SERVICES.Throttling_Max_Response_Time%TYPE,
	p_throttling_avg_response_time IN STATS_OSB_SERVICES.Throttling_AVG_Response_Time%TYPE,
	p_success_rate IN STATS_OSB_SERVICES.Success_Rate%TYPE,
	p_failure_rate IN STATS_OSB_SERVICES.Failure_Rate %TYPE,
	p_failover_count IN STATS_OSB_SERVICES.Failover_Count%TYPE,
	p_wss_Error IN STATS_OSB_SERVICES.Wss_Error%TYPE,
	p_node IN STATS_OSB_SERVICES.Node%TYPE)
IS
BEGIN
	INSERT INTO STATS_OSB_SERVICES(Id, Service_Name, Service_Full_Name, Message_Count, Error_Count, Min_Response_Time, 
	Max_Response_Time, Avg_Response_Time, Throttling_Count, Throttling_Min_Response_Time, 
	Throttling_Max_Response_Time, Throttling_AVG_Response_Time, Success_Rate,Failure_Rate, 
	Failover_Count, Wss_Error,Node, Date_Collected) VALUES (sos_seq.nextval, p_service_name, p_service_full_name, p_message_count,
	p_error_count, p_min_response_time, p_max_response_time, p_avg_response_time, p_throttling_count, p_throttling_min_response_time,
	p_Throttling_max_response_time, p_throttling_avg_response_time, p_success_rate, p_failure_rate, p_failover_count, p_wss_Error, 
	p_node, CURRENT_TIMESTAMP); 
	COMMIT;
EXCEPTION
	WHEN OTHERS THEN  -- handles all other errors
    	raise_application_error (-20002,'An error has occurred inserting an SERVICES.- '||SQLCODE||' -ERROR- '||SQLERRM);
END;
/

CREATE OR REPLACE PROCEDURE insertSTATS_OSB_URI(
	p_uri_name IN STATS_OSB_URI.Uri_Name%TYPE,	
	p_uri_count IN STATS_OSB_URI.Uri_Counts%TYPE,
	p_error_count IN STATS_OSB_URI.Error_Count%TYPE,
	p_min_response_time IN STATS_OSB_URI.Min_Response_Time%TYPE,
	p_max_response_time IN STATS_OSB_URI.Max_Response_Time%TYPE,
	p_avg_response_time IN STATS_OSB_URI.Avg_Response_Time%TYPE,
	p_node IN STATS_OSB_URI.Node%TYPE)
IS
BEGIN
	INSERT INTO STATS_OSB_URI(Id,Id_Service_Stats,Uri_Name,Uri_Counts,Error_Count,Min_Response_Time,
		Max_Response_Time,Avg_Response_Time, node, Date_Collected) VALUES (sou_seq.nextval, sos_seq.currval,
		p_uri_name, p_uri_count,p_error_count,p_min_response_time,p_max_response_time,p_avg_response_time,
		p_node, CURRENT_TIMESTAMP); 
	COMMIT;
EXCEPTION
	WHEN OTHERS THEN  -- handles all other errors
		raise_application_error (-20002,'An error has occurred inserting an URI.- '||SQLCODE||' -ERROR- '||SQLERRM);
END;
/

--PROCEDURE insertSTATS_OSB_WEBSERVICES
CREATE OR REPLACE PROCEDURE insertSTATS_OSB_WEBSERVICES(
	p_ws_name IN STATS_OSB_WEBSERVICES.WS_Name%TYPE,	
	p_ws_count IN STATS_OSB_WEBSERVICES.WS_Count%TYPE,
	p_error_count IN STATS_OSB_WEBSERVICES.Error_Count%TYPE,
	p_min_response_time IN STATS_OSB_WEBSERVICES.Min_Response_Time%TYPE,
	p_max_response_time IN STATS_OSB_WEBSERVICES.Max_Response_Time%TYPE,
	p_avg_response_time IN STATS_OSB_WEBSERVICES.Avg_Response_Time%TYPE,
	p_node IN STATS_OSB_WEBSERVICES.Node%TYPE)
IS
BEGIN
	INSERT INTO STATS_OSB_WEBSERVICES(Id,Id_Service_Stats,WS_Name,WS_Count,Error_Count,Min_Response_Time,
		Max_Response_Time,Avg_Response_Time, Node, Date_Collected) VALUES (sows_seq.nextval, sos_seq.currval,
		p_ws_name, p_ws_count,p_error_count,p_min_response_time,p_max_response_time,p_avg_response_time,
		p_node, CURRENT_TIMESTAMP); 
	COMMIT;
EXCEPTION
	WHEN OTHERS THEN  -- handles all other errors
		raise_application_error (-20002,'An error has occurred inserting an WEBSERVICES.- '||SQLCODE||' -ERROR- '||SQLERRM);
END;
/

--Functions 

CREATE OR REPLACE FUNCTION get_all_services_names
  RETURN SYS_REFCURSOR
AS
  result SYS_REFCURSOR;
BEGIN
  OPEN result FOR SELECT DISTINCT Service_Full_Name FROM STATS_OSB_SERVICES;
  RETURN result;
END get_all_services_names;
/

CREATE OR REPLACE FUNCTION get_all_components
  RETURN SYS_REFCURSOR
AS
  result SYS_REFCURSOR;
BEGIN
  OPEN result FOR SELECT DISTINCT node FROM STATS_OSB_SERVICES;
  RETURN result;
END get_all_components;
/

CREATE OR REPLACE FUNCTION get_data_from_to()
  RETURN SYS_REFCURSOR
AS
  result SYS_REFCURSOR;
BEGIN
  OPEN result FOR SELECT * FROM STATS_OSB_SERVICES WHERE date BETWEEN AND  ;
  RETURN result;
END get_all_components;
/