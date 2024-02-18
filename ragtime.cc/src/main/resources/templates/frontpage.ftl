<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="articles" model="QueryTemplateModel" params="type=ragtime.cc.model.Article"/>

<@page.layout title="Frontpage">
   <h2>Frontpage</h2>
   <ul>
    <#list articles as article>
      <li>
         <#-- [${article_index + 1}] --> 
         <a href="article?id=${article.id}">${article.title}</a>
      <br/>
      <#-- ${article.content} -->
      </li>
    </#list>
  </ul>
</@page.layout>