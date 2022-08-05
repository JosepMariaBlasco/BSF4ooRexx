#!/usr/bin/env rexx
/*
    author:    Rony G. Flatscher, copyright 2008
    date:      2007-12-29, 2008-01-04
    name:      UNO_ReflectionUtil.cls
    purpose:   ease the creation of reflective ooRexx utilities for OOo
    version:   1.0.1
    changed:
               2008-02-20, --rgf, UNO_TYPEDEF class referred mistakingly to an attribute "datatype"
               2008-02-23, --rgf, changed UNO_PROPERTY toString(); added "definedBy"
                                  attribute for "UNO_ATTRIBUTE", "UNO_METHOD", and
                                  "UNO_PROPERTY", also their "fullyQualifiedName"
                                  attribute now are built using "containing"-IDL-name
                                  and the (unqualified) "name" of the member
               2008-03-22, ---rgf, rename "datatype" to "unoTypeName" in members of
                                  constants and enums
               2008-03-24, ---rgf, added "fullyQualifiedName" to member classes (made sure
                                   that UNO_INTERFACE, UNO_STRUCT and UNO_ENUM are truly fully
                                   qualified, otherwise memberName is assigned)
               2008-04-04, ---rgf, added "parent" to member objects, i.e. the container IDL definition
               2008-08-22, ---rgf, now always calls "uno.cls", otherwise programs using this
                                   module may inadvertently cause that this module does not get
                                   access to BSF.CLS and UNO.CLS definitions (this problem will
                                   go away for good with the upcoming ooRexx 4.0 where the requires
                                   handling removes its awkward corners)
               2008-08-28, ---rgf, added method "toJavaSyntax" to "UNO.METHOD" class
               2008-09-13, ---rgf, - corrected parsing error in UNO_SERVICE constructor, which
                                     caused the first member definition to be skipped
                                   - added an attribute "Optionality" to UNO_SERVICE
                                     members of type "UNO_SERVICE" and "UNO_INTERFACE"
               2009-02-20, ---rgf, - UNO_METHOD.toJavaSyntax-method:
                                     changed one TAB before the exception list to
                                     three blanks to allow two exceptions to be placed
                                     in one line in the create_UNO_API_info.rxo tool

               2009-02-21, ---rgf, - renamed to "UNO_ReflectionUtil.cls"
               2010-06-05, ---rgf, - removing check for minimum ooRexx version (already done in BSF.CLS)
                                   - changed version number to 1.0.1
                                   - experimental: changed "call UNO.CLS" to ":: requires UNO.CLS", such
                                     that an existing package for "UNO.CLS" gets reused; possible
                                     problem: if all instances of OOo went away then the cached
                                     UNO objects proxying OOo objects may become invalid and cause
                                     runtime errors, if (re-)used thereafter

               checking for ooRexx 4.0.1 (relying on 4.0 requires-processing,
                                     but needing 4.0.1 for BSF4ooRexx anyway), now requiring
                                     "UNO.CLS" such that this utility can be used without any
                                     other prerequisites

    last svn check-in: $Revision$ $Author$ $Date$

    needs:     ooRexx 4.0.1 or greater, UNO.CLS
    license:   choice of LGPL 3.0 or ASF 2.0

*/

-- call UNO.CLS         -- this will create a new package for UNO.CLS

   -- set up "classDir": maps UNO-types to respective class objects
dir=.uno_definition~classDir
   dir["UNO_CONSTANTS"]=.UNO_CONSTANTS
   dir["UNO_ENUM"     ]=.UNO_ENUM
   dir["UNO_EXCEPTION"]=.UNO_EXCEPTION
   dir["UNO_INTERFACE"]=.UNO_INTERFACE
   dir["UNO_MODULE"   ]=.UNO_MODULE
   dir["UNO_SERVICE"  ]=.UNO_SERVICE
   dir["UNO_SINGLETON"]=.UNO_SINGLETON
   dir["UNO_STRUCT"   ]=.UNO_STRUCT
   dir["UNO_TYPEDEF"  ]=.UNO_TYPEDEF


::requires UNO.CLS      -- get UNO/OOo support

