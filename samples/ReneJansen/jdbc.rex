#!/usr/bin/env rexx
/*
   author:     Rene Vincent Jansen ("rvj"), 2006-11
   purpose:    demonstrate how to use JDBC using different RDBMS
   version:    0.6
   needs:      JDBC-driver (usually a .jar archive) added to CLASSPATH, such that Java can
               find the JDBC related classes of the database you wish to work with

               - as of 2020-08-12 URLs to the RDBMS used in here, such that one can get
                 the RDBMS and its JDBC driver

                 - "A" - MariaDB (a fork of MySQL): <https://mariadb.org/>
                   open-source RDBMS implemented in C++; downloads for the RDBMS and the
                   JDBC driver (loook for "Connect/J"): <https://downloads.mariadb.org/>;
                   ad e.g. "mariadb-java-client-2.6.2.jar" (use the latest version and
                   adjust the file name) to the CLASSPATH environment variable. ; also
                   adjust the connection URL according to your database name, userid and password

                 - "D" - Apache Derby: <https://db.apache.org/derby/index.html>, an open-source
                   RDBMS implemented fully in Java; download page:
                   <https://db.apache.org/derby/derby_downloads.html>, "derby.jar"
                   contains the RDBMS and the JDBC driver, add "derby.jar"
                   to the CLASSPATH environment variable.

                 - "H" - HyperSQL (hsql): <http://hsqldb.org/>, an open-source RDBMS implemented
                   fully in Java; download page:
                   <http://sourceforge.net/project/showfiles.php?group_id=23316>,
                   "hsql.jar" contains the RDBMS and the JDBC driver, add "hsql.jar" to
                   the CLASSPATH environment variable.

                 - "L" - SQLite: <https://www.sqlite.org/index.html>, an open-source RDBMS
                   implemented in C++, you need to run the RDBMS separately, which you
                   can download for Java via <https://github.com/xerial/sqlite-jdbc> at:
                   <https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/>. The JDBC driver
                   includes the binaries for most of the important operating systems already!
                   Just add "hsqldb.jar" to the CLASSPATH environment variable.

                 - "P" - PostgreSQL: <https://www.postgresql.org/>, an open-source RDBMS
                   implemented in C++, you need to run the RDBMS separately, which you
                   can download at: <https://www.postgresql.org/download/> for your
                   operating system; to connect via Java you need the PostgreSQL JDBC
                   driver which can be downloaded from: <https://jdbc.postgresql.org/download.html>,
                   add e.g. "postgresql-42.2.14.jar" (use the latest version and adjust
                   the file name) to the CLASSPATH environment variable. ; also adjust the
                   connection URL according to your database name, userid and password

                 - "S" - SQLite: <https://www.sqlite.org/index.html>, an open-source RDBMS
                   implemented in C++, popular usage (e.g. in mobiles); JDBC includes
                   the SQLite engines for all operating systems(!), so only need to
                   download the JDBC driver, download page:
                   <https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/>, add e.g.
                   "sqlite-jdbc-3.32.3.2.jar" (use the latest version and adjust the
                   file name) to the CLASSPATH environment variable.

                 - "Y" - MySQL: <https://www.mysql.com/>, an open-source RDBMS implemented in
                   C++ and owned by Oracle, community RDBMS <https://dev.mysql.com/downloads/>;
                   JDBC driver look for "Connector/J" in: <https://dev.mysql.com/downloads/>,
                   pick "Platform Independent" as Operating System, download the zip-archive,
                   extract the .jar file (e.g. "mysql-connector-java-8.0.21.jar")
                   (alternate URL for JDBC: <https://www.mysql.com/products/connector/>); add
                   e.g. "mysql-connector-java-8.0.21.jar" (use the latest version and adjust
                   the file name) to the CLASSPATH environment variable. ; also adjust the
                   connection URL according to your database name, userid and password.

                 - "Z" - H2 Database: <https://h2database.com/html/main.htm>, a very fast
                   Open Source Cross Platform Db written in Java. DB can be run Embedded within 
                   the application or in Network Server mode. In-memory Databases are supported 
                   as well. Latest Stable Version is v1.4.199. There is a Windows Setup .exe program 
                   or a Zip Archive for other Platforms, such as Linux and MAC OS X. Default
                   Installations use port 8082 For Network Client Administration Console. For JDBC
                   Access Deploy the Jar File, h2-1.4.199.jar to the Java Classpath. 

   changed:    - 20061128, rvj
               - 20061201, rvj
               - 20100822, rvj
               - 20110322, rgf
               - 20140915, rgf: added DataSource access to RDBMS, which works also, when BSF4ooRexx
                                is installed in java.ext.dirs directory, which would not work with
                                the DriverManager variant in Java 1.7/7
               - 20180103, rgf: corrected HSQL statements (obviously the DataSource interface was never exercised)
               - 20200812, rgf: added MariaDB, MySQL, SQLite, adjusted hsql and PostgreSQL
               - 20200814, rgf: renamed to ".rex", fixed error in error branch
               - 20210914, tfd: add Option for H2 Embedded Java Database
   license:    Apache License 2.0
*/

