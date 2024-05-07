<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="ArticleTemplateModel" params="n=Rike"/>
<#--    
<@c.data name="home1" model="ArticleTemplateModel" params="n=Home1"/>
<@c.data name="home2" model="ArticleTemplateModel" params="n=Home2"/>
<@c.data name="home3" model="ArticleTemplateModel" params="n=Home3"/>
-->

<#--
  Page/Body 
-->
<@page.layout title="${article.title}" pageclass="CHome">
   <#-- Lead/content -->
   <@c.editable msg="article.${article.id}">
     <div class="CSection lead">
       ${article.content}
     </div>
   </@c.editable>
   
   <#-- Articles  -->
   <#--    
   <div class="CSection row row-cols-1 row-cols-md-3">
     <div class="col">
       <@c.editable msg="article.${home1.id}">
         ${home1.content}
       </@c.editable>
     </div>
     <div class="col">
       <@c.editable msg="article.${home2.id}">
         ${home2.content}
       </@c.editable>
     </div>
     <div class="col">
       <@c.editable msg="article.${home3.id}">
         ${home3.content}
       </@c.editable>
     </div>
   </div>
    -->
</@page.layout>


<#--
  Card
-->
<#macro card title id content media link>
  <div class="card shadow h-100">
    <div class="row g-0">
      <div class="col-md-8">
        <#-- rounded-start -->
        <a href="${link}"><img src="${media}" class="img-fluid" alt="Screenshot"/></a>
      </div>
      <div class="col-md-4">
        <@c.editable msg="article.${id}">
        <div class="card-body">
          <a href="${link}"><h5 class="card-title">${title}</h5></a>
          ${content}
        </div>
        </@c.editable>
      </div>
    </div>
  </div>
</#macro>
