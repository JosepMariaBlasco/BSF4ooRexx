#!/usr/bin/env rexx
/*
   author:     Rony G. Flatscher
   date:       2020-03-09
   name:       rexxcUtil.rex
   purpose:    get information about rexxc compiled programs, allow for base64 encoding
               and base64 decoding such files;

               if encoding/decoding takes place, then the following rules apply:

               1) all data up to and including .compiledHeader remain unchanged
               2) byte immediately following .comiledHeader gets changed from '00'x to '40'x
                  to indicate base64 encoding present
               3) all subsequent bytes get base64 encoded and replace the binary data

   usage:      rexxcUtil.rex [arguments]

               arguments:
               [?]                         ... usage information
               fileName                    ... outputs information about the rexxc compiled Rexx program
               -e fileName [newFileName]   ... base64 encode rexxc compiled fileName; replace file unless newFileName given
               -u fileName [newFileName]   ... unencode (decode) rexxc compiled fileName; replace file unless newFileName given

   remark:     encoding of a rexxc compiled program should insulate any unintended code page
               translations that would destroy the compiled Rexx program; this scenario may
               occur whenever third party libraries read script files assuming they consist
               of plain text that needs to be code page translated before usage (first observed
               with BSF4ooRexx)

   needs:      ooRexx 5.0 or later

   version:    1.07
   changed:    2019-05-07, ---rgf,
                        - fix parsing of file names, check whether input file exists
                          supply return code for calling program:
                          0 (o.k.)
                          1 (error: unknown switch)
                          2 (error: file does not exist)
                          3 (error: file is not rexxc'd)
                          4 (error: not a base64 encoded file)
                          5 (error: already encoded)
                          6 (error: already decoded)

               2019-05-11, ---rgf,
                        - fix typo in "unknown switch" error message
                        - add return codes indicating error in rexxc-image
                          rc=2      -- error: file does not exist
                          rc=51     -- error: compiledHeader not found
                          rc=52     -- error: fileTag not equal to compiledHeader
                          rc=53     -- error: illegal magicNumber
                          rc=54     -- error: imageVersion in rexxc image does not match the one supported by this program
                          rc=55     -- error: illegal wordSize, must be 32 or 64
                          rc=56     -- error: required languageLevel larger than the interpreter's languageLevel
                          rc=57     -- error: illegal imageSize does not match imageData length

               2019-07-12, ---rgf; revision 11890 adds support for base64 encoded compiled code "rexxc srcFile tgtFile -e"

               2019-07-16, ---rgf; revision 11894 breaks up the encoded data in 72 char lines (like sendmail/MIME)

               2019-07-17, ---rgf; revision 11896

               2020-03-09, ---rgf; according to Rick's information in the ooRexx developer list
                                   the binary can be executed by future ooRexx versions, hence
                                   adjusting the test for languageLevel accordingly

   license: dual licensed as CPL 1.0 (cf. <https://opensource.org/licenses/cpl1.0.php>) and
                             AL 2.0 <https://opensource.org/licenses/Apache-2.0>

additional information for analyzing data cf.:

--> working with ProgramMetaData.* and LanguageLevel.hpp from r11875 which sets imageVersion to 42

   interpreter/classes/support/ProgramMetaData.hpp: defines the fields characterizing the compiled Rexx script

       ... cut ..
   protected:
       enum
       {
           MAGICNUMBER = 11111,           // remains constant from release-to-release
           METAVERSION = 42               // gets updated when internal form changes
       };

       --> Rexx positions:

       char fileTag[16];               1-16      // special header for file tagging, currently the string: "/**/@REXX"
    *) uint16_t       magicNumber;    17-18      // special tag to indicate good meta data, currently: 11111
   **) uint16_t       imageVersion;   19-20      // version identifier for validity, currently supported only: 42
       uint16_t       wordSize;       21-22      // size of a word, currently: 32 or 64
       uint16_t       bigEndian;      23-24      // true if this is a big-endian platform, currently either 0 or 1
  ***) LanguageLevel  requiredLevel;  25-28      // required language level for execution, currently: 60500
       uint32_t       reserved;       29-32      // padding to bring imageSize to a 64-bit boundary
 ****) size_t         imageSize;      33-{36|40} // size of the image; "size_t" is either 32 or 64 bit depending on wordsize
       char           imageData[4];              // the copied image data; start of the image data which is always 'imageSize' long
       ... cut ..

   *) conatant value: 11111
  **) value: 42 (this file layout version introduced "reserved" and zeroes fileTag before using it)
 ***) enum forced to 32 bits
****) size_t may be uint32_t or uint64_t depending on the platform; wordSize/8 gives the number of bytes to use
*/


   -- setup
pkgLocal=.context~package~local
pkgLocal~compiledHeader      ="/**/@REXX" -- cf. ProgramMetaData.?pp
pkgLocal~compiledHeaderLength=.compiledHeader~length
pkgLocal~encodedHeader       ="/**/@REXX@" || "0A"x   -- cf. ProgramMetaData.cpp
pkgLocal~encodedHeaderLength =.encodedHeader~length
pkgLocal~magicNumber         =11111       -- cf. ProgramMetaData.?pp
pkgLocal~imageVersion        =42          -- only supported image version, cf. ProgramMetaData.?pp
pkgLocal~encodingChunkLength =72
pkgLocal~bDebug              =.false -- .true       -- if .true, then use sayDebug to show state of decoding at point in error

   -- get and process arguments
parse arg args
if args="" | args~pos('?')>1 then
do
   call showUsage
   exit 0
end

res=processArguments(args)
if res<>0 then       -- something went wrong
do
   if res>100 then   -- show usage!
   do
      say
      call showUsage
   end
   exit (res//100)   -- use remainder as exit code
end


   -- process argument, if error encountered returns .false, .true else
::routine processArguments
   parse arg args
   args=args~strip      -- remove blanks
   pos=0                -- indicate no switch
   if args[1]='-' then  -- a switch
   do
       parse var args '-' switch rest
       pos="E U"~wordPos(switch~upper)    -- 1='D'ecode, 2='E'ncode
       if pos=0 then
       do
          .error~say("error: unknown switch" pp("-"switch) "in argument, aborting...")
          return 101     -- show usage, hence >9
       end

       rest=rest~strip  -- remove any surrounding blanks
       if rest~pos('"')>0 then   -- we have a quoted file (may contain blanks)
       do
           if rest[1]='"' then   -- first file name is quoted
           do
               parse var rest '"' fileName1 '"' fileName2
               fileName2=fileName2~strip~strip('both','"')  -- remove any surrounding blanks and double-quotes around fileName2
           end
           else                  -- second file name is quoted
           do
               parse var rest fileName1  '"' fileName2 '"'
           end
       end
       else
       do
          parse var rest fileName1 fileName2
          fileName2=fileName2~strip -- remove any surrounding blanks
       end

         -- determine name of output file
       if fileName2<>"" then outFile=fileName2  -- strip blanks, remove double quotes, if any
                        else outFile=fileName1

       if \sysFileExists(fileName1) then
       do
          .error~say(.LINE "error: file" pp(fileName1) "does not exist, aborting...")
          return 2
       end

         -- get and analyze rexxc file
       o=.rexxc~new(fileName1)
       if \o~rexxc? then
       do
          .error~say("error: file" pp(fileName1) "is not a 'rexxc' compiled Rexx program, aborting...")
          return 3
       end

       if pos=1 then    -- encode
       do
          if o~encoded? then
          do
             .error~say("error: file" pp(fileName1) "is already base64 encoded, aborting...")
             return 5
          end
            -- base64 encode
          newData=o~allCharsBase64     -- get encoded form
       end
       else             -- decode
       do
          if \o~encoded? then
          do
             .error~say("error: file" pp(fileName1) "is not base64 encoded, cannot decode, aborting...")
             return 4
          end
            -- base64 decode
          newData=o~allCharsBinary     -- get binary form
       end

         -- prepend leadin (hash bang), if any
       if \o~leadin~isNil then newData=o~leadin || newData
         -- write file, replace if it exists
       .stream~new(outFile)~~open("write replace")~~charout(newData)~~close
   end

   else  -- no switch, remainder must be a rexxc file for which information should be shown
   do
      file=args~strip~strip("both",'"')  -- remove any surrounding blanks and double-quotes
      if \sysFileExists(file) then
      do
         .error~say("error: file" pp(file) "does not exist, aborting...")
         return 2
      end

      o=.rexxc~new(file)
      say o~info
   end

   return o~returnCode    -- return rc (return code) value of .rexxc object



::routine showUsage
  parse source . . thisProgram
  say filespec("name",thisProgram)
  say .resources~usage
  return

/*
-- working with ProgramMetaData.* and LanguageLevel.hpp from r11875 which sets imageVersion to 42
   This class parses the file, creates and saves the binary or the encoded form.
*/
::class rexxc

::attribute  fileName         -- name of compiled Rexx program
::attribute  encoded?          -- if binary .false, if base64 encoded .true
::attribute  rexxc?   -- is file a proper compiled Rexx program

::attribute  fileTag          -- the string "/**/@REXX"
::attribute  leadin           -- characters before compiledHeader string

::attribute  magicNumber      -- pos 17, uint16_t
::attribute  imageVersion     -- pos 19, uint16_t
::attribute  wordSize         -- pos 21, uint16_t (bits used for size_t: 32 or 64)
::attribute  wordSizeBytes    -- calculated (4 or 8)

-- little endian: 3210 (need reverse), big endian: 0123
::attribute  bigEndian?        -- pos 23, uint16_t
::attribute  requiredLevel    -- pos 25, uint32_t (enum 32 bits wide)
::attribute  reserved         -- pos 29, uint32_t
::attribute  imageSize        -- pos 33, size_t (uint32_t) or uint64_t depending on wordSize

::attribute  imageData        -- pos 33, char [4]

-- ::attribute  allChars         -- content of file

::attribute  allCharsBinary   -- entire file in binary form
::attribute  allCharsBase64   -- entire file in base64 encoded form

::attribute  startOfCompiledHeader  -- position compiledHeader starts
::attribute  returnCode       -- return code: 0 or error-number

::method init                 -- constructor
  use local allChars stream tmp len1  -- local variables (all others are attributes)
  use strict arg fileName

   -- set default values for attributes
  rexxc?          =.false
  encoded?        =.false
  leadin          =.nil
  fileTag         =.nil
  magicNumber     =.nil
  imageVersion    =.nil
  wordSize        =.nil
  wordSizeBytes   =.nil
  bigEndian?       =.nil
  requiredLevel   =.nil
  reserved        =.nil
  imageSize       =.nil
  imageData       =.nil
  allCharsBinary  =.nil
  allCharsBase64  =.nil
  returnCode      =0       -- return code: assume everything works out fine

   -- read the entire content of the file
  if \sysFileExists(fileName) then
  do
     .error~say("error: file" pp(fileName) "does not exist, aborting...")
     returnCode=2      -- error: file does not exist
     return
  end

  stream=.stream~new(fileName)~~open
  allChars=stream~charIn(1,stream~chars)
  stream~close

  numeric digits 20           -- allow full digits for values up to 2**64

  startOfCompiledHeader=allChars~pos(.compiledHeader)      -- get starting position for compiledHeader
  if startOfCompiledHeader=0 then               -- not a rexxc compiled Rexx program
  do
     if .bDebug=.true then
     do
        .error~say("error: file" pp(fileName)": string" pp(.compiledHeader) "not found, aborting...")
        self~sayDebug
     end
     returnCode=51     -- error: compiledHeader not found
     return
  end

  if startOfCompiledHeader=1 then   -- define leadin string
     leadin=""
  else
     leadin=allChars~substr(1,startOfCompiledHeader-1)

  encoded?=(.encodedHeader=allChars~substr(startOfCompiledHeader,.encodedHeaderLength))
  if encoded? then
  do
     allCharsBase64=allChars~substr(startOfCompiledHeader+.encodedHeaderLength)  -- accounts for trailing "@" || "0a"x
     allCharsBinary=decodeEncodedData(allCharsBase64) -- since 20190716, r11894
  end
  else
  do
     allCharsBinary=allChars~substr(startOfCompiledHeader)
     allCharsBase64=.encodedHeader || encodeBinaryData(allCharsBinary)  -- since 20190716, r11894
  end


  fileTag=allCharsBinary[1,16]      -- "/**/@REXX"
  if fileTag[1,.compiledHeaderLength]<>.compiledHeader then -- fileTag value must be equal to .compiledHeader
  do
     if .bDebug=.true then
     do
        .error~say("error: file" pp(fileName)': fileTag has illegal value "'||fileTag|'", expecting value "'.compiledHeader'", aborting...' )
        self~sayDebug
     end
     returnCode=52     -- error: byte after compiledHeader not '00'x nor '40'x ('@')
     return
  end

   -- get endianness before interpreting integer values
  bigEndian?=(allCharsBinary[23,2]~c2x~x2d=.true)

  tmp=allCharsBinary[17,2]
  if \bigEndian? then tmp=tmp~reverse
  magicNumber=tmp~c2x~x2d
  if magicNumber<>.magicNumber then
  do
     if .bDebug=.true then
     do
        .error~say("error: file" pp(fileName)': magic number' pp(magicNumber) 'does not match' pp(.magicNumber)', aborting...')
        self~sayDebug
     end
     returnCode=53     -- error: illegal magicNumber
     return
  end

  tmp=allCharsBinary[19,2]
  if \bigEndian? then tmp=tmp~reverse   -- if little endian, reverse characters
  imageVersion=tmp~c2x~x2d

  if imageVersion<>.imageVersion then
  do
     if .bDebug=.true then
     do
        .error~say("error: file" pp(fileName)': imageVersion' pp(imageVersion) 'does not match' pp(.imageVersion)', aborting...')
        self~sayDebug
     end
     returnCode=54     -- error: imageVersion in rexxc image does not match the one supported by this program
     return
  end


  tmp=allCharsBinary[21,2]
  if \bigEndian? then tmp=tmp~reverse   -- if little endian, reverse characters
  wordSize=tmp~c2x~x2d                 -- # of bits for a word (32, 64)
  if "32 64"~wordpos(wordSize)=0 then
  do
     if .bDebug=.true then
     do
        .error~say("error: file" pp(fileName)': illegal wordSize' pp(wordSize) 'must be 32 or 64, aborting...')
        self~sayDebug
     end
     returnCode=55     -- error: illegal wordSize, must be 32 or 64
     return
  end

  wordSizeBytes=wordSize/8

  tmp=allCharsBinary[25,4]
  if \bigEndian? then tmp=tmp~reverse   -- if little endian, reverse characters
  requiredLevel=tmp~c2x~x2d
  if requiredLevel>.rexxinfo~languageLevel*10000 then
  do
     if .bDebug=.true then
     do
        .error~say("error: file" pp(fileName)': requiredLevel' pp(requiredLevel/10000) "is larger than the current interpreter's languageLevel" pp(.rexxinfo~languageLevel)", aborting...")
        self~sayDebug
     end
     returnCode=56     -- error: required languageLevel does not match the interpreter's languageLevel
     return
  end

  tmp=allCharsBinary[29,4]
  if \bigEndian? then tmp=tmp~reverse   -- if little endian, reverse characters
  reserved=tmp~c2x~x2d

  tmp=allCharsBinary[33,wordsizeBytes] -- 4 or 8 bytes depending on wordSize !
  if \bigEndian? then tmp=tmp~reverse   -- if little endian, reverse characters
  imageSize=tmp~c2x~x2d

  imageData=allCharsBinary~substr(33+wordSizeBytes)

  if imageSize<>imageData~length then
  do
     if .bDebug=.true then
     do
        .error~say("error: file" pp(fileName)': imageSize' pp(imageSize) 'does not match the length of imageData' pp(imageData~length)', aborting...')
        self~sayDebug
     end
     returnCode=57     -- error: illegal imageSize does not match imageData length
     return
  end
  rexxc?=.true       -- we have a rexxc compiled program in hand



::method sayDebug
  use local
  say "   fileName............" pp(fileName)
  say "   rexxc? ............." pp(rexxc?)
  say "   encoded? ..........." pp(encoded?)
  say "   returnCode ........." pp(returnCode)
  if leadin~isNil then
     say "   leadin ............." pp(leadin)
  else
     say "   leadin ............." pp(leadin~changeStr("0d"x,'')~changeStr("0a"x,''))
  say "   compiledHeader ....." pp(.compiledHeader)
  say "   magicNumber ........" pp(magicNumber)
  say "   imageVersion ......." pp(imageVersion)
  say "   wordSize ..........." pp(wordSize)
  say "   wordSizeBytes ......" pp(wordSizeBytes)
  say "   bigEndian? ........." pp(bigEndian?)
  if requiredLevel~isNil then
     say "   requiredLevel ......" pp(requiredLevel)
  else
     say "   requiredLevel ......" pp(requiredLevel) '(should match ".RexxInfo~languageLevel * 10000")'
  say "   reserved ..........." pp(reserved)
  say "   imageSize .........." pp(imageSize)
  if imageData~isNil then
     say "   imageData .........." pp(imageData)
  else
  do
     if imageData~length<17 then say "   imageData .........." pp(imageData~c2x)
                            else say "   imageData .........." pp(imageData~left(min(16,imageData~length))~c2x"...")
  end

-- returns a string with the most important information in a form that can be easily parsed with Rexx;
-- the fragment "fileName=name of file" is last, such that easy parsing is possible, even if it contains spaces
::method info
  use local

  if rexxc?=.false then    -- if not a rexxc compiled program, set rexxc attribute values to "-1"
     return "rexxc?="rexxc? -
         "encoded?=-1" "wordSize=-1" "bigEndian?=-1" "imageVersion=-1" -
         "requiredLevel=-1" "imageSize=-1" "fileName="fileName

  return "rexxc?="rexxc? -
         "encoded?="||encoded? "wordSize="wordSize "bigEndian?="||bigEndian? "imageVersion="imageVersion -
         "requiredLevel="requiredLevel "imageSize="imageSize "fileName="fileName


::routine pp
  return "["arg(1)"]"


::routine decodeEncodedData   -- removes LFs from encoded data and then returns the decodeBase64 version of it
  use arg allCharsBase64
  tmpStr=allCharsBase64~changestr("0a"x,"")   -- remove LF characters
  return tmpStr~decodeBase64

::routine encodeBinaryData    -- encode binary data and insert LF after 72 chars to match r11894
  parse arg allCharsBinary
  tmpStr=allCharsBinary~encodeBase64
  mb=.mutableBuffer~new
  step=.encodingChunkLength
  do i=1 to tmpStr~length by step
     mb~append(tmpStr[i,step]~strip,"0a"x)
  end
  return mb~string



::resource usage
     purpose: get information about rexxc compiled Rexx programs; allow for base64 encoding or base64 decoding

     usage:
        rexxcUtil.rex [-e | -u] fileName

     arguments:
        [?]                         ... usage information
        fileName                    ... outputs information about the rexxc compiled Rexx program named fileName
        -e fileName [newFileName]   ... encode rexxc compiled fileName, replace file unless newFileName given
        -u fileName [newFileName]   ... unencode (decode) rexxc compiled fileName, replace file unless newFileName given
::END



/*
   -------------------- Apache License 2.0 --------------------
      Copyright 2019 Rony G. Flatscher

      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
   -------------------- Apache License 2.0  --------------------
*/
