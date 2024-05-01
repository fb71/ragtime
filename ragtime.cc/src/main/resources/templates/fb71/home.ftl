<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@c.data name="article" model="ArticleTemplateModel" params="t=Home"/>
<@c.data name="ulli" model="ArticleTemplateModel" params="n=Home-Ulli"/>
<@c.data name="friederike" model="ArticleTemplateModel" params="n=Home-Ulli"/>
<@c.data name="about" model="ArticleTemplateModel" params="n=About"/>

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
   <div class="CSection row row-cols-1 row-cols-md-2">
     <div class="col">
       <@card title="ulrike-philipp.de" content="${ulli.content}" media="media/ulrike-philipp-1.png" link="https://ulrike-philipp.de"/>
     </div>
     <div class="col">
       <@card title="" content="${friederike.content}" media="" link="#"/>
     </div>
   </div>
   
   <#-- Articles -->   
   <div class="CSection row row-cols-1 row-cols-md-3">
     <div class="col">
       ${about.content}
     </div>
     <div class="col">
       ${about.content}
     </div>
     <div class="col">
       ${about.content}
     </div>
   </div>
</@page.layout>


<#--
  Card. 
-->
<#macro card title content media link>
  <div class="card shadow h-100">
    <div class="row g-0">
      <div class="col-md-8">
        <#-- rounded-start -->
        <img src="${media}" class="img-fluid" alt="Screenshot">
      </div>
      <div class="col-md-4">
        <div class="card-body">
          <h5 class="card-title"><a href="${link}">${title}</a></h5>
          ${content}
        </div>
      </div>
    </div>
  </div>
</#macro>
