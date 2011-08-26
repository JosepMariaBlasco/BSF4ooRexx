conn=.dbus~session 	/* get connection to session dbus	*/

    /* define message arguments */
busName       ="org.freedesktop.Notifications"
objectName    ="/org/freedesktop/Notifications"
interfaceName ="org.freedesktop.Notifications"
memberName    ="Notify"
replySignature="u" 	       /* uint32 */
callSignature ="susssasa{sv}i" /* string,uint32,string,string,string,array of string,dict,int32 */

id=conn~message("call",busName,objectName,interfaceName,memberName,replySignature,callSignature, -
                "An ooRexx App", , "oorexx", "ooRexx Demo", "Hello, my beloved world!", , , -1)

::requires "dbus.cls"          /* get DBus support  */

