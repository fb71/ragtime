<#--
  
-->
<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="EntityByIdTemplateModel" params="type=ragtime.cc.model.Article"/>

<@page.layout title="${article.title}" pageclass="CArticle">
   <@c.editable msg="article.${article.id}">
       ${article.content}
   </@c.editable>
</@page.layout>