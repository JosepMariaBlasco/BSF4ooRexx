#!/usr/bin/rexx
/* File:        CLR.CLS
 * Description: Library for making Microsoft's .NET Framework available in ooRexx: .NET classes and 
 *              objects appear as ooRexx classes and objects and one can send ooRexx messages to them.
 *              Requires BSF4ooRexx and jni4net.
 */
 
.local~clr.bridge = BSF.import("net.sf.jni4net.Bridge")  -- get jni4net support 
.clr.bridge~setVerbose(.false)  -- .true for debug output

.clr.bridge~bsf.dispatch("init")  -- initialize jni4net support

.local~clr.assembly = BSF.import("system.reflection.Assembly")

ooRexxAssembly = .clr.assembly~LoadWithPartialName("oorexx.net")  -- get additional .NET proxies
.clr.bridge~RegisterAssembly(ooRexxAssembly)

CALL clr.initAssemblies

.local~clr.system.array = BSF.import("system.Array")  -- preload frequently used classes
.local~clr.system.enum = BSF.import("system.Enum")
.local~clr.system.object = BSF.import("system.Object")
.local~clr.system.type = BSF.import("system.Type")

.CLRLogger~setLevel("OFF")  -- turn off logging by default ("OFF")

::REQUIRES BSF.CLS  -- get BSF4ooRexx support

/* Routine:     clr.initAssemblies
 * Description: Stores class names defined in commonly used .NET assemblies ("mscorlib" and "System") 
 *              in the local directory .clr.assemblyName for fast and easy lookup.
 */
::ROUTINE clr.initAssemblies PRIVATE
  
  .local~clr.assemblyName = .directory~new
  
  CALL clr.addAssembly "mscorlib"
  CALL clr.addAssembly "System"

/* Routine:     clr.addAssembly
 * Description: Retrieves class names defined in given assembly name and adds them to the local 
 *              directory .clr.assemblyName for fast and easy lookup.
 * Arguments:   - assemblyName: the assembly name which is to be searched for classes
 */
::ROUTINE clr.addAssembly PUBLIC
  PARSE ARG assemblyName
  
  assemblyTypes = .clr.assembly~LoadWithPartialName(assemblyName)~GetExportedTypes  -- retrieve classes defined in assembly with given name
  
  DO i = 1 TO assemblyTypes~size
    .clr.assemblyName~setEntry(assemblyTypes[i]~getFullName, assemblyName)
  END

/* Routine:     clr.extractAssemblyName
 * Description: Tries to determine the assembly name of the given class by splitting it and removing 
 *              the last part. Only works for classes where the fully qualified name is just one  
 *              "level" above its assembly name. If the class is in a different assembly, it needs to
 *              be added to the known assemblies by calling clr.addAssembly.
 * Arguments:   - className: the (fully qualified) class name of a .NET class
 * Returns:     the (supposed) assembly name
 */
::ROUTINE clr.extractAssemblyName PRIVATE
  PARSE ARG className
  
  split = LASTPOS(".", className)
  assemblyName = SUBSTR(className, 1, (split-1))  -- extract assembly name from fully qualified class name 
  
  RETURN assemblyName
  
/* Routine:     clr.findAssemblyName
 * Description: Determines the assembly name for the given (fully qualified) class name. If the class 
 *              can not be found in the known assemblies (as instantiated by clr.initAssemblies), the 
 *              (supposed) assembly name is returned as determined by clr.extractAssemblyName.
 * Arguments:   - className: the (fully qualified) class name of a .NET class
 * Returns:     the (probably supposed) assembly name
 */
::ROUTINE clr.findAssemblyName PRIVATE
  PARSE ARG className
    
  assemblyName = .clr.assemblyName~entry(className)  -- lookup assembly name in known assemblies
  
  IF assemblyName <> .nil THEN
    RETURN assemblyName
  ELSE
    RETURN clr.extractAssemblyName(className)
 
