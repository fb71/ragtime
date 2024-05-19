<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#--
  Page/Body 
 -->
<@page.layout title="Tiles">
    <div class="ITiles row row-cols-3">
        <#list articles?sequence?sort_by("modified")?reverse as article>
            <div class="ITile IClickable col" onclick="location.href='${topic.urlPart}?a=${article.id}';">
            <@c.editable msg="article.${article.id}">
                <#if article.medias?sequence?size == 0>
                    ${article.content}
                <#else>
                    <#list article.medias?sequence as media>
                        <img src="media/${media.name}?w=300&h=300" class="img-fluid" alt="${media.name}"/>
                    </#list>
                </#if>
            </@c.editable>
            </div>
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
