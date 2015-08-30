. /home/oracle/.profile
cd /home/oracle/scripts

export ORACLE_SID=orcl

$ORACLE_HOME/bin/sqlplus -s "/ as sysdba" @crt_prt_stats.sql