/* Routine:     clr.import
 * Description: Creates and returns an instance of CLRClass which provides a reference to a static 
 *              .NET class.
 * Arguments:   - className: the (fully qualified) class name of the static .NET class
 * Returns:     a CLRClass referencing the given .NET class
 */
::ROUTINE clr.import PUBLIC
  PARSE ARG className
  
  clrClass = .CLRClass~new(className)

  RETURN clrClass

/* Routine:     clr.createEventHandler
 * Description: Makes given ooRexx class inherit from .NET class "System.EventHandler" to enable it to
 *              receive events triggered by .NET objects.
 * Arguments:   - rexxEventHandler: the ooRexx class which handles the event
 *              - rexxData: optional argument to be included in the proxies slotDir~userdata value
 * Returns:     a Java proxy to the given ooRexx class
 */
::ROUTINE clr.createEventHandler PUBLIC
  USE ARG rexxEventHandler, rexxData = .nil
    
  prxEventHandler = BSFCreateRexxProxy(rexxEventHandler, rexxData)
  prxClass = bsf.createProxyClass("system.EventHandler")
  eventHandler = prxClass~new(prxEventHandler)
  
  RETURN eventHandler

/* Routine:     clr.createArray
 * Description: Creates and returns a .NET array of given class and capacity wrapped in a Java proxy.
 * Arguments:   - className: the type of the objects to be stored in the array
 *              - capacity: the capacity of the array
 * Returns:     a .NET array of the given class wrapped in a Java proxy
 */
::ROUTINE clr.createArray PUBLIC
  PARSE ARG className, capacity
  
  RETURN .clr.system.array~CreateInstance(.clr.system.type~GetType(className), capacity)

/* Routine:     clr.wrap
 * Description: Processes received parameter "param" depending on its type. Always returns a CLR.
 * Arguments:   - param: any string/object/instance/other which is supposed to be (in) a CLR
 * Returns:     a CLR instance wrapping "param"
 */
::ROUTINE clr.wrap PRIVATE
  USE ARG param
  
  IF param~isA(.CLR) THEN  -- if "param" already is a CLR, nothing is to be done
  DO
    .CLRLogger~trace("clr.wrap: returning unchanged CLR")
    RETURN param
  END
  ELSE IF param~isA(.string) THEN  -- if "param" is a string...
  DO
    IF VERIFY(param, "0123456789") = 0 THEN  -- return "System.Int32" if it contains only digits
    DO
      .CLRLogger~trace("clr.wrap: new System.Int32 CLR")
      RETURN .clr~new("System.Int32", param)  
    END
    ELSE  -- return "System.String" elsewise
    DO
      .CLRLogger~trace("clr.wrap: new System.String CLR")
      RETURN .clr~new("System.String", param)
    END
  END
  ELSE  -- return new CLR, let constructor of CLR handle "param"
  DO
    .CLRLogger~trace("clr.wrap: returning new CLR")
    RETURN .clr~new(param)
  END

/* Routine:     clr.findMatchingMethod
 * Description: Searches given "Type" object for given method name. 
 * Arguments:   - clrType: a .NET "System.Type" defining its available methods
 *              - methodName: the name of the method to be searched for
 * Returns:     case sensitive name of the method or .nil if it could not be found
 */
::ROUTINE clr.findMatchingMethod PRIVATE
  USE ARG clrType, methodName
  
  .CLRLogger~trace("Searching method name for" methodName "in" clrType)
  
  typeMethods = clrType~GetMethods  -- retrieve methods available for this "Type"
  typeMethodsCount = typeMethods~size
  
  DO i = 1 TO typeMethodsCount
    IF typeMethods[i]~getName~caselessEquals(methodName) THEN  -- case insensitive comparison
      RETURN typeMethods[i]~getName
  END
  
  RETURN .nil  -- return .nil only if no matching method was found

