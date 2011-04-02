package org.rexxla.bsf.engines.rexx;

import org.apache.bsf.BSFException;

/**
  * This class allows to create an exception which contains a reference to the Rexx
 *  condition object represented as a RexxProxy object, such that Java programs become
 *  able to directly interact with the Rexx condition object.
 *

 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2009 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ----------------------------------------------------------------------------- </pre>
 *
  * @version 1.0.0, 2009-06-08
  * @author Rony G. Flatscher (<a href="http://www.wu.ac.at">WU Vienna</a>, <a href="http://www.wu.ac.at/english">http://www.wu.ac.at/english</a>)
  */

public class RexxException extends BSFException
{
    /** Version information on this class. */
    static final public String version="100.20090608";


    /** Contains a RexxProxy object for the Rexx condition object. */
    RexxProxy rexxConditionObject=null;

    /** Getter method.
     *
     *  @return a RexxProxy object referring to the Rexx condition object
     */
    public RexxProxy getRexxConditionObject()
    {
        return rexxConditionObject;
    }


    /** Creates a RexxException with a message.
     *
     * @param message error message
     */
    public RexxException (String message)
    {
        super(message);
    }

    /** Creates a RexxException with a message, and a RexxProxy allowing to access the Rexx condition object
     *  for further inspection.
     *
     * @param message error message
     * @param rexxConditionObject RexxProxy object referring to the Rexx condition object
     */
    public RexxException (String message, RexxProxy rexxConditionObject)
    {
        super(message);
        this.rexxConditionObject=rexxConditionObject;
    }


    /**
     * @param reason can be one of the constants defined in {@link org.apache.bsf.BSFException}, values:
     *    <dt>0   <dd>(REASON_INVALID_ARGUMENT),
     *    <dt>10  <dd>(REASON_IO_ERROR),
     *    <dt>20  <dd>(REASON_UNKNOWN_LANGUAGE),
     *    <dt>100 <dd>(REASON_EXECUTION_ERROR),
     *    <dt>499 <dd>(REASON_UNSUPPORTED_FEATURE),
     *    <dt>500 <dd>(REASON_OTHER_ERROR).
     *    </dl>
     * @param message error message
     * @param rexxConditionObject RexxProxy object referring to the Rexx condition object
     */
    public RexxException (int reason, String message, RexxProxy rexxConditionObject)
    {
        super(reason, message);
        this.rexxConditionObject=rexxConditionObject;
    }
}


