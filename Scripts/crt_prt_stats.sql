set serveroutput on
declare
 cont1 number(4);
 regname varchar2(30);
 regval varchar2(30);
 i number(3);
 sentencia varchar2(300);
 dias number(3):=3;
begin
 -- Crear Particiones para STATS_OSB_SERVICES
 
 select trunc(sysdate+dias) - (select to_date(PARTITION_NAME,'rrrrmmdd')+1 from dba_tab_partitions where table_name='STATS_OSB_SERVICES' and PARTITION_POSITION =(select max(PARTITION_POSITION) from dba_tab_partitions where table_name='STATS_OSB_SERVICES')) into cont1 from dual;
DBMS_OUTPUT.PUT_LINE ('Contador: '||cont1);
if cont1 > 0 then
  for i in 1..cont1 loop
    SELECT to_char(to_date(PARTITION_NAME,'rrrrmmdd')+1,'rrrrmmdd'), to_char(to_date(PARTITION_NAME,'rrrrmmdd')+2,'rrrrmmdd') into regname, regval FROM dba_tab_partitions where table_name='STATS_OSB_SERVICES' AND PARTITION_POSITION =(select max(PARTITION_POSITION) from dba_tab_partitions where table_name='STATS_OSB_SERVICES');
    --SELECT to_char(to_date(PARTITION_NAME,'rrrrmmdd')+2,'rrrrmmdd') into regval FROM dba_tab_partitions where table_name='STATS_OSB_SERVICES' AND PARTITION_POSITION =(select max(PARTITION_POSITION) from dba_tab_partitions where table_name='STATS_OSB_SERVICES');
     sentencia:='ALTER TABLE minion.stats_osb_services ADD PARTITION '||chr(34)||regname||chr(34)||' VALUES LESS THAN (TO_DATE('||chr(39)||regval||' 00:00:00'||chr(39)||', '||chr(39)||'YYYYMMDD HH24:MI:SS'||chr(39)||')) TABLESPACE STATS_OSB_SERVICES';
     DBMS_OUTPUT.PUT_LINE (sentencia);
     execute immediate (sentencia);
    commit;
  end loop;
end if;
-- Crear particiones para STATS_OSB_URI

select trunc(sysdate+dias) - (select to_date(PARTITION_NAME,'rrrrmmdd')+1 from dba_tab_partitions where table_name='STATS_OSB_URI' and PARTITION_POSITION =(select max(PARTITION_POSITION) from dba_tab_partitions where table_name='STATS_OSB_URI')) into cont1 from dual;
DBMS_OUTPUT.PUT_LINE ('Contador: '||cont1);
if cont1 > 0 then
  for i in 1..cont1 loop
    SELECT to_char(to_date(PARTITION_NAME,'rrrrmmdd')+1,'rrrrmmdd'), to_char(to_date(PARTITION_NAME,'rrrrmmdd')+2,'rrrrmmdd') into regname, regval FROM dba_tab_partitions where table_name='STATS_OSB_URI' AND PARTITION_POSITION =(select max(PARTITION_POSITION) from dba_tab_partitions where table_name='STATS_OSB_URI');
     sentencia:='ALTER TABLE minion.STATS_OSB_URI ADD PARTITION '||chr(34)||regname||chr(34)||' VALUES LESS THAN (TO_DATE('||chr(39)||regval||' 00:00:00'||chr(39)||', '||chr(39)||'YYYYMMDD HH24:MI:SS'||chr(39)||')) TABLESPACE STATS_OSB_SERVICES';
     DBMS_OUTPUT.PUT_LINE (sentencia);
     execute immediate (sentencia);     
    commit;
  end loop;
end if;

-- Crear particiones para STATS_OSB_WEBSERVICES

select trunc(sysdate+dias) - (select to_date(PARTITION_NAME,'rrrrmmdd')+1 from dba_tab_partitions where table_name='STATS_OSB_WEBSERVICES' and PARTITION_POSITION =(select max(PARTITION_POSITION) from dba_tab_partitions where table_name='STATS_OSB_WEBSERVICES')) into cont1 from dual;
DBMS_OUTPUT.PUT_LINE ('Contador: '||cont1);
if cont1 > 0 then
  for i in 1..cont1 loop
    SELECT to_char(to_date(PARTITION_NAME,'rrrrmmdd')+1,'rrrrmmdd'), to_char(to_date(PARTITION_NAME,'rrrrmmdd')+2,'rrrrmmdd') into regname, regval FROM dba_tab_partitions where table_name='STATS_OSB_WEBSERVICES' AND PARTITION_POSITION =(select max(PARTITION_POSITION) from dba_tab_partitions where table_name='STATS_OSB_WEBSERVICES');
     sentencia:='ALTER TABLE minion.STATS_OSB_WEBSERVICES ADD PARTITION '||chr(34)||regname||chr(34)||' VALUES LESS THAN (TO_DATE('||chr(39)||regval||' 00:00:00'||chr(39)||', '||chr(39)||'YYYYMMDD HH24:MI:SS'||chr(39)||')) TABLESPACE STATS_OSB_SERVICES';
     DBMS_OUTPUT.PUT_LINE (sentencia);
     execute immediate (sentencia);     
    commit;
  end loop;
end if;

end;
/
exit;