/* Routine:     clr.findMatchingProperty
 * Description: Searches given "Type" object for given property name. 
 * Arguments:   - clrType: a .NET "System.Type" defining its available properties
 *              - propertyName: the name of the property to be searched for
 * Returns:     case sensitive name of the property or .nil if it could not be found
 */
::ROUTINE clr.findMatchingProperty PRIVATE
  USE ARG clrType, propertyName
  
  .CLRLogger~trace("Searching property name for" propertyName "in" clrType)
  
  typeProperties = clrType~GetProperties  -- retrieve properties available for this "Type"
  typePropertiesCount = typeProperties~size
    
  DO i = 1 TO typePropertiesCount
    IF typeProperties[i]~getName~caselessEquals(propertyName) THEN  -- case insensitive comparison
      RETURN typeProperties[i]~getName
  END
  
  RETURN .nil  -- return .nil only if no matching property was found
  
/* Routine:     clr.findMatchingEvent
 * Description: Searches given "Type" object for given event name. 
 * Arguments:   - clrType: a .NET "System.Type" defining its available events
 *              - eventName: the name of the event to be searched for
 * Returns:     case sensitive name of the event or .nil if it could not be found
 */
::ROUTINE clr.findMatchingEvent PRIVATE
  USE ARG clrType, eventName
  
  .CLRLogger~trace("Searching event name for" eventName "in" clrType)
  
  typeEvents = clrType~GetEvents  -- retrieve events available for this "Type"
  typeEventsCount = typeEvents~size
    
  DO i = 1 TO typeEventsCount
    IF typeEvents[i]~getName~caselessEquals(eventName) THEN  -- case insensitive comparison
      RETURN typeEvents[i]~getName
  END
  
  RETURN .nil  -- return .nil only if no matching event was found

/* Class:       CLR
 * Description: Wraps a .NET proxy and handles initialization and method calls to it.
 */