/* Expects a string-encoded definition of a UNO type, i.e. the result
   of "uno.getDefinition", and returns a reflection object of the appropriate
   type where all encoded fields are broken up and assigned to attributes.
*/
::routine createReflectionObject public
  parse arg unoTypeName "|" 1 unoDefinition
   -- get appropriate class object, create the respective reflexion object and return it
  return .uno_definition~classDir[unoTypeName]~new(unoDefinition)



/* ========================================================================= */
::class "UNO_Definition" public inherit Comparable

::method init
  expose attributeList
  attributeList=.list~of("unoTypeName", "name", "fullyQualifiedName", "definition")


::attribute unoTypeName    -- e.g. "UNO_ATTRIBUTE"
::attribute definition     -- encoded string

::attribute name get       -- (simple/unqualified) name
::attribute name set       -- (simple/unqualified) name
  expose fullyQualifiedName name
  use arg name             -- save name
  if \var('fullyQualifiedName') then   -- not set, default to "name"
     fullyQualifiedName=name

::attribute fullyQualifiedName

::attribute attributeList  -- contains the list of attribute names available


::method    compareTo      -- implement Comparable's method for sorting purposes
  expose name
  use arg other
  return name~caselessCompareTo(other~name)

::method toString
  expose unoTypeName name fullyQualifiedName definition
  return name"|"unoTypeName

  -- return name"^"fullyQualifiedName"^"unoTypeName


::method init class
  expose classDir
  classDir=.directory~new

::attribute classDir class


/* ========================================================================= */
::class "UNO_DefinitionWithMembers" subclass UNO_Definition public
::method init

  forward class (super)
  self~attributeList~~append("definitions")~~append("definitionsRaw")

::attribute definitions          -- relation containing the definition objects
::attribute definitionsRaw       -- array of encoded constant values ("memberName|value")

::method toString
  expose definitions
  return self~toString:super"|#items="definitions~items


/* ========================================================================= */
::class UNO_ATTRIBUTE   subclass UNO_Definition public
::method init
  expose readOnly datatype  definedBy

  forward class (super) continue
  parse arg self~definition, definedBy
  parse arg self~name "|" self~unoTypeName "|" readOnly "|" datatype
  self~attributeList~~append("readOnly")~~append("dataType")~~append("definedBy")
  self~fullyQualifiedName=definedBy"."self~name

::attribute readOnly       -- readonly ?
::attribute datatype       -- datatype
::attribute definedBy      -- remember where defined


::attribute string get                 -- string value
   expose readonly datatype
   return "an" self~class~id"-["self~name"|"datatype"|"readonly"]"


::method toString
  expose readOnly dataType
  return self~toString:super"|"readOnly"|"dataType


/* ========================================================================= */
::class UNO_CONSTANTS    subclass UNO_DefinitionWithMembers  public
::method init

  forward class (super) continue
  parse arg self~definition
  parse arg self~unoTypeName "|" fullyQualifiedName rest

  self~fullyQualifiedName=fullyQualifiedName
  pos=fullyQualifiedName~lastpos(".")  -- get last dot
  if pos=0 then self~name=fullyQualifiedName
           else self~name=fullyQualifiedName~substr(pos+1)

  self~definitionsRaw=rest~makearray(" ")   -- create an array of "memberName|value|datatype"
  self~definitions=.relation~new   -- relation
                              -- <com.sun.star.packages.zip.ZipConstants> defines
                              -- different types for individual constants!

  do item over self~definitionsRaw -- parse
     parse var item name "|" value "|" type
     self~definitions[name]=.constantValue~new(self, name,value,type)  -- save constant
  end




/* --------------------------------------------------------- */
::class constantValue inherit comparable
::method init
  expose attributeList containedIn parent name value unoTypeName fullyQualifiedName -- establish direct access to attributes (object variables)

  use strict arg parent, name, value, unoTypeName -- assign arguments to attributes
  fullyQualifiedName=name     -- rgf, added 2008-03-24
  attributeList=.list~of("parent", "fullyQualifiedName", "name", "value", "unoTypeName")

::attribute parent
::attribute unoTypeName
::attribute containedIn
::attribute fullyQualifiedName
::attribute name
::attribute value
::attribute string get                 -- string value
   expose name value unoTypeName
   return "a" self~class~id"-["name"|"value"|"unoTypeName"]"

::attribute attributeList

   -- default sorting order: by value
::method compareTo
  expose name
  use arg other

