# ${fillContext.ooRexxDocGenerationContext.title}

<#list fillContext.ooRexxDocGenerationContext.headers as key, value>
- ${key}: ${filler.evaluate(value)}
</#list>
---

${fillContext.ooRexxDocGenerationContext.overview}

---

## Packages
<#list fillContext.ooRexxPackages?sort_by("name") as package>
- [${package.name}](#${package.id?lower_case})
</#list>


<#list fillContext.ooRexxPackages?sort_by("name") as package>
- ${package.name}
    <#if package.routines?size != 0>
    - Routines
        - <#list package.routines?sort_by("name") as routine>[${routine.name}](#${routine.id?lower_case})  </#list>
    </#if>
    <#if package.methods?size != 0>
    - Floating Methods
        - <#list package.methods?sort_by("name") as method>[${method.name}](#${method.id?lower_case})  </#list>
    </#if>
    <#if package.attributes?size != 0>
    - Floating Attributes
        - <#list package.attributes?sort_by("name") as attribute>[${attribute.name}](#${attribute.id?lower_case})   </#list>
    </#if>
    <#if package.constants?size != 0>
    - Floating Constants
        - <#list package.constants?sort_by("name") as constant>>[${constant.name}](#${constant.id?lower_case})  </#list>
    </#if>
    <#if package.classes?size != 0>
    - Classes
        - <#list package.classes?sort_by("name") as class>[${class.name}](#${class.id?lower_case})  </#list>
    </#if>
</#list>

<#list fillContext.ooRexxPackages?sort_by("name") as package>

---

---

## ${package.name} <a id="${package.id?lower_case}"></a> ([Top](#packages))
<#if package.comment??>

> ${package.commentText?replace("\n", "\n> ")}

    <#if package.comment.commentAnnotations?size != 0>
        <#list package.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
        </#list>
        


    </#if>
</#if>

<#if package.routines?size != 0>
- Routines
    - <#list package.routines?sort_by("name") as routine>[${routine.name}](#${routine.id?lower_case})  </#list>
</#if>
<#if package.methods?size != 0>
- Floating Methods
    - <#list package.methods?sort_by("name") as method>[${method.name}](#${method.id?lower_case})  </#list>
</#if>
<#if package.attributes?size != 0>
- Floating Attributes
    - <#list package.attributes?sort_by("name") as attribute>[${attribute.name}](#${attribute.id?lower_case})   </#list>
</#if>
<#if package.constants?size != 0>
- Floating Constants
    - <#list package.constants?sort_by("name") as constant>>[${constant.name}](#${constant.id?lower_case})  </#list>
</#if>
<#if package.classes?size != 0>
- Classes
    - <#list package.classes?sort_by("name") as class>[${class.name}](#${class.id?lower_case})  </#list>
</#if>

<#if package.annotations?size != 0>
Annotated with:
<#list package.annotations?sort_by("name") as packageAnnotation>
    ${packageAnnotation.dataToString()}
</#list>
</#if>
     <#if package.routines?size != 0>
### Routines
         <#list package.routines?sort_by("name") as routine>

---

#### Routine: ${routine.name} <a id="${routine.id?lower_case}"></a> ([Package](#${package.id?lower_case}))
             <#if routine.external??>
- `EXTERNAL ${routine.external}`
             </#if>
             <#if package.annotations?size != 0>
- Annotated with:
                <#list routine.annotations as routineAnnotation>
    ${routineAnnotation.dataToString()}
                </#list>
             </#if>
             <#if routine.comment??>
> ${routine.commentText?replace("\n", "\n> ")}

 <#if routine.comment.commentAnnotations?size != 0>
     <#list routine.comment.commentAnnotations as commentAnnotation>
 - `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
     </#list>
     


 </#if>
             </#if>
             <#if routine.variableReferences?size != 0>
- Variable reference(s):
                 <#list routine.variableReferences as variableReference>
    - `${variableReference}`
                 </#list>


             </#if>
             <#if routine.useArg != "">
- `USE ARG ${routine.useArg}`
             </#if>

             <#if routine.parseArg??>
- `PARSE ARG ${routine.parseArg}`
             </#if>
         </#list>
     </#if>

    <#if package.methods?size != 0>
### Floating Methods
        <#list package.methods?sort_by("name") as method>

---

#### Floating Method: ${method.name}  <a id="${method.id?lower_case}"></a> ([Package](#${package.id?lower_case}))
            <#if !method.guarded>
- UNGUARDED
            </#if>
            <#if !method.unprotected>
- PROTECTED
            </#if>
            <#if method.classMethod>
- CLASS
            </#if>
            <#if method.abstractMethod>
- ABSTRACT
            </#if>
            <#if method.delegate??>
- `DELEGATE ${method.delegate}`
            </#if>
            <#if method.external??>
- `EXTERNAL ${method.external}`
            </#if>
            <#if method.comment??>
> ${method.commentText?replace("\n", "\n> ")}

<#if method.comment.commentAnnotations?size != 0>
    <#list method.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
    </#list>
    


</#if>
            </#if>

            <#if method.variableReferences?size != 0>
- Variable reference(s):
                <#list method.variableReferences as variableReference>
    - ${variableReference?html}
                </#list>
            </#if>

            <#if method.useArg?? && method.useArg != "">
- `USE ARG ${method.useArg}`
            </#if>

            <#if method.parseArg??>
- `PARSE ARG ${method.parseArg}`
            </#if>
            <#if method.annotations?size != 0>
- Annotated with:
                <#list method.annotations as methodAnnotation>
    ${methodAnnotation.dataToString()}
                </#list>
            </#if>
        </#list>
    </#if>
    <#if package.attributes?size != 0>
### Floating Attributes
        <#list package.attributes?sort_by("name") as attribute>

---

#### Floating Attribute: ${attribute.name}   <a id="${attribute.id?lower_case}"></a>  ([Package](#${package.id?lower_case}))
            <#if attribute.type != "BOTH">
- ${attribute.type}
            <#else>
- GETTER, SETTER
            </#if>
            <#if !attribute.guarded>
- UNGUARDED
            </#if>
            <#if !attribute.unprotected>
- PROTECTED
            </#if>
            <#if attribute.classMethod>
- CLASS
            </#if>
            <#if attribute.abstractMethod>
- ABSTRACT
            </#if>
    
            <#if attribute.delegate??>
- `DELEGATE ${attribute.delegate}`
            </#if>
            <#if attribute.external??>
- `EXTERNAL ${attribute.external}`
            </#if>
    
            <#if attribute.comment??>
> ${attribute.commentText?replace("\n", "\n> ")}

<#if attribute.comment.commentAnnotations?size != 0>
    <#list attribute.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
    </#list>
    


</#if>
            </#if>
    
            <#if attribute.useArg??>
- `USE ARG ${attribute.useArg}`
            </#if>
    
            <#if attribute.parseArg??>
- `PARSE ARG ${attribute.parseArg}`
            </#if>
            <#if attribute.annotations?size != 0>
- Annotated with:
                <#list attribute.annotations as attrAnnotation>
    ${attrAnnotation.dataToString()}
                </#list>
            </#if>
        </#list>
    </#if>
    <#if package.constants?size != 0>
### Floating Constants
        <#list package.constants?sort_by("name") as constant>

---

#### Floating Constant: ${constant.name}   <a id="${constant.id?lower_case}"></a> ([Package](#${package.id?lower_case}))

- Value: ${constant.value}
            <#if constant.annotations?size != 0>
- Annotated with:
                <#list constant.annotations as constantAnnotation>
    ${constantAnnotation.dataToString()}
                </#list>
            </#if>

            <#if constant.comment??>
> ${constant.commentText?replace("\n", "\n> ")}

<#if constant.comment.commentAnnotations?size != 0>
    <#list constant.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
    </#list>
    


</#if>
            </#if>
        </#list>
    </#if>

    <#if package.classes?size != 0>
### Classes
    </#if>
        <#list package.classes?sort_by("name") as class>

---


#### Class: ${class.name}   <a id="${class.id?lower_case}"></a> ([Package](#${package.id?lower_case}))
            <#if class.abstractClass == true>
- ABSTRACT
            </#if>
            <#if class.metaClass??>
- Meta-Class:${class.metaClass.name}
            </#if>
            <#if class.subClass??>
- Sub-Class: ${class.subClass.reference}
            </#if>
            <#if class.mixinClasses?size != 0>
- Mixin-Classes: <#list class.mixinClasses as mixin>${mixin.reference} </#list>
            </#if>
            <#if class.inheritClasses?size != 0>
- Inherit-Classes: <#list class.inheritClasses as inherit>${inherit.reference} </#list>
            </#if>
            <#if class.annotations?size != 0>
                <#list class.annotations as classAnnotation>
- Annotated with: ${classAnnotation.dataToString()}
                </#list>
            </#if>
           <#if class.comment??>
> ${class.commentText?replace("\n", "\n> ")}

<#if class.comment.commentAnnotations?size != 0>
   <#list class.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
   </#list>
   


</#if>
            </#if>
            <#if class.attributes?size != 0>


---

- Attributes
    - <#list class.attributes as attribute>[${attribute.name}](#${attribute.id?lower_case})  </#list>
            </#if>
            <#if class.methods?size != 0>


---

- Methods
    - <#list class.methods as method>[${method.name}](#${method.id?lower_case})  </#list>
            </#if>
            <#if class.constants?size != 0>


---

- Constants
    - <#list class.constants as constant>[${constant.name}](#${constant.id?lower_case})  </#list>
            </#if>
            <#list class.attributes?sort_by("name") as attribute>
###### Attribute: ${attribute.name}  <a id="${attribute.id?lower_case}"></a> ([Class](#${class.id?lower_case}))
                <#if attribute.type != "BOTH">
- ${attribute.type}
                <#else>
- GETTER, SETTER
                </#if>
                <#if !attribute.guarded>
- UNGUARDED
                </#if>
                <#if !attribute.unprotected>
- PROTECTED
                </#if>
                <#if attribute.classMethod>
- CLASS
                </#if>
                <#if attribute.abstractMethod>
- ABSTRACT
                </#if>

                <#if attribute.delegate??>
- `DELEGATE ${attribute.delegate}`
                </#if>
                <#if attribute.external??>
- `EXTERNAL ${attribute.external}`
                </#if>

                <#if attribute.comment??>
> ${attribute.commentText?replace("\n", "\n> ")}

<#if attribute.comment.commentAnnotations?size != 0>
    <#list attribute.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
    </#list>
    


</#if>
                </#if>

                <#if attribute.useArg??>
- `USE ARG ${attribute.useArg}`
                </#if>

                <#if attribute.parseArg??>
- `PARSE ARG ${attribute.parseArg}`
                </#if>

            <#if attribute.annotations?size != 0>
- Annotated with:
                <#list attribute.annotations as attrAnnotation>
    ${attrAnnotation.dataToString()}
                </#list>
            </#if>
            </#list>

            <#list class.methods?sort_by("name") as method>
###### Method: ${method.htmlName} <a id="${method.id?lower_case}"></a> ([Class](#${class.id?lower_case}))
                        <#if !method.guarded>
- UNGUARDED
                        </#if>
                        <#if !method.unprotected>
- PROTECTED
                        </#if>
                        <#if method.classMethod>
- CLASS
                        </#if>
                        <#if method.abstractMethod>
- ABSTRACT
                        </#if>

                        <#if method.delegate??>
- `DELEGATE ${method.delegate}`
                        </#if>
                        <#if method.external??>
- `EXTERNAL ${method.external}`
                        </#if>

                        <#if method.comment??>
> ${method.commentText?replace("\n", "\n> ")}

<#if method.comment.commentAnnotations?size != 0>
    <#list method.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
    </#list>
    


</#if>
                        </#if>

                        <#if method.variableReferences?size != 0>
- Variable reference(s):<#list method.variableReferences as variableReference>${variableReference?html}</#list>
                        </#if>

                        <#if method.useArg?? && method.useArg != "">
- `USE ARG ${method.useArg}`
                        </#if>

                        <#if method.parseArg??>
- `PARSE ARG ${method.parseArg}`
                        </#if>
                        <#if method.annotations?size != 0>
                            <#list method.annotations as methodAnnotation>
- Annotated with: ${methodAnnotation.dataToString()}
                            </#list>
                        </#if>
            </#list>
            <#list class.constants?sort_by("name") as constant>
                <#if constant??>
###### Constant: ${constant.name} <a id="${constant.id?lower_case}"></a> ([Class](#${class.id?lower_case}))
- Value: ${constant.value}
                    <#if constant.annotations?size != 0>
- Annotated with:
                    <#list constant.annotations as constantAnnotation>
    ${constantAnnotation.dataToString()}
                    </#list>
                    </#if>
                            <#if constant.comment??>
> ${constant.commentText?replace("\n", "\n> ")}

<#if constant.comment.commentAnnotations?size != 0>
    <#list constant.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
    </#list>
    


</#if>
                            </#if>
                </#if>
            </#list>
        </#list>
    <#if package.requires?size != 0>
---

### ${package.name} - Requires
        <#list package.requires as require>
- ${require.reference}
        </#list>
    </#if>
    <#if package.resources?size != 0>
---

### Resources
    </#if>
    <#list package.resources?sort_by("name") as resource>

#### ${resource.name}

- Annotated with: <#list resource.annotations as constantAnnotation>${constantAnnotation.dataToString()} </#list>

                    <#if resource.comment??>
> ${resource.commentText?replace("\n", "\n> ")}

<#if resource.comment.commentAnnotations?size != 0>
    <#list resource.comment.commentAnnotations as commentAnnotation>
- `${commentAnnotation.key}: ${commentAnnotation.firstWord}` ${commentAnnotation.remainder}
    </#list>
    \n\n
</#if>
                    </#if>
    </#list>

    <#if package.requiredBy?size != 0>
---


### ${package.name} - Required By
            <#list package.requiredBy as requiredBy>
                <#if requiredBy.id??>
${requiredBy.name}
                </#if>
            </#list>
    </#if>

    <#if package.options?size != 0>
---


### ${package.name} - Options
        <#list package.options as option>
- ${option.key}: ${option.value}
        </#list>
    </#if>
</#list>