::CLASS CLR PUBLIC

  /* Method:      init
   * Description: Constructor which creates an instance of CLR based on the supplied class name and
   *              parameters.
   * Arguments:   - className: name of the .NET class to be instantiated or a BSF proxy to be wrapped
   *              - param: first parameter for the class, default .nil
   *              - ...: additional parameters
   * Returns:     the freshly created CLR
   */
  ::METHOD init PUBLIC
    EXPOSE clrInstance clrType
    USE ARG className, param = .nil, ...
    
    .CLRLogger~trace("Creating new CLR instance of" className "with parameters" param)
    
    IF className~isA(.string) THEN
    DO
      assemblyName = clr.findAssemblyName(className)  -- find assembly of (fully qualified) class name

      SELECT
        WHEN className = "System.Boolean" THEN  -- special handling for boolean parameters and instantiation
        DO
          IF param = .true | param~caselessEquals("true") THEN param = "true"
          ELSE param = "false"
          
          clrInstance = .clr.assembly~LoadWithPartialName(assemblyName)~CreateInstance(className)
          clrInstance = clrInstance~GetType~GetMethod("Parse", bsf.createJavaArrayOf(.clr.system.type, .clr.system.type~GetType("System.String")))~Invoke(clrInstance, bsf.createJavaArrayOf(.clr.system.object, .clr.bridge~convert(param)))
        END
        
        WHEN className = "System.String" THEN  -- special handling for string parameters and instantiation
        DO
          clrInstance = .bsf~new("system.String", param)
        END
        
        WHEN className = "System.Int32" THEN  -- special handling for integer parameters and instantiation
        DO
          clrInstance = .clr.assembly~LoadWithPartialName(assemblyName)~CreateInstance(className)
          clrType = clrInstance~GetType
          clrInstance = clrType~GetMethod("Parse", bsf.createJavaArrayOf(.clr.system.type, .clr.system.type~GetType("System.String")))~Invoke(clrInstance, bsf.createJavaArrayOf(.clr.system.object, .clr.bridge~convert(param)))
        END
        
        OTHERWISE  -- all other classes can be done in an unified way
        DO
          argCount = arg() - 1
          
          IF argCount = 0 THEN  -- no arguments, can use default constructor
            clrInstance = .clr.assembly~LoadWithPartialName(assemblyName)~CreateInstance(className)  
          ELSE  -- arguments need to be wrapped in an array to be passed to appropriate constructor
          DO
            argsList = bsf.createJavaArray(.clr.system.object, argCount)

            DO i = 1 TO argCount
              .CLRLogger~trace("Parsing argument" arg(i+1))
              argument = clr.wrap(arg(i+1))
              .CLRLogger~trace("Parsed argument" argument argument~clr.getType argument~clr.getInstance)
              argsList[i] = argument~clr.getInstance
            END      
            
            clrInstance = .clr.assembly~LoadWithPartialName(assemblyName)~CreateInstance(className, .false, .nil, .nil, argsList, .nil, .nil)
          END
        END
      END
         
      clrType = clrInstance~GetType
    END
    ELSE  -- supplied argument is (supposed to be) a BSF_REFERENCE
    DO
      clrInstance = className
      clrType = className~GetType
    END
    
    .CLRLogger~trace("Created CLR instance of" className "with clrInstance" clrInstance "and clrType" clrType)
    
  /* Method:      unknown
   * Description: Catches every message sent to this CLR and determines how it is to be treated.
   * Arguments:   - command: the message sent to this CLR
   *              - args: the arguments of the message
   * Returns:     depending on the command
   */
  ::METHOD unknown PUBLIC
    EXPOSE clrInstance clrType
    USE ARG command, args
        
    IF RIGHT(command,1) = "=" THEN  -- check if it is an assignment
    DO
      IF args[1]~isA(.CLREvent) THEN  -- check passed argument, might already be handled ("+=")
        RETURN args[1]
      
      PARSE VAR command property "=" .  -- extract property name (without "=")
      
      propertyName = clr.findMatchingProperty(clrType, property)  -- try to find given property in "Type"
      
      IF args~items = 1 THEN
        RETURN self~clr.setPropertyValue(propertyName, args[1])  -- set property value to given argument
    END
    ELSE  -- not an assignment
    DO
      methodName = clr.findMatchingMethod(clrType, command)  -- try to find given method in "Type"
      
      IF methodName <> .nil THEN  -- aimed for a method
      DO
        typeList = bsf.createJavaArray(.clr.system.type, args~items)
        argsList = bsf.createJavaArray(.clr.system.object, args~items)
              
        IF args~items > 0 THEN  -- arguments need to be wrapped in an array to be passed to method
        DO i = 1 TO args~items
          .CLRLogger~trace("Parsing argument" args[i]~class)
          argument = clr.wrap(args[i])
          .CLRLogger~trace("Parsed argument" argument argument~clr.getType argument~clr.getInstance)
          typeList[i] = argument~clr.getType
          argsList[i] = argument~clr.getInstance
        END      
                
        result = clrInstance~GetType~GetMethod(methodName, typeList)~Invoke(clrInstance, argsList)  -- invoke given method with supplied parameters
        
        IF result <> .nil THEN
          RETURN .clr~new(result)
        ELSE
          RETURN .nil
      END
      ELSE  -- aimed for another member
      DO			
        propertyName = clr.findMatchingProperty(clrType, command)  -- try to find given property in "Type"
				
        IF propertyName <> .nil THEN  -- aimed for a property
        DO
          propertyInfo = clrInstance~GetType~GetProperty(propertyName)
				
          RETURN .clr~new(propertyInfo~GetValue(clrInstance, .nil))  -- return value of property
        END
        ELSE  -- aimed for an event
        DO
          eventName = clr.findMatchingEvent(clrType, command)  -- try to find given event in "Type"
          
          RETURN .CLREvent~new(self, eventName)  -- return new CLREvent
        END
      END
    END
    
  /* Method:      clr.dispatch
   * Description: Directly forwards a message to the "unknown" method of this CLR. This is needed if a
   *              method on .NET side is to be called which is named like a method of this CLR object.
   *              Examples are "start" or "init" (inherited from .Object).
   * Arguments:   - methodName: name of the method to be invoked
   * Returns:     the return value of the unknown method
   */
  ::METHOD clr.dispatch PUBLIC
    PARSE ARG methodName
    
    RETURN self~unknown(methodName, arg(2, 'A'))  -- calls unknown method

  /* Method:      string
   * Description: Overrides "string" method of .Object to enable certain CLR instances to output their
   *              actual content instead of their class name.
   * Returns:     the value of the contained proxy
   */    
  ::METHOD string PUBLIC
    RETURN self~clr.getInstance~toString
  
  /* Method:      clr.getType
   * Description: Returns the "System.Type" of the .NET object wrapped in this CLR.
   * Returns:     the "Type" of the .NET object in this CLR
   */    
  ::METHOD clr.getType PUBLIC
    EXPOSE clrType
    RETURN clrType

  /* Method:      clr.getInstance
   * Description: Returns the actual instance of the .NET object wrapped in this CLR.
   * Returns:     the actual instance of the .NET object in this CLR
   */    
  ::METHOD clr.getInstance PUBLIC
    EXPOSE clrInstance
    RETURN clrInstance

  /* Method:      clr.setPropertyValue
   * Description: Sets the specified property to the supplied value in the .NET object wrapped in  
   *              this CLR.
   * Arguments:   - propertyName: the name of the property to be set
   *              - propertyValue: the value the property is to be set to
   * Returns:     self
   */
  ::METHOD clr.setPropertyValue PRIVATE
    EXPOSE clrInstance clrType
    USE ARG propertyName, propertyValue
    
    .CLRLogger~trace("Setting property" propertyName "to" propertyValue)
    
    IF propertyValue~isA(.CLR) THEN  -- if supplied value is a CLR, extract its wrapped instance
      propertyValue = propertyValue~clr.getInstance
    ELSE  -- else handle value according to its (expected) type
    DO
      propertyType = clrType~GetProperty(propertyName)~GetPropertyType() -- get expected datatype for this property
      
      IF propertyType~isEnum = .true THEN  -- special handling for enums
        propertyValue = .clr.system.enum~Parse(propertyType, propertyValue, .true)
      ELSE
        propertyValue = .clr~new(propertyType~toString, propertyValue)~clr.getInstance
    END
          
    clrType~GetProperty(propertyName)~SetValue(clrInstance, propertyValue, .nil)  -- actually set value
    
    RETURN self
      