/*
  self =self~value
  other=other~value
  numeric digits 20
  if self~datatype("N"), other~datatype("N") then
     return (self-other)~sign -- returns -1 (self<other), +1 (self>other), 0 (self=other)
*/
  return name~caselessCompareTo(other~name)-- use the String's "compareTo"

/* ========================================================================= */
::class UNO_ENUM        subclass UNO_DefinitionWithMembers  public
::method init
  expose defaultValue

  forward class (super) continue
  parse arg self~definition
  parse arg self~unoTypeName "|" fullyQualifiedName "|" defaultValue rest
  self~fullyQualifiedName=fullyQualifiedName

  datatype="UNO_LONG"            -- enums are always of type UNO_LONG !

  pos=fullyQualifiedName~lastpos(".")  -- get last dot
  if pos=0 then self~name=fullyQualifiedName
           else self~name=fullyQualifiedName~substr(pos+1)

  self~definitionsRaw=rest~makearray(" ")    -- create an array of "memberName|value"
  self~definitions=.relation~new       -- relation
  do item over self~definitionsRaw     -- parse
     parse var item name "|" value
     self~definitions[name]=.enumValue~new(self,name,value,datatype)
  end
  attributeList=.list~of("defaultValue")

::attribute defaultValue         -- defaultValue

::method toString
  expose defaultValue
  return self~toString:super"^defaultValue:"defaultValue


/* --------------------------------------------------------- */
::class enumValue     inherit comparable
::method init
  expose attributeList parent name fullyQualifiedName value unoTypeName  -- establish direct access to attributes (object variables)

  use strict arg parent, name, value, unoTypeName -- assign arguments to attributes
  fullyQualifiedName=name     -- rgf, added 2008-03-24
  attributeList=.list~of("parent", "name", "value", "unoTypeName", "fullyQualifiedName")

::attribute parent
::attribute unoTypeName
::attribute name
::attribute fullyQualifiedName
::attribute value
::attribute string get                 -- string value
   expose name value unoTypeName
   return "an" self~class~id"-["name"|"value"|"unoTypeName"]"

::attribute attributeList

   -- default sorting order: by name
::method compareTo
  expose name
  use arg other
  return name~caselessCompareTo(other~name)  -- use the String's "compareTo"



/* ========================================================================= */
::class UNO_EXCEPTION   subclass UNO_DefinitionWithMembers  public
::method init
  forward class (super) continue

  parse arg self~definition
  parse arg self~unoTypeName "|" fullyQualifiedName rest

  self~fullyQualifiedName=fullyQualifiedName
  pos=fullyQualifiedName~lastpos(".")  -- get last dot
  if pos=0 then self~name=fullyQualifiedName
           else self~name=fullyQualifiedName~substr(pos+1)

  self~definitionsRaw=rest~makearray(" ")    -- create an array of "memberName|datatype"
  self~definitions=.relation~new       -- relation
  do item over self~definitionsRaw     -- parse
     parse var item memberName "|" dataType ":" unoTypeName ":" referencedType ":" referencedUnoTypeName
     o=.exceptionMember~new(self, memberName, dataType, unoTypeName, referencedType, referencedUnoTypeName)

     if wordpos(unoTypeName,"UNO_INTERFACE UNO_ENUM UNO_STRUCT")>0 then
       o~fullyQualifiedName=dataType"."memberName
     else
       o~fullyQualifiedName=memberName

     self~definitions[memberName]=o
  end


/* --------------------------------------------------------- */
::class exceptionMember mixinclass fullDataType
::attribute fullyQualifiedName      -- rgf, 2008-03-24
::attribute parent
::method init
  expose parent
  use strict arg parent, ...
  newArgs=arg(2,"Array")   -- get rest of arguments (consume processed 'parent' argument)

  -- let superclass initialize
  forward class (super) arguments (newArgs)


::class exceptionMemberOld inherit comparable
::method init
     -- establish direct access to attributes (object variables)
  expose attributeList name datatype unoTypeName referencedType referencedUnoTypename

     -- assign arguments to attributes:
  use strict arg name, datatype, unoTypeName, referencedType, referencedUnoTypename
  attributeList=.list~of("parent", "name", "dataType", "unoTypeName", "referencedType", "referencedUnoTypename")

::attribute parent
::attribute name

