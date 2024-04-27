<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="ArticleTemplateModel" params=""/>
<#-- @c.data name="aside" model="ArticleTemplateModel" params="t=aside"/ -->

<@page.layout title="${article.title}" pageclass="CFrontpage">
   
   <@c.editable msg="article.${article.id}">
       ${article.content}
   </@c.editable>

</@page.layout>