/* Class:       CLRClass
 * Description: Wraps a .NET proxy of a static class and handles its initialization and method calls.
 */
::CLASS CLRClass PRIVATE

  /* Method:      init
   * Description: Constructor which creates an instance of CLRClass based on the supplied class name.
   * Arguments:   - className: name of the static .NET class to be referenced
   * Returns:     the freshly created CLRClass
   */
  ::METHOD init PUBLIC
    EXPOSE clrClass clrType
    USE ARG className
        
    .CLRLogger~trace("Creating new CLRClass instance of" className)
      
    assemblyName = clr.findAssemblyName(className)  -- find assembly of (fully qualified) class name
      
    clrClass = .clr.assembly~LoadWithPartialName(assemblyName)
    clrType = clrClass~GetType(className)
    
    .CLRLogger~trace("Created CLRClass instance of" className "with clrClass" clrClass "and clrType" clrType)

  /* Method:      unknown
   * Description: Catches every message sent to this CLRClass and determines how it is to be treated.
   * Arguments:   - command: the message sent to this CLR
   *              - args: the arguments of the message
   * Returns:     depending on the command
   */
  ::METHOD unknown PUBLIC
    EXPOSE clrClass clrType
    USE ARG command, args
   
    IF RIGHT(command,1) = "=" THEN  -- check if it is an assignment
    DO
      IF args[1]~isA(.CLREvent) THEN  -- check passed argument, might already be handled ("+=")
        RETURN args[1]
      
      PARSE VAR command property "=" .  -- extract property name (without "=")
      
      propertyName = clr.findMatchingProperty(clrType, property)  -- try to find given property in "Type"
      
      IF args~items = 1 THEN
        RETURN self~clr.setPropertyValue(propertyName, args[1])  -- set property value to given argument
    END
    ELSE
    DO
      methodName = clr.findMatchingMethod(clrType, command)  -- try to find given method in "Type"
      
      IF methodName <> .nil THEN  -- aimed for a method
      DO
        typeList = bsf.createJavaArray(.clr.system.type, args~items)
        argsList = bsf.createJavaArray(.clr.system.object, args~items)
              
        IF args~items > 0 THEN  -- arguments need to be wrapped in an array to be passed to method
        DO i = 1 TO args~items
          .CLRLogger~trace("Parsing argument" args[i]~class)
          argument = clr.wrap(args[i])
          .CLRLogger~trace("Parsed argument" argument argument~clr.getType argument~clr.getInstance)
          typeList[i] = argument~clr.getType
          argsList[i] = argument~clr.getInstance
        END      
                
        result = clrType~GetMethod(methodName, typeList)~Invoke(.nil, argsList)  -- invoke given method with supplied parameters
        
        IF result <> .nil THEN
          RETURN .clr~new(result)
        ELSE
          RETURN .nil
      END
      ELSE  -- aimed for another member
      DO			
        propertyName = clr.findMatchingProperty(clrType, command)  -- try to find given property in "Type"
        
        IF propertyName <> .nil THEN  -- aimed for a property
        DO
          propertyInfo = clrType~GetProperty(propertyName)
        
          RETURN .clr~new(propertyInfo~GetValue(clrType, .nil))  -- return value of property
        END
        ELSE  -- aimed for an event
        DO
          eventName = clr.findMatchingEvent(clrType, command)  -- try to find given event in "Type"
          
          RETURN .CLREvent~new(self, eventName)  -- return new CLREvent
        END
      END
    END