-- "fullDatatype", defined as:
--       fullyQualifiedDataTypeName:UNOTypeClass:[referencedType]:[UNOTypeClass]
--       where "[referencedType:UNOTypeClass]" is given, if an UNO_TYPEDEF
::attribute datatype
::attribute unoTypeName
::attribute referencedType
::attribute referencedUnoTypename

::attribute string get                 -- string value
   expose pos name dataType unoTypeName referencedType referencedUnoTypename
   if referencedType="" then
      return "an" self~class~id"-["name"|"dataType"|"unoTypeName"]"

   return "an" self~class~id"-["name"|"dataType"|"unoTypeName"|"referencedType"|"referencedUnoTypename"]"

::attribute attributeList

   -- default sorting order: by name
::method compareTo
  expose name
  use arg other
  return name~caselessCompareTo(other~name)



/* ========================================================================= */
::class UNO_INTERFACE   subclass UNO_DefinitionWithMembers  public
::method init
  expose methods attributes

  forward class (super) continue
  parse arg self~definition
  parse arg self~unoTypeName "|" fullyQualifiedName rest

  self~fullyQualifiedName=fullyQualifiedName
  pos=fullyQualifiedName~lastpos(".")  -- get last dot
  if pos=0 then self~name=fullyQualifiedName
           else self~name=fullyQualifiedName~substr(pos+1)

  self~definitionsRaw=rest~makearray(" ")    -- create an array of "memberName|datatype"
  self~definitions=.relation~new       -- relation
  methods=.relation~new
  attributes=.relation~new
  do item over self~definitionsRaw     -- parse
     parse var item memberName "|" type "|"

     o=.nil
     if type="UNO_ATTRIBUTE" then
     do
        o=.uno_attribute~new(item, fullyQualifiedName)
        attributes[memberName]=o
     end
     else if type="UNO_METHOD" then
     do
        o=.uno_method~new(item, fullyQualifiedName)
        methods[memberName]=o
     end
     self~definitions[memberName]=o
  end
  self~attributeList~~append("methods")~~append("attributes")

::attribute methods              -- relation of methods
::attribute attributes           -- relation of attributes

::method toString
  expose methods attributes
  return self~toString:super"^#methods:"methods~items"^#attributes:"attributes~items



/* ========================================================================= */
::class UNO_METHOD      subclass UNO_Definition    public
::method init
  expose name oneWay returnValue argumentList exceptionList definedBy

  forward class (super) continue
  parse arg self~definition, definedBy
  parse arg self~name "|" self~unoTypeName "|" oneWay "|" returnValue "|" args "|" exc

  self~fullyQualifiedName=definedBy"."self~name

  argumentList=.list~new         -- create argument list
  argsArr=args~makeArray(",")    -- create array of individual args
  do i=1 to argsArr~items
     parse value argsArr[i] with name ":" dataType
     argumentList~append(.argument~new(i, name, dataType))
  end

  exceptionList=.list~new        -- create exception list
  do while exc<>""
     parse var exc ex "," exc
     exceptionList~append(ex)
  end

  self~attributeList~~append("oneWay")~~append("returnValue")         -
                    ~~append("argumentList")~~append("exceptionList") -
                    ~~append("definedBy")

::attribute oneWay         -- oneWay ?
::attribute returnValue    -- datatype
::attribute argumentList   -- list of argName:datatype
::attribute exceptionList  -- list of exception names
::attribute definedBy            -- remember where defined

::attribute string get                 -- string value
   expose returnValue
   return "an" self~class~id"-["self~name "/returns" returnValue"|...]"


::method toString

  expose oneWay returnValue argumentList exceptionList
  return self~toString:super || "|"oneWay"|returns="returnValue || -
            "|#arguments="argumentList~items"|#exceptions="exceptionList~items


/* Create and return string which renders definition as a Java method definition. */
::method toJavaSyntax
  expose oneWay returnValue argumentList exceptionList

  parse var returnValue tmpStr ":" .
  tmpStr=tmpStr self~name"("

  argList=""
  do arg over argumentList       -- create argument list
     if argList<>"" then argList=argList", "
     argList=argList || arg~name
  end
  tmpStr=tmpStr || argList")"

  if exceptionList~items>0 then  -- create exception list, if any
  do
     excList=""
     do exc over exceptionList
        if excList<>"" then excList=excList", "
        excList=excList || exc
     end
      -- rgf, 2009-02-20, changed one TAB to three blanks to allow two exceptions to
      --                  be placed in one line in the create_UNO_API_info.rxo tool
     tmpStr=tmpStr "0a09"x || "  " "throws" excList
  end
  return tmpStr



