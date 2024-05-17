<#--
  
-->
<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@page.layout title="${article.title}">
   <@c.editable msg="article.${article.id}">
       ${article.content}
   </@c.editable>
</@page.layout>