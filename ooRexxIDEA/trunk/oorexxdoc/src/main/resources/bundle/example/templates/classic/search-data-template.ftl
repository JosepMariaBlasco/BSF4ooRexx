var searchData = [
    <#list fillContext.ooRexxPackages as package>
    {
        id: "${package.id}", type: "Package", name: "${package.jsName}", info: null,  href: "${package.id}.html"
    },
        <#list package.classes as class>
        {
        id: "${class.id}", type: "Class", name: "${class.jsName}", info: "${package.jsName}", href: "${package.id}.html#${class.id}"
        },
            <#list class.methods as method>
            {
            id: "${method.id}", type: "Method", name: "${method.jsName}", info: "Class ${class.jsName}", href: "${package.id}.html#${method.id}"
            },
            </#list>
            <#list class.attributes as attribute>
            {
            id: "${attribute.id}", type: "Attribute", name: "${attribute.jsName}", info: "Class ${class.jsName}", href: "${package.id}.html#${attribute.id}"
            },
            </#list>
        </#list>
        <#list package.routines as routine>
        {
        id: "${routine.id}", type: "Routine", name: "${routine.jsName}", info: "${package.jsName}",  href: "${package.id}.html#${routine.id}"
        },
        </#list>
    </#list>
];
