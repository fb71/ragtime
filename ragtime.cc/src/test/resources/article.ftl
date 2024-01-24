<#--
  
-->
<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="CompositeTemplateModel" params="type=ragtime.cc.model.Article"/>

<@page.layout title="Artikel">
   <h1>Artikel - ${article.title}</h1>

   params.id = ${params.id}
   <p>${article.content}</p>
   
</@page.layout>