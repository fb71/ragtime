<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="ArticleByTagTemplateModel" params=""/>

<@page.layout title="${article.title}">
   ${article.content}
</@page.layout>
