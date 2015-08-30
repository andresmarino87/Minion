set pagesize 300
set heading off
set feedback off
spool salida.sql

select 'ALTER TABLE ' || table_owner || '.' || table_name ||' DROP PARTITION '||chr(34)||PARTITION_NAME||chr(34)||';'
  from dba_tab_partitions where table_name='STATS_OSB_SERVICES' and to_date(PARTITION_NAME,'rrrrmmdd') < trunc(sysdate-6);
spool off
@salida.sql

spool salida.sql
select 'ALTER TABLE ' || table_owner || '.' || table_name ||' DROP PARTITION '||chr(34)||PARTITION_NAME||chr(34)||';'
  from dba_tab_partitions where table_name='STATS_OSB_URI' and to_date(PARTITION_NAME,'rrrrmmdd') < trunc(sysdate-6);
spool off
@salida.sql

spool salida.sql
select 'ALTER TABLE ' || table_owner || '.' || table_name ||' DROP PARTITION '||chr(34)||PARTITION_NAME||chr(34)||';'
  from dba_tab_partitions where table_name='STATS_OSB_WEBSERVICES' and to_date(PARTITION_NAME,'rrrrmmdd') < trunc(sysdate-6);
spool off
@salida.sql

exit;
