<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#--
  Page/Body 
-->
<@page.layout title="Basic">
    <#list articles?sequence?sort_by("modified")?reverse as article>
        <@c.editable msg="article.${article.id}">
            ${article.content}
        </@c.editable>
        <hr/>
    </#list>
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
