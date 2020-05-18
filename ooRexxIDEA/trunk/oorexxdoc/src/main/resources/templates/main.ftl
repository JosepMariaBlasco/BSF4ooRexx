<html lang="en">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <!--<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">-->

    <title>ooRexxDoc - ${title}</title>

    <style type="text/css">
        body { padding-top: 70px; }

        #search {
            margin-left: 10px;
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
                <li class="active"><a>Home</a></li>
            </ul>
            <form class="navbar-form">
                <div class="form-group">
                    <input type="text" id="search" class="form-control" placeholder="Search" id="test" data-provide="typeahead" autocomplete=off>
                </div>
                <span class="navbar-right">
                    <#list header?keys as key>
                        <span class="label label-info">${key}: ${header[key]}</span>
                    </#list>
                </span>
            </form>

        </div>
    </div>
</nav>
<div id="search-result"></div>
<div class="container-fluid main-container">
    <div class="row">
        <div class="col-md-offset-3 col-md-6 content">
            <div class="panel panel-default">
                <div class="panel-body">
                    <#if overview??>
                        ${overview}
                        <#else>
                        Welcome
                    </#if>

                    <hr width="99%"/>
                    <#list packages as package>
                        <a href="packages/${package.name}.html">${package.name}</a>
                        <ul>
                            <#if package.routines?size != 0>
                                <li>Routines
                                    <ul>
                                        <#list package.routines as routine>
                                            <li><a href="packages/${package.name}.html#${routine.id}">${routine.name}</a></li>
                                        </#list>
                                    </ul>
                                </li>
                            </#if>
                            <#if package.methods?size != 0>
                                <li>Floating Methods
                                    <ul>
                                        <#list package.methods as method>
                                            <li><a href="packages/${package.name}.html#${method.id}">${method.name}</a></li>
                                        </#list>
                                    </ul>
                                </li>
                            </#if>
                            <#if package.attributes?size != 0>
                                <li>Floating Attributes
                                    <ul>
                                        <#list package.attributes as attribute>
                                            <li><a href="packages/${package.name}.html#${attribute.id}">${attribute.name}</a></li>
                                        </#list>
                                    </ul>
                                </li>
                            </#if>
                            <#if package.constants?size != 0>
                                <li>Floating Constants
                                    <ul>
                                        <#list package.constants as constant>
                                            <li><a href="packages/${package.name}.html#${constant.id}">${constant.name}</a></li>
                                        </#list>
                                    </ul>
                                </li>
                            </#if>
                            <#if package.classes?size != 0>
                                <li>Classes
                                    <ul>
                                        <#list package.classes as class>
                                            <li><a href="packages/${package.name}.html#${class.id}">${class.name}</a></li>
                                        </#list>
                                    </ul>
                                </li>
                            </#if>
                        </ul>
                        <hr width="99%"/>
                    </#list>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="js/jquery-3.2.1.min.js"></script>
<script src="js/typeahead-bootstrap.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/search-data.js"></script>
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
                window.location.href = "packages/"+item.href;
                $("#search").val(item.name)
            },
            items: 20
        });
    })
</script>

</body>
</html>