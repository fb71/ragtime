<#--
  
-->
<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="CompositeTemplateModel" params="type=ragtime.cc.model.Article"/>

<@page.layout title="${article.title}">
   ${article.content}
</@page.layout>