signal on syntax

parse arg dbms_ori kind_ori . -- get first two arguments
parse upper arg dbms kind .   -- get first two arguments  in uppercase

-- say "... dbms="pp(dbms) "kind="pp(kind)

dbms?="ADHLPYZ"~pos(dbms)>0   -- check valid dbms
if kind="" then kind="S"      -- default to DataSource
kind?="SM"~pos(kind)>0        -- check valid kind

if \dbms? | \kind? then       -- either/both argument(s) wrong?
do
   say
   if \dbms? then say "error: database" pp(dbms_ori) "unknown"
   if \kind? then say "error: kind" pp(kind_ori) "unknown"
   say
   say "usage: rexx jdbc.rex database [kind]"
   say
   say "where 'database' is one of the letters:"
   say "    A ... MariaDB   , cf. https://downloads.mariadb.org/"
   say "    D ... Derby *)  , cf. https://db.apache.org/derby/derby_downloads.html"
   say "    H ... HSQL *)   , cf. http://hsqldb.org/"
   say "    L ... SQLite *) , cf. https://github.com/xerial/sqlite-jdbc (RDBMS contained)"
   say "    P ... PostgreSQL, cf. https://https://www.postgresql.org/"
   say "    Y ... MySQL     , cf. https://www.mysql.com/products/connector/"
   say "    Z ... H2 DB *)  , cf. ttps://h2database.com/html/download.html/"
   say "          *) ... RDBMS fully implemented in Java or contained in JDBC driver"
   say
   say "where 'kind' is optionally one of the letters:"
   say "    M ... use the classic 'java.sql.DriverManager' approach to connect"
   say "    S ... use the 'javax.sql.DataSource' approach to connect (default)"
   say
   exit -1
end

bUseDriverManager=(kind="M")     -- use "DriverManager" for connecting?
if bUseDriverManager=.true then
do
   driverMgr = bsf.loadClass("java.sql.DriverManager") -- load the DriverManager Java class
   say "... kind="pp(kind) "therefore using 'java.sql.DriverManager' for connecting ..."
end
else  -- default
   say "... kind="pp(kind) "therefore using 'javax.sql.DataSource' for connecting ..."

