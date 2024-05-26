<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#--
  Page/Body 
 -->
<@page.layout title="Tiles">
    <div class="ITiles row row-cols-3">
        <#list articles?sequence?sort_by("modified")?reverse as article>
            <@c.editable msg="article.${article.id}">
            <div class="ITile IClickable col" onclick="location.href='${topic.urlPart}?a=${article.id}';">
                <#if article.medias?sequence?size == 0>
                    ${article.content}
                <#else>
                    <#assign media = article.medias?first>
                    <img src="media/${media.id}?w=310&h=310" class="img-fluid" alt="${media.name}"/>
                </#if>
            </div>
            </@c.editable>
        </#list>
    </div>
</@page.layout>

<#--
  Tile
 -->
<#macro tile title id content media link>
</#macro>

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
