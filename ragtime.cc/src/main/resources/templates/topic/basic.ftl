<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#--
  Basic
-->
<@page.layout title="Basic">
    <#list articles?sequence as article>
        <@c.editable msg="article.${article.entity.id}">
            ${article.content}
        </@c.editable>
        <hr/>
    </#list>
</@page.layout>
