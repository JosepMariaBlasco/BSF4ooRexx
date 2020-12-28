<html lang="en">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <!--<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">-->

    <title>ooRexxDoc - ${title} - ${package.name}</title>

    <style type="text/css">
        body { padding-top: 70px; }

        #toTop{
            position: fixed;
            bottom: 10px;
            right: 10px;
            cursor: pointer;
            display: none;
        }

        pre {
            overflow-x: auto;
        }
        pre code {
            overflow-wrap: normal;
            white-space: pre;
        }

        .keyword {
            color: #ff5f00;
        }

        #search {
            margin-left: 10px;
        }

        :target {
            display: block;
            position: relative;
            top: -50px;
            visibility: hidden;
        }

        .class-info {
            display: inline-block;
            margin-bottom: 15px;
        }

    </style>
</head>

<body>
<nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">ooRexxDoc</a>
        </div>
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav">
                <li><a href="../oorexxdoc.html">Home</a></li>
                <li class="active"><a href="${package.id}.html">${package.name}</a></li>
            </ul>
            <form class="navbar-form">
                <div class="form-group">
                    <input type="text" id="search" class="form-control" placeholder="Search" id="test" data-provide="typeahead" autocomplete=off>
                </div>
                <span class="navbar-right">
                    <#list header?keys as a>
                        <span class="label label-info">${a}: ${header[a]}</span>
                    </#list>
                </span>
            </form>
        </div>
    </div>
</nav>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">Packages</div>
                <div class="panel-body" style="max-height: 420px;overflow-y: scroll;">
                    <#list packages as navPackage>
                        <a href="${navPackage.id}.html">${navPackage.name}</a>
                        <hr width="99%"/>
                    </#list>
                </div>
            </div>
        </div>

        <div class="col-md-8">
            <div class="panel panel-default">
                <div class="panel-heading">${package.name} Overview</div>
                <div class="panel-body">
                    <ul>
                        <#if package.routines?size != 0>
                            <li>Routines
                                <#list package.routines as routine>
                                    <a href="${package.id}.html#${routine.id}">${routine.name}</a>
                                </#list>
                            </li>
                        </#if>
                        <#if package.methods?size != 0>
                            <li>Floating Methods
                                <#list package.methods as method>
                                    <a href="${package.id}.html#${method.id}">${method.name}</a>
                                </#list>
                            </li>
                        </#if>
                        <#if package.attributes?size != 0>
                            <li>Floating Attributes
                                <#list package.attributes as attribute>
                                    <a href="${package.id}.html#${attribute.id}">${attribute.name}</a>
                                </#list>
                            </li>
                        </#if>
                        <#if package.constants?size != 0>
                            <li>Floating Constants
                                <#list package.constants as constant>
                                    <a href="${package.id}.html#${constant.id}">${constant.name}</a>
                                </#list>
                            </li>
                        </#if>
                        <#if package.classes?size != 0>
                            <li>Classes
                                <ul>
                                    <#list package.classes as class>
                                        <li><a href="${package.id}.html#${class.id}">${class.name}</a>
                                            <#if class.attributes?size != 0>
                                                <ul>
                                                    <li>Attributes:
                                                        <#list class.attributes as attribute>
                                                            <a href="${package.id}.html#${attribute.id}">${attribute.name}</a>
                                                        </#list>
                                                    </li>
                                                </ul>
                                            </#if>
                                            <#if class.methods?size != 0>
                                                <ul>
                                                    <li>Methods:
                                                        <#list class.methods as method>
                                                            <a href="${package.id}.html#${method.id}">${method.name}</a>
                                                        </#list>
                                                    </li>
                                                </ul>
                                            </#if>
                                        </li>
                                    </#list>
                                </ul>
                            </li>
                        </#if>
                        <#if package.requires?size != 0>
                            <li>Requires
                                <#list package.requires as require>
                                    ${require.reference}
                                </#list>
                            </li>
                        </#if>
                        <#if package.resources?size != 0>
                            <li>Resources
                                <#list package.resources as resource>
                                    <a href="${package.id}.html#${resource.id}">${resource.name}</a>
                                </#list>
                            </li>
                        </#if>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-xs-9">
            <div class="container-fluid" id="content-main">

            </div>
        </div>
    </div>
