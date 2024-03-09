<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#-- @c.data name="articles" model="QueryTemplateModel" params="type=ragtime.cc.model.Article"/-->

<@c.data name="article" model="ArticleTemplateModel" params="t=Home"/>
<@c.data name="aside" model="ArticleTemplateModel" params="t=aside"/>

<@page.layout title="${article.title}" pageclass="CHome">
   
    <aside class="CStart d-none d-md-block float-md-end border-start py-5 ms-5 ps-5 pe-3">
        ${aside.content}
    </aside>

   ${article.content}
   
   <div class="CEnd d-block d-md-none border-top pt-4 mt-4" style="color:var(--common-accent-color)">
        ${aside.content}
   </div>

</@page.layout>