/* --------------------------------------------------------- */
::class argument inherit comparable
::method init
  expose attributeList pos name datatype  -- establish direct access to attributes (object variables)

  use strict arg pos, name, datatype      -- assign arguments to attributes
  attributeList=.list~of("pos", "name", "dataType")

::attribute pos
::attribute name
::attribute datatype
::attribute string get                 -- string value
   expose pos name dataType
   return "a" self~class~id"-["pos"|"name"|"dataType"]"

::attribute attributeList

   -- default sorting order: by pos-number
::method compareTo
  use arg left, right
  return (left-right)~sign





/* ========================================================================= */
::class UNO_MODULE      subclass UNO_DefinitionWithMembers  public
::method init
  forward class (super) continue
  parse arg self~definition
  parse arg self~unoTypeName "|" fullyQualifiedName rest
  self~fullyQualifiedName=fullyQualifiedName

  pos=fullyQualifiedName~lastpos(".")  -- get last dot
  if pos=0 then self~name=fullyQualifiedName
           else self~name=fullyQualifiedName~substr(pos+1)

  self~definitionsRaw=rest~makearray(" ")    -- create an array of "memberName|datatype"
  self~definitions=.relation~new       -- relation
  do item over self~definitionsRaw     -- parse
     parse var item memberName "|" dataType
     self~definitions[memberName]=.moduleMember~new(self, item, fullyQualifiedName)
  end



/* --------------------------------------------------------- */
::class moduleMember inherit comparable
::method init
     -- establish direct access to attributes (object variables)
  expose attributeList parent name fullyQualifiedName unoTypeName
  use arg parent
  parse arg ., name "|" unoTypeName, fqn
  fullyQualifiedName=fqn"."name     -- create fully qualified name

  attributeList=.list~of("parent", "name", "fullyQualifiedName", "unoTypeName")

::attribute parent
::attribute name
::attribute fullyQualifiedName         -- rgf, 2008-03-24
::attribute unoTypeName

::attribute string get                 -- string value
   expose pos name unoTypeName
   return "a" self~class~id"-["unoTypeName"|"name"]"


::attribute attributeList

   -- default sorting order: by name
::method compareTo
  expose name
  use arg other
  return name~caselessCompareTo(other~name)



/* ========================================================================= */
::class UNO_PROPERTY    subclass UNO_Definition    public
::method init
  expose modifiers dataType definedBy
  forward class (super) continue
  parse arg self~definition
  parse arg self~name "|" self~unoTypeName "|" modifiersText "|" datatype "|" definedBy


  self~fullyQualifiedName=definedBy"."self~name

  modifiers=.array~new
  do while modifiersText<>""
     parse var modifiersText mod "," modifiersText
     modifiers~append(mod)
  end
  modifiers~sort

  self~attributeList~~append("dataType")~~append("modifiers") -
                    ~~append("definedBy")

::attribute dataType          -- datatype


  /* possible property modifiers (property attributes) as of OOo 2.0:
     "MAYBEVOID"      (1  ), "BOUND"          (2  ), "CONSTRAINED"    (4  )
     "TRANSIENT"      (8  ), "READONLY"       (16 ), "MAYBEAMBIGUOUS" (32 )
     "MAYBEDEFAULT"   (64 ), "REMOVEABLE"     (128), "OPTIONAL"       (256) */
::attribute modifiers         -- list of modifiers, if any
::attribute definedBy         -- point to service, in which this property is truly defined

::method toString
  expose datatype modifiers definedBy
  return self~toString:super || "|" || pp(modifiers) || "|"dataType"|"definedBy

pp: procedure
  use arg modifiers
  if modifiers~items=0 then return "#modifiers:0"
                       else return "#modifiers:"modifiers~items"-"modifiers~toString(, "+")



