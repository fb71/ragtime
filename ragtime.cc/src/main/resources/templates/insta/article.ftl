<#--
  
-->
<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@page.layout title="${article.title}" hideTopicBio=true>
    <@c.editable msg="article.${article.id}">
        ${content}
    </@c.editable>
</@page.layout>