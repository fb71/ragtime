<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#--
  Page/Body 
-->
<@page.layout title="Basic">
    <#list articles?sequence as article>
        <@c.editable msg="article.${article.entity.id}">
            ${article.content}
        </@c.editable>
        <hr/>
    </#list>
</@page.layout>
