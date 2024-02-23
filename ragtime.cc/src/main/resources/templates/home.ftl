<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="articles" model="QueryTemplateModel" params="type=ragtime.cc.model.Article"/>

<@c.data name="article" model="ArticleByTagTemplateModel" params="t=Home"/>

<@page.layout title="${article.title}">
   ${article.content}
</@page.layout>
