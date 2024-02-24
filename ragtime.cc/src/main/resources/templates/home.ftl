<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#-- @c.data name="articles" model="QueryTemplateModel" params="type=ragtime.cc.model.Article"/-->

<@c.data name="article" model="ArticleTemplateModel" params="t=Home"/>
<@c.data name="aside" model="ArticleTemplateModel" params="t=aside"/>

<@page.layout title="${article.title}">
   
    <#assign style = "style=\"color:#695ea1\"">
    <aside class="d-none d-md-block float-md-end border-start ms-5 ps-5" ${style}>
        ${aside.content}
    </aside>

   ${article.content}

    <div class="d-block d-md-none border-top mt-4" ${style}>
        ${aside.content}
    </div>
   
</@page.layout>
