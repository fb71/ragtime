<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="ArticleTemplateModel" params="t=Home"/>
<@c.data name="aside" model="ArticleTemplateModel" params="t=aside"/>

<@page.layout title="${article.title}" pageclass="CHome">
   
    <aside class="CAside CStart d-none d-md-block float-md-end border-start">
        <@c.editable msg="article.${aside.id}">
          ${aside.content}
        </@c.editable>
    </aside>

   <@c.editable msg="article.${article.id}">
     ${article.content}
   </@c.editable>
   
   <div class="CAside CEnd d-block d-md-none border-top">
        <@c.editable msg="article.${aside.id}">
          ${aside.content}
        </@c.editable>
   </div>

</@page.layout>