/* ========================================================================= */
::class UNO_SERVICE     subclass UNO_DefinitionWithMembers  public
::method init
  expose implementationName services interfaces properties definedBy
  forward class (super) continue
  parse arg self~definition

  parse arg header rest
  parse var header self~unoTypeName "|" fullyQualifiedName "|" implementationName

  self~fullyQualifiedName=fullyQualifiedName

  pos=0
  if pos("+",fullyQualifiedName)=0 then   -- not a concatenation of service UNO IDL names ?
     pos=fullyQualifiedName~lastpos(".")  -- get last dot

  -- if pos=0 then self~name=fullyQualifiedName
  --         else self~name=fullyQualifiedName~substr(pos+1)
  self~name=fullyQualifiedName~substr(pos+1)

  self~definitionsRaw=rest~makearray(" ")    -- create an array of "memberName|datatype"
  self~definitions=.relation~new       -- relation
  properties=.relation~new
  interfaces=.relation~new
  services  =.relation~new

  do item over self~definitionsRaw     -- parse
     parse var item memberUNOTypeName "|" type "|" optional "|" definedBy
     -- self~definitions[memberUNOTypeName]=.serviceMember~new(self, memberUNOTypeName, type)

     if optional="" then optional="MANDATORY"   -- hence two explicit values: "OPTIONAL", "MANDATORY"

     if type="UNO_PROPERTY" then
     do
        o=.uno_property~new(item)
        properties[memberUNOTypeName]=o
     end
     else if type="UNO_INTERFACE" then
     do
        o=.serviceMember~new(self, memberUNOTypeName, type, optional)
        interfaces[optional]=memberUNOTypeName   -- either "OPTIONAL" or "" (= mandatory)
     end
     else if type="UNO_SERVICE" then
     do
        o=.serviceMember~new(self, memberUNOTypeName, type, optional)
        services[optional]=memberUNOTypeName     -- either "OPTIONAL" or "" (= mandatory)
     end

     self~definitions[memberUNOTypeName]=o

  end
  self~attributeList~~append("properties")~~append("interfaces") -
                    ~~append("services") ~~append("implementationName") -
                    ~~append("definedBy")

::attribute implementationName
::attribute properties           -- relation of properties: name-> .UNO_PROPERTY
::attribute interfaces           -- relation of interfaces: name-> "OPTIONAL"|""
::attribute services             -- relation of services:   name-> "OPTIONAL"|""
::attribute definedBy

::method toString
  expose implementationName properties interfaces services

  return self~toString:super || "|" || implementationName || ":#properties:"properties~items || -
            "|#interfaces:"interfaces~items || -
            "|#services:"services~items

/*
  return self~toString:super || "|#properties:"properties~items || -
            "|#interfaces:"interfaces~items"/mandatory:"interfaces~allat("")~items || -
            "|#services:"services~items"/mandatory:"services~allat("")~items
*/

/* --------------------------------------------------------- */

::class serviceMember inherit comparable
::method init
     -- establish direct access to attributes (object variables)
  expose attributeList name unoTypeName fullyQualifiedName parent optionality

     -- assign arguments to attributes:
  use strict arg parent, fullyQualifiedName, unoTypeName, optionality
  pos=lastpos(".", fullyQualifiedName)
  if pos=0 then name=fullyQualifiedName
           else name=fullyQualifiedName~substr(pos+1)

  attributeList=.list~of("parent", "name", "fullyQualifiedName", "unoTypeName", "optionality")

::attribute parent            -- the parent (container) object
::attribute name
::attribute fullyQualifiedName
::attribute unoTypeName
::attribute optionality

::attribute string get                 -- string value
   expose name fullyQualifiedName unoTypeName optional
   return "a" self~class~id"-["unoTypeName"|"name"|"fullyQualifiedName"|"optional"]"


::attribute attributeList

   -- default sorting order: by name
::method compareTo
  expose name
  use arg other
  return name~caselessCompareTo(other~name)



/* ========================================================================= */
::class UNO_SINGLETON   subclass UNO_Definition    public
::method init
  expose oldStyleServiceName
  forward class (super) continue
  parse arg self~definition
  parse arg self~unoTypeName "|" fullyQualifiedName "|" oldStyleServiceName

  self~fullyQualifiedName=fullyQualifiedName
  pos=fullyQualifiedName~lastpos(".")  -- get last dot
  if pos=0 then self~name=fullyQualifiedName
           else self~name=fullyQualifiedName~substr(pos+1)

  self~attributeList~~append("oldStyleServiceName")

::attribute oldStyleServiceName  -- referencedType

::method toString
  expose oldStyleServiceName
  return self~toString:super || "^"oldStyleServiceName




