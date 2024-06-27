<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#--
  Basic
-->
<@page.layout title="${topic.title}">
    <#list articles?sequence as article>
        <@c.editable msg="article.${article.entity.id}">
            ${article.content}
        </@c.editable>
    
        <#if article?counter < articles?sequence?size>
            <hr/>
        </#if>
    </#list>
</@page.layout>
