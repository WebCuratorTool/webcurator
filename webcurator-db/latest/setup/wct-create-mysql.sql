CREATE DATABASE IF NOT EXISTS DB_WCT;
\u DB_WCT
create user if not exists  usr_wct@localhost identified by 'usr_wct';
grant all on DB_WCT.* to usr_wct@localhost;
#set password for usr_wct@localhost = PASSWORD('password');
ALTER USER 'usr_wct'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';