/* ========================================================================= */
::class UNO_STRUCT      subclass UNO_DefinitionWithMembers  public
::method init
  forward class (super) continue
  parse arg self~definition
  parse arg self~unoTypeName "|" fullyQualifiedName rest

  self~fullyQualifiedName=fullyQualifiedName
  pos=fullyQualifiedName~lastpos(".")  -- get last dot
  if pos=0 then self~name=fullyQualifiedName
           else self~name=fullyQualifiedName~substr(pos+1)

  self~definitionsRaw=rest~makearray(" ")    -- create an array of "memberName|datatype"
  self~definitions=.relation~new       -- relation
  do item over self~definitionsRaw     -- parse
     parse var item memberName "|" dataType ':' unoTypeName ':' refType ':' refUnoTypeName

     o=.structMember~new(self, memberName, dataType, unoTypeName, refType, refUnoTypeName)

     if wordpos(unoTypeName,"UNO_INTERFACE UNO_ENUM UNO_STRUCT")>0 then
       o~fullyQualifiedName=dataType"."memberName
     else
       o~fullyQualifiedName=memberName

     self~definitions[memberName]=o
  end


/* --------------------------------------------------------- */

  -- use strict arg name, datatype, unoTypeName, referencedType, referencedUnoTypename

::class structMember mixinclass fullDataType
::attribute fullyQualifiedName      -- rgf, 2008-03-24
::attribute parent
::method init
  expose parent
  use strict arg parent, ...
  newArgs=arg(2,"Array")   -- get rest of arguments (consume processed 'parent' argument)

  -- let superclass initialize
  forward class (super) arguments (newArgs)





/* ========================================================================= */
::class UNO_TYPEDEF     subclass UNO_Definition    public
::method init
  expose referencedType referencedUnoTypeName
  forward class (super) continue
  parse arg self~definition
  parse arg self~unoTypeName "|" fullyQualifiedName referencedType ":" referencedUnoTypeName

  self~fullyQualifiedName=fullyQualifiedName
  pos=fullyQualifiedName~lastpos(".")  -- get last dot
  if pos=0 then self~name=fullyQualifiedName
           else self~name=fullyQualifiedName~substr(pos+1)

  self~attributeList~~append("referencedType")~~append("referencedUnoTypeName")

::attribute referencedType       -- referencedType
::attribute referencedUnoTypeName-- referencedUnoTypeName

::method toString
  expose referencedType referencedUnoTypeName datatype
  return self~unoTypeName"|"self~fullyQualifiedName"|"referencedType"|"referencedUnoTypeName




/* --------------------------------------------------------- */
/* Class that parses a fullDataType of the form

      datatype:unoTypeName:[referencedDatatype]:[referencedTypeName]

   into its constituent attributes.

   Meant to be used via subclassing wherever fullDataTypes are returned
   from uno.getDefinition and need to be parsed.
*/
::class fullDataType mixinclass object inherit comparable
::method init
     -- establish direct access to attributes (object variables)
  expose attributeList name datatype unoTypeName referencedType referencedUnoTypename

     -- assign arguments to attributes:
  use strict arg name, datatype, unoTypeName, referencedType, referencedUnoTypename
  attributeList=.list~of("name", "dataType", "unoTypeName", "referencedType", "referencedUnoTypename")

::attribute name

-- "fullDatatype", defined as:
--       fullyQualifiedDataTypeName:UNOTypeClass:[referencedType]:[UNOTypeClass]
--       where "[referencedType:UNOTypeClass]" is given, if an UNO_TYPEDEF
::attribute datatype
::attribute unoTypeName
::attribute referencedType
::attribute referencedUnoTypename

::attribute string get                 -- string value
   expose pos name dataType unoTypeName referencedType referencedUnoTypename

   id=self~class~id
   if "aeiou"~caselessPos(id~left(1))>0 then article="an"
                                        else article="a"

   if referencedType="" then
      return article self~class~id"-["name"|"dataType"|"unoTypeName"]"

   return article self~class~id"-["name"|"dataType"|"unoTypeName"|"referencedType"|"referencedUnoTypename"]"

::attribute attributeList

   -- default sorting order: by name
::method compareTo
  expose name
  use arg other
  return name~caselessCompareTo(other~name)

/*

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2008-2010 Rony G. Flatscher

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
    -----------------------------------------------------------------------------
*/