select
  when dbms = 'A' then        -- 'MariaDB' (a fork of MySQL): <https://mariadb.org/>
  do
     if bUseDriverManager=.true then
     do
        url="jdbc:mariadb://localhost/test_rexx_db?user=root&password=pwroot"
        statement = driverMgr~getConnection(url)~createStatement
     end
     else   -- use implemented javax.sql.DataSource interface instead
     do
        ds=.bsf~new("org.mariadb.jdbc.MariaDbDataSource")
        ds~user    ='root'       -- same as: ds~setUser('root')
        ds~password='pwroot'     -- same as: ds~setPassword('pwroot')
        ds~setDatabaseName("test_rexx_db")
        statement=ds~getConnection~createStatement
     end
  end

  when dbms = 'D' then        -- 'derby': <https://db.apache.org/derby/>
  do
     if bUseDriverManager=.true then
     do
           -- make the dbms connection and create a statement
        props = .bsf~new("java.util.Properties")  -- create instance
        props~put("user", "user1")
        props~put("password", "user1")
        statement = driverMgr~getConnection('jdbc:derby:test_rexx_db-derby;create=true',props)~createStatement
     end
     else -- use implemented javax.sql.DataSource interface instead
     do
        ds=.bsf~new("org.apache.derby.jdbc.EmbeddedDataSource")
        ds~setUser("user1")      -- alternative: ds~user    ='user1'
        ds~setPassword("user1")  -- alternative: ds~password='user1'
        ds~setCreateDatabase("create") -- Apache Derby: always create it, if it does not exist
        ds~setDatabaseName("test_rexx_db-derby")
        statement=ds~getConnection~createStatement
     end
  end

  when dbms = 'H' then        -- 'hsql': <http://hsqldb.org/>, used by OpenOffice.org since version 2.0 (fall 2005)
  do
     if bUseDriverManager=.true then
     do
           -- make the dbms connection (memory only dbms in this case) and create a statement
        statement = driverMgr~getConnection('jdbc:hsqldb:mem:test_rexx_db','sa','')~createStatement
     end
     else -- use implemented javax.sql.DataSource interface instead
     do
        ds=.bsf~new("org.hsqldb.jdbc.JDBCDataSource")    -- older DataSource name was: 'org.hsqldb.jdbc.jdbcDataSource'
        ds~user    ='sa'       -- same as: ds~setUser('sa')
        ds~password=''         -- same as: ds~setPassword('')
        ds~setDatabaseName("jdbc:hsqldb:mem:test_rexx_db")  -- cf. <https://www.javatips.net/api/org.hsqldb.jdbc.jdbcdatasource> as of 20180103
        statement=ds~getConnection~createStatement
     end
  end

  when dbms = 'L' then        -- 'SQLite': <https://www.sqlite.org/index.html>, used e.g. in mobiles
  do
     if bUseDriverManager=.true then
     do
        url="jdbc:sqlite:"   -- no database file supplied, hence memory only database!
        connection=bsf.import("java.sql.DriverManager")~getConnection(url)
        statement =connection~createStatement
     end
     else   -- use implemented javax.sql.DataSource interface instead
     do
        ds=.bsf~new('org.sqlite.SQLiteDataSource')
        ds~setDatabaseName("jdbc:sqlite")    -- memory-only database, also "jdbc:sqlite:memory"
        statement=ds~getConnection~createStatement
     end
  end

  when dbms = 'P' then        -- 'PostgreSQL': <https://www.postgresql.org/>
  do
     if bUseDriverManager=.true then
     do
           -- make the dbms connection and create a statement
        statement = driverMgr~getConnection('jdbc:postgresql:test_rexx_db','postgres','pwroot')~createStatement
     end
     else -- use implemented javax.sql.DataSource interface instead
     do
        ds=.bsf~new('org.postgresql.ds.PGSimpleDataSource')
        ds~user    ='postgres' -- same as: ds~setUser('postgres')
        ds~password='pwroot'   -- same as: ds~setPassword('pwroot')
        ds~setDatabaseName("test_rexx_db")
        statement=ds~getConnection~createStatement
     end
  end

  when dbms = 'Y' then        -- 'MySql'
  do
     if bUseDriverManager=.true then
     do
        url="jdbc:mysql://localhost/test_rexx_db?user=user&password=pwuser"
        statement = driverMgr~getConnection(url)~createStatement
     end
     else   -- use implemented javax.sql.DataSource interface instead
     do
        ds=.bsf~new("com.mysql.cj.jdbc.MysqlDataSource")
        ds~user    ='user'       -- same as: ds~setUser('user')
        ds~password='pwuser'     -- same as: ds~setPassword('pwuser')
        ds~setDatabaseName("test_rexx_db")
        statement=ds~getConnection~createStatement
     end
  end

  -- Added Generic H2 (Embedded) Database Option -- 20210914 --tfd
  -- Javadoc Reference for H2 Database API
  --   https://h2database.com/javadoc/index.html
  -- Tested With H2 Jar File: h2-1.4.199.jar
  when dbms = 'Z' then        -- 'H2': <https://h2database.com/html/main.html>
  do
     if bUseDriverManager=.true then
     do
        -- make the dbms connection and create a statement
        statement = driverMgr~getConnection('jdbc:h2:~/test_rexx_db','sa','')~createStatement
     end
     else -- use implemented javax.sql.DataSource interface instead
     do
        ds=.bsf~new('org.h2.jdbcx.JdbcDataSource')
        ds~user    ='sa' -- same as: ds~setUser('sa')
        ds~password=''   -- same as: ds~setPassword('')
        ds~setUrl('jdbc:h2:~/test_rexx_db')
        statement=ds~getConnection~createStatement
     end
  end

  otherwise nop
end

say "... using JDBC statement object:" pp(statement)
say

call dropTable statement, "test" -- make sure table gets dropped, if it exists (from a previous run)
   -- create the table
statement~executeUpdate("CREATE TABLE test( name char(42), place char(42))")

   -- insert some rows
statement~executeUpdate("INSERT INTO test (name, place) VALUES('Lee Peedin',        'Wallace, NC, USA' )")
statement~executeUpdate("INSERT INTO test (name, place) VALUES('Jeff Hennick',      'Fort Myers, FL, USA'  )")
statement~executeUpdate("INSERT INTO test (name, place) VALUES('Rene Jansen',       'Amsterdam, Netherlands')")
statement~executeUpdate("INSERT INTO test (name, place) VALUES('Rony G. Flatscher', 'Vienna, Austria'   )")
statement~executeUpdate("INSERT INTO test (name, place) VALUES('Tony Dycks', 'Los Angeles County, CA, USA' )")

   -- select database content: specify query and execute to get result set
rs = statement~executeQuery('select name, place from test order by name')
do while rs~next              -- iterate over rows in result set
  say pp(rs~getString("name")~strip) "from" pp(rs~getString("place")~strip)
end
say

   -- use an aggregate query, defining an alias name (for retrieval by column name)
rs = statement~executeQuery('select count(*) as Total_Fans from test')
   -- calculate total
do while rs~next
   -- the following statement uses the alias name to address the column
  say "BSF4ooRexx has probably at least" rs~getString("Total_Fans") "fans! (Using alias name 'Total_Fans' as argument.)"
  int1=box('int',1)  -- box as an int (foce using Java method expecting an 'int')
  say "BSF4ooRexx has probably at least" rs~getString(int1) "fans! (Using positional argument.)"
end
exit

syntax:
   co=condition('o')       -- get condition object
   exc=co~additional[2]    -- get Java exception object, if any
   if exc~isA(.bsf) then   -- a Java exception?
   do
      say "/////// Java-exceptions:"
      say ppJavaExceptionChain(co)  -- new in BSF.CLS since 2020-08-12
      say "\\\\\\\"
   end
   raise propagate         -- let ooRexx handle the exception as well, pointing out the error line


::requires bsf.cls            -- get the Java support

::routine dropTable           -- drop a table
  use arg stmt, tableName
  signal on syntax            -- catch exception, if table does not exist yet (very first run)
  stmt~executeUpdate("DROP TABLE" tableName)
syntax:
  return
