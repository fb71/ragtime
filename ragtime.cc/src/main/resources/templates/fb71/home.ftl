<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="ArticleTemplateModel" params="t=Home"/>
<@c.data name="ulli" model="ArticleTemplateModel" params="n=Home-Ulli"/>
<@c.data name="gienke" model="ArticleTemplateModel" params="n=Home-Gienke"/>
<@c.data name="home1" model="ArticleTemplateModel" params="n=Home1"/>
<@c.data name="home2" model="ArticleTemplateModel" params="n=Home2"/>
<@c.data name="home3" model="ArticleTemplateModel" params="n=Home3"/>

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

   <#-- Screenshots -->   
   <div class="CSection row g-4 row-cols-1 row-cols-md-2">
     <div class="col">
       <@card title="ulrike-philipp.de" content="${ulli.content}" media="media/ulrike-philipp-1.png" link="https://ulrike-philipp.de"/>
     </div>
     <div class="col">
       <@card title="psychotherapie-gienke.de" content="${gienke.content}" media="media/gienke-1.png" link="https://psychotherapie-gienke.de/home"/>
     </div>
   </div>
   
   <#-- Articles -->   
   <div class="CSection row row-cols-1 row-cols-md-3">
     <div class="col">
       ${home1.content}
     </div>
     <div class="col">
       ${home2.content}
     </div>
     <div class="col">
       ${home3.content}
     </div>
   </div>
</@page.layout>


<#--
  Card
-->
<#macro card title content media link>
  <div class="card shadow h-100">
    <div class="row g-0">
      <div class="col-md-8">
        <#-- rounded-start -->
        <a href="${link}"><img src="${media}" class="img-fluid" alt="Screenshot"/></a>
      </div>
      <div class="col-md-4">
        <div class="card-body">
          <a href="${link}"><h5 class="card-title">${title}</h5></a>
          ${content}
        </div>
      </div>
    </div>
  </div>
</#macro>