/* Class:       CLREvent
 * Description: Handles assignment and deassignment of event handlers.
 */
::CLASS CLREvent PRIVATE

  /* Method:      init
   * Description: Constructor which saves the passed arguments in the class.
   * Arguments:   - clr: the CLR which wraps the object the event is registered to
   *              - eventName: the name of the event
   */
  ::METHOD init PUBLIC
    EXPOSE clr eventName
    USE ARG clr, eventName

  /* Method:      "+"
   * Description: Is called upon assignment of an event handler with "+=".
   * Arguments:   - eventHandler: the event handler created by clr.createEventHandler
   * Returns:     self
   */
  ::METHOD "+" PUBLIC
    EXPOSE clr eventName
    USE ARG eventHandler
        
    .CLRLogger~trace("Adding EventHandler " eventHandler "to" clr "(Event:" eventName ")")
    
    addMethod = "add_" || eventName  -- jni4net reflects the method to add an event handler with "add_" and the event name
    INTERPRET "clr~" || addMethod || "(eventHandler)"  -- let ooRexx carry out the statement in the string
    
    RETURN self

  /* Method:      "-"
   * Description: Is called upon deassignment of an event handler with "-=".
   * Arguments:   - eventHandler: the event handler created by clr.createEventHandler
   * Returns:     self
   */
  ::METHOD "-" PUBLIC
    EXPOSE clr eventName
    USE ARG eventHandler
    
    .CLRLogger~trace("Removing EventHandler " eventHandler "from" clr "(Event:" eventName ")")
    
    removeMethod = "remove_" || eventName  -- jni4net reflects the method to remove an event handler with "remove_" and the event name
    INTERPRET "clr~" || removeMethod || "(eventHandler)"  -- let ooRexx carry out the statement in the string
    
    RETURN self  
    
