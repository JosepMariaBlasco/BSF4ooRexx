/*
*  2020-09-04, retrieved with

      svn co https://svn.apache.org/repos/asf/jakarta/taglibs/deprecated/bsf/ bsftaglibs

   changes:
        2020-09-07, rgf: - changed package name to "org.rexxla.taglibs.bsf"
                         - uppercased Java class name
                         - using return value EVAL_BODY_BUFFERED instead of EVAL_BODY_TAG, e.g.
                           <https://tomcat.apache.org/tomcat-9.0-doc/jspapi/index.html?javax/servlet/jsp/tagext/BodyTag.html>
                         - registering beans ordered by attribute names
*/

/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * Copyright 2000 Rony G., Flatscher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rexxla.taglibs.jsr223 ;


public class Scriptlet extends BaseImpl
{
    protected String  getTagName()
    {
        return "script";    // "scriptlet"
    }
    protected boolean isExpression()
    {
        return false;
    }
}