</div>
<div class="container-fluid">
    <div class="row">
        <div class="container-fluid">
            <h1>${package.name}</h1>

            <#if package.comment??>
                <br /><br />
                <div class="panel panel-default">
                    <div class="panel-body">
                        ${package.commentText}
                        <#if package.comment.commentAnnotations?size != 0 && package.commentText != "">
                            <br /><br />
                        </#if>
                        <#list package.comment.commentAnnotations as commentAnnotation>
                            <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                        </#list>
                    </div>
                </div>
            </#if>


            <#list package.annotations as packageAnnotation>
                Annotated with: ${packageAnnotation.dataToString()} <br />
            </#list>

            <#list package.routines as routine>
                <div class="row">
                    <div class="panel panel-default">
                        <a id="${routine.id}"></a>
                        <div class="panel-heading">
                            <h4>Routine: ${routine.name}</h4>
                        </div>
                        <div class="panel-body">
                            <#assign routineSpacing = false/>
                            <#if routine.external??>
                                <#assign routineSpacing = true/>
                                <span class="label label-info">EXTERNAL</span> ${routine.external}
                            </#if>

                            <#list routine.annotations as routineAnnotation>
                                Annotated with: ${routineAnnotation.dataToString()}
                            </#list>

                            <#if routine.comment??>
                                <#if routineSpacing>
                                    <br /><br />
                                </#if>
                                <div class="panel panel-default">
                                    <div class="panel-body">
                                        ${routine.commentText}
                                        <#if routine.comment.commentAnnotations?size != 0 && routine.commentText != "">
                                            <br /><br />
                                        </#if>
                                        <#list routine.comment.commentAnnotations as commentAnnotation>
                                            <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                        </#list>
                                    </div>
                                </div>
                            </#if>

                            <#if routine.variableReferences?size != 0>
                                <br /><b>Variable reference(s):</b>
                                <#list routine.variableReferences as variableReference>
                                    <i>${variableReference?html} </i>
                                </#list>
                                <br />
                            </#if>


                            <#if routine.useArg != "">
                                <br /><span class="keyword">USE ARG</span> ${routine.useArg}
                            </#if>

                            <#if routine.parseArg??>
                                <br /><span class="keyword">PARSE ARG</span> ${routine.parseArg}
                            </#if>

                        </div>
                    </div>
                </div>
            </#list>


            <#list package.methods as method>
                <div class="row">
                    <div class="panel panel-default">
                        <a id="${method.id}"></a>
                        <div class="panel-heading">
                            <h4>Floating Method: ${method.name}</h4>
                        </div>
                        <div class="panel-body">
                            <ul>
                                <li style="list-style-type: none;">
                                    <#if !method.guarded>
                                        <span class="label label-info">UNGUARDED</span>
                                    </#if>
                                    <#if !method.unprotected>
                                        <span class="label label-info">PROTECTED</span>
                                    </#if>
                                    <#if method.classMethod>
                                        <span class="label label-info">CLASS</span>
                                    </#if>
                                    <#if method.abstractMethod>
                                        <span class="label label-info">ABSTRACT</span>
                                    </#if>

                                    <#if method.delegate??>
                                        <span class="label label-info">DELEGATE</span> ${method.delegate}
                                    </#if>
                                    <#if method.external??>
                                        <span class="label label-info">EXTERNAL</span> ${method.external}
                                    </#if>

                                    <#if method.comment??>
                                        <br /><br />
                                        <div class="panel panel-default">
                                            <div class="panel-body">
                                                ${method.commentText}
                                                <#if method.comment.commentAnnotations?size != 0 && method.commentText != "">
                                                    <br /><br />
                                                </#if>
                                                <#list method.comment.commentAnnotations as commentAnnotation>
                                                    <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                                </#list>
                                            </div>
                                        </div>
                                    </#if>

                                    <#if method.variableReferences?size != 0>
                                        <br /><b>Variable reference(s):</b>
                                        <#list method.variableReferences as variableReference>
                                            <i>${variableReference?html} </i>
                                        </#list>
                                        <br />
                                    </#if>

                                    <#if method.useArg?? && method.useArg != "">
                                        <br /><span class="keyword">USE ARG</span> ${method.useArg}
                                    </#if>

                                    <#if method.parseArg??>
                                        <br /><span class="keyword">PARSE ARG</span> ${method.parseArg}
                                    </#if>

                                    <#list method.annotations as methodAnnotation>
                                        Annotated with: ${methodAnnotation.dataToString()}
                                    </#list>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </#list>

            <#list package.attributes as attribute>
                <div class="row">
                    <div class="panel panel-default">
                        <a id="${attribute.id}"></a>
                        <div class="panel-heading">
                            <h4>Floating Attribute: ${attribute.name}</h4>
                        </div>
                        <div class="panel-body">
                            <ul>
                                <li><b>${attribute.htmlName}</b>
                                    <#if attribute.type != "BOTH">
                                        <span class="label label-info">${attribute.type}</span>
                                    <#else>
                                        <span class="label label-info">GETTER</span>
                                        <span class="label label-info">SETTER</span>
                                    </#if>
                                    <#if !attribute.guarded>
                                        <span class="label label-info">UNGUARDED</span>
                                    </#if>
                                    <#if !attribute.unprotected>
                                        <span class="label label-info">PROTECTED</span>
                                    </#if>
                                    <#if attribute.classMethod>
                                        <span class="label label-info">CLASS</span>
                                    </#if>
                                    <#if attribute.abstractMethod>
                                        <span class="label label-info">ABSTRACT</span>
                                    </#if>

                                    <#if attribute.delegate??>
                                        <span class="label label-info">DELEGATE</span> ${attribute.delegate}
                                    </#if>
                                    <#if attribute.external??>
                                        <span class="label label-info">EXTERNAL</span> ${attribute.external}
                                    </#if>

                                    <#if attribute.comment??>
                                        <br /><br />
                                        <div class="panel panel-default">
                                            <div class="panel-body">
                                                ${attribute.commentText}
                                                <#if attribute.comment.commentAnnotations?size != 0  && attribute.commentText != "">
                                                    <br /><br />
                                                </#if>
                                                <#list attribute.comment.commentAnnotations as commentAnnotation>
                                                    <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                                </#list>
                                            </div>
                                        </div>
                                    </#if>

                                    <#if attribute.useArg??>
                                        <br /><span class="keyword">USE ARG</span> ${attribute.useArg}
                                    </#if>

                                    <#if attribute.parseArg??>
                                        <br /><span class="keyword">PARSE ARG</span> ${attribute.parseArg}
                                    </#if>

                                    <#list attribute.annotations as attrAnnotation>
                                        Annotated with: ${attrAnnotation.dataToString()}
                                    </#list>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </#list>

            <#list package.constants as constant>
                <div class="row">
                    <div class="panel panel-default">
                        <a id="${constant.id}"></a>
                        <div class="panel-heading">
                            <h4>Floating Constant: ${constant.name}</h4>
                        </div>
                        <div class="panel-body">
                            <ul>
                                <li><b>${constant.htmlName}</b>
                                    Value: ${constant.value}
                                    <#list constant.annotations as constantAnnotation>
                                        , Annotated with: ${constantAnnotation.dataToString()}
                                    </#list>

                                    <#if constant.comment??>
                                        <br /><br />
                                        <div class="panel panel-default">
                                            <div class="panel-body">
                                                ${constant.commentText}
                                                <#if constant.comment.commentAnnotations?size != 0 && constant.commentText != "">
                                                    <br /><br />
                                                </#if>
                                                <#list constant.comment.commentAnnotations as commentAnnotation>
                                                    <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                                </#list>
                                            </div>
                                        </div>
                                    </#if>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </#list>

            <#list package.classes as class>
                <div class="row">
                    <div class="panel panel-default">
                        <a id="${class.id}"></a>
                        <div class="panel-heading">
                            <h4>Class: ${class.name}</h4>
                            <#if class.abstractClass == true>
                                <span class="label label-info">ABSTRACT</span>
                            </#if>
                        </div>

                        <div class="panel-body">

                            <div>
                                <#if class.metaClass??>
                                    <span class="label label-info class-info">Meta-Class:${class.metaClass.name}</span>
                                </#if>
                                <#if class.subClass??>
                                    <span class="label label-info class-info">Sub-Class: ${class.subClass.reference}</span>
                                </#if>
                                <#if class.mixinClasses?size != 0>
                                    <#list class.mixinClasses as mixin><span class="label label-info class-info">Mixin-Classes: ${mixin.reference}</span></#list>
                                </#if>
                                <#if class.inheritClasses?size != 0>
                                    <#list class.inheritClasses as inherit><span class="label label-info class-info">Inherit-Classes: ${inherit.reference}</span></#list>
                                </#if>
                            </div>

                            <#list class.annotations as classAnnotation>
                                Annotated with: ${classAnnotation.dataToString()}
                            </#list>

                            <#if class.comment??>
                                <div class="panel panel-default">
                                    <div class="panel-body">
                                        ${class.commentText}
                                        <#if class.comment.commentAnnotations?size != 0 && class.commentText != "">
                                            <br /><br />
                                        </#if>
                                        <#list class.comment.commentAnnotations as commentAnnotation>
                                            <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                        </#list>
                                    </div>
                                </div>
                            </#if>

                            <div>
                                <h4>Attributes</h4>
                                <#if class.attributes?size == 0>
                                    <i>No Attributes</i>
                                </#if>
                                <#list class.attributes as attribute>
                                    <a id="${attribute.id}"></a>
                                    <ul>
                                        <li><b>${attribute.htmlName}</b>
                                            <#if attribute.type != "BOTH">
                                                <span class="label label-info">${attribute.type}</span>
                                            <#else>
                                                <span class="label label-info">GETTER</span>
                                                <span class="label label-info">SETTER</span>
                                            </#if>
                                            <#if !attribute.guarded>
                                                <span class="label label-info">UNGUARDED</span>
                                            </#if>
                                            <#if !attribute.unprotected>
                                                <span class="label label-info">PROTECTED</span>
                                            </#if>
                                            <#if attribute.classMethod>
                                                <span class="label label-info">CLASS</span>
                                            </#if>
                                            <#if attribute.abstractMethod>
                                                <span class="label label-info">ABSTRACT</span>
                                            </#if>

                                            <#if attribute.delegate??>
                                                <span class="label label-info">DELEGATE</span> ${attribute.delegate}
                                            </#if>
                                            <#if attribute.external??>
                                                <span class="label label-info">EXTERNAL</span> ${attribute.external}
                                            </#if>

                                            <#if attribute.comment??>
                                                <br /><br />
                                                <div class="panel panel-default">
                                                    <div class="panel-body">
                                                        ${attribute.commentText}
                                                        <#if attribute.comment.commentAnnotations?size != 0 && attribute.commentText != "">
                                                            <br /><br />
                                                        </#if>
                                                        <#list attribute.comment.commentAnnotations as commentAnnotation>
                                                            <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                                        </#list>
                                                    </div>
                                                </div>
                                            </#if>

                                            <#if attribute.useArg??>
                                                <br /><span class="keyword">USE ARG</span> ${attribute.useArg}
                                            </#if>

                                            <#if attribute.parseArg??>
                                                <br /><span class="keyword">PARSE ARG</span> ${attribute.parseArg}
                                            </#if>

                                            <#list attribute.annotations as attrAnnotation>
                                                Annotated with: ${attrAnnotation.dataToString()}
                                            </#list>
                                        </li>
                                    </ul>
                                </#list>
                            </div>

                            <hr />

                            <div>
                                <h4>Methods</h4>
                                <#if class.methods?size == 0>
                                    <i>No Methods</i>
                                </#if>
                                <#list class.methods as method>
                                    <a id="${method.id}"></a>
                                    <ul>
                                        <li><b>${method.htmlName}</b>
                                            <#if !method.guarded>
                                                <span class="label label-info">UNGUARDED</span>
                                            </#if>
                                            <#if !method.unprotected>
                                                <span class="label label-info">PROTECTED</span>
                                            </#if>
                                            <#if method.classMethod>
                                                <span class="label label-info">CLASS</span>
                                            </#if>
                                            <#if method.abstractMethod>
                                                <span class="label label-info">ABSTRACT</span>
                                            </#if>

                                            <#if method.delegate??>
                                                <span class="label label-info">DELEGATE</span> ${method.delegate}
                                            </#if>
                                            <#if method.external??>
                                                <span class="label label-info">EXTERNAL</span> ${method.external}
                                            </#if>

                                            <#if method.comment??>
                                                <br /><br />
                                                <div class="panel panel-default">
                                                    <div class="panel-body">
                                                        ${method.commentText}
                                                        <#if method.comment.commentAnnotations?size != 0 && method.commentText != "">
                                                            <br /><br />
                                                        </#if>
                                                        <#list method.comment.commentAnnotations as commentAnnotation>
                                                            <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                                        </#list>
                                                    </div>
                                                </div>
                                            </#if>

                                            <#if method.variableReferences?size != 0>
                                                <br /><b>Variable reference(s):</b>
                                                <#list method.variableReferences as variableReference>
                                                    <i>${variableReference?html} </i>
                                                </#list>
                                                <br />
                                            </#if>

                                            <#if method.useArg?? && method.useArg != "">
                                                <br /><span class="keyword">USE ARG</span> ${method.useArg}
                                            </#if>

                                            <#if method.parseArg??>
                                                <br /><span class="keyword">PARSE ARG</span> ${method.parseArg}
                                            </#if>

                                            <#list method.annotations as methodAnnotation>
                                                Annotated with: ${methodAnnotation.dataToString()}
                                            </#list>
                                        </li>
                                    </ul>
                                </#list>
                            </div>

                            <hr />

                            <div>
                                <h4>Constants</h4>
                                <#if class.constants?size == 0>
                                    <i>No Constants</i>
                                </#if>
                                <#list class.constants as constant>
                                    <#if constant??>
                                        <a id="${constant.id}"></a>
                                        <ul>
                                            <li><b>${constant.htmlName}</b>
                                                Value: ${constant.value}
                                                <#list constant.annotations as constantAnnotation>
                                                    , Annotated with: ${constantAnnotation.dataToString()}
                                                </#list>

                                                <#if constant.comment??>
                                                    <br /><br />
                                                    <div class="panel panel-default">
                                                        <div class="panel-body">
                                                            ${constant.commentText}
                                                            <#if constant.comment.commentAnnotations?size != 0 && constant.commentText != "">
                                                                <br /><br />
                                                            </#if>
                                                            <#list constant.comment.commentAnnotations as commentAnnotation>
                                                                <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                                            </#list>
                                                        </div>
                                                    </div>
                                                </#if>
                                            </li>
                                        </ul>
                                    </#if>
                                </#list>
                            </div>
                        </div>
                    </div>
                </div>
            </#list>

            <#if package.requires?size != 0>
                <div class="row">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h2>Requires</h2>
                        </div>
                        <div class="panel-body">
                            <#list package.requires as require>
                                ${require.reference} <br />
                            </#list>
                        </div>
                    </div>
                </div>
            </#if>

            <#list package.resources as resource>
                <div class="row">
                    <div class="panel panel-default">
                        <a id="${resource.id}"></a>
                        <div class="panel-heading">
                            <h4>Resource: ${resource.name}</h4>
                        </div>
                        <div class="panel-body">
                            <#list resource.annotations as constantAnnotation>
                                , Annotated with: ${constantAnnotation.dataToString()}
                            </#list>

                            <#if resource.comment??>
                                <br /><br />
                                <div class="panel panel-default">
                                    <div class="panel-body">
                                        ${resource.commentText}
                                        <#if resource.comment.commentAnnotations?size != 0 && resource.commentText != "">
                                            <br /><br />
                                        </#if>
                                        <#list resource.comment.commentAnnotations as commentAnnotation>
                                            <b>${commentAnnotation.key}</b>: ${commentAnnotation.value} <br />
                                        </#list>
                                    </div>
                                </div>
                            </#if>
                        </div>
                    </div>
                </div>
            </#list>

            <#if package.requiredBy?size != 0>
                <div class="row">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h2>Required By</h2>
                        </div>
                        <div class="panel-body">
                            <#list package.requiredBy as requiredBy>
                                <#if requiredBy.id??>
                                    <a href="${requiredBy.id}.html">${requiredBy.name}</a>
                                </#if>
                            </#list>
                        </div>
                    </div>
                </div>
            </#if>

            <#if package.options?size != 0>
                <div class="row">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h2>Options</h2>
                        </div>
                        <div class="panel-body">
                            <#list package.options as option>
                                ${option.key}: ${option.value} <br />
                            </#list>
                        </div>
                    </div>
                </div>
            </#if>

        </div>
    </div>
    <br />
    <script src="../js/jquery.min.js"></script>
    <script src="../js/typeahead-bootstrap.js"></script>
    <script src="../js/bootstrap.min.js"></script>
    <script src="../js/search-data.js"></script>
    <script>
        $(document).ready(function(){
            $('body').append('<div id="toTop" class="btn btn-info"><span class="glyphicon glyphicon-chevron-up"></span> Back to Top</div>');
            $(window).scroll(function () {
                if ($(this).scrollTop() != 0) {
                    $('#toTop').fadeIn();
                } else {
                    $('#toTop').fadeOut();
                }
            });
            $('#toTop').click(function(){
                $("html, body").animate({ scrollTop: 0 }, 200);
                return false;
            });
        });

    </script>
    <script>
        $(document).ready(function() {
            $('#search').typeahead({
                source: searchData,
                displayText: function(item) {
                    if(item.info !== null) {
                        return item.type + ":" + item.name + " (" + item.info + ")";
                    }

                    return item.type + ":" + item.name;
                },
                matcher: function(item) {
                    return item.name.toLowerCase().indexOf(this.query.toLowerCase()) != -1
                },
                afterSelect: function(item) {
                    window.location.href = item.href;
                    $("#search").val(item.name)
                },
                items: 20
            });
        })
    </script>
</body>
</html>