/* Class:       CLRThread
 * Description: Provides the environment to start threads interacting with .NET objects. To use it, 
 *              the ooRexx class needs to subclass CLRThread and override the "run" method. It must 
 *              then be started with the "start" method to actually start a new thread.
 */
::CLASS CLRThread PUBLIC

  /* Method:      start
   * Description: Creates a new thread which is able to flawlessly interact with .NET objects in 
   *              different threads. Must be called to execute the "run" method in a new thread.
   * Arguments:   - rexxData: optional argument to be included in the proxies slotDir~userdata value
   */
  ::METHOD start PUBLIC
    USE ARG rexxData = .nil
    rexxRunnableProxy = BsfCreateRexxProxy(self, rexxData, "java.lang.Runnable")
    rexxThread = .bsf~new("java.lang.Thread", rexxRunnableProxy)
    rexxThread~bsf.dispatch("start")
  
  /* Method:      run
   * Description: Needs to be overridden by specialising ooRexx class.
   */
  ::METHOD run ABSTRACT
    
/* Class:       CLRLogger
 * Description: Provides simple logging functionality within CLR.CLS and for programs using it. 
 *              Different log levels are defined and can be used to output all kinds of information  
 *              at runtime or for debugging purposes. The kind of messages displayed can be limited  
 *              by specifying the intended log level.
 */
::CLASS CLRLogger PUBLIC

  /* Method:      init
   * Description: Constructor which defines the available log levels.
   */
  ::METHOD init PUBLIC CLASS
    EXPOSE logLevels
    
    logLevels = .directory~new
  
    logLevels["OFF"] = 70
    logLevels["FATAL"] = 60
    logLevels["ERROR"] = 50
    logLevels["WARN"] = 40
    logLevels["INFO"] = 30
    logLevels["DEBUG"] = 20
    logLevels["TRACE"] = 10

  /* Method:      setLevel
   * Description: Sets the log level limit. Only messages of this type (and above) will be outputted. 
   *              For example, a log level limit of "ERROR" will only output messages of type   
   *              "ERROR" and "FATAL".
   * Arguments:   - desiredLevel: the log level limit
   */
  ::METHOD setLevel PUBLIC CLASS
    EXPOSE logLevel logLevels
    PARSE ARG desiredLevel
    
    IF logLevels[desiredLevel] <> .nil THEN  -- check if supplied argument is a valid log level
      logLevel = logLevels[desiredLevel]
    ELSE
      logLevel = logLevels["OFF"]
    
  /* Method:      unknown
   * Description: Catches log messages. For example, a log output of type "INFO" can be invoked by 
   *              .CLRLogger~info("log output"). Output is given (or not) depending on the set level
   *              limit.
   * Arguments:   - levelName: the log level this message belongs to
   *              - args: the message itself
   */
  ::METHOD unknown PUBLIC CLASS
    EXPOSE logLevel logLevels
    USE ARG levelName, args
    
    IF args~ITEMS > 0 & logLevels[levelName] <> .nil THEN  -- check if there is a message and if the given log level is defined
    DO
      message = args[1]
      
      IF logLevels[levelName] >= logLevel THEN  -- only process message if invoked log level is greater or equal log level limit
        .CLRLogger~output(levelName, message)
    END
  
  /* Method:      output
   * Description: Provides formatting and actual output for a log message.
   * Arguments:   - logLevel: the log level this message belongs to
   *              - message: the message itself
   */
  ::METHOD output PRIVATE CLASS
    PARSE ARG logLevel, message
    
    SAY "[" || .dateTime~new || " | " ||  LEFT(logLevel, 5) || "] :: " || message  -- default output with date, time, log level (always five characters for consistent columns) and message