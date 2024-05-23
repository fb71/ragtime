<#--
  
-->
<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@page.layout title="${article.title}" hideTopicBio=true>
    <#if article.medias?sequence?size gt 0>
        <div id="carouselExampleIndicators" class="carousel slide mx-auto" data-bs-ride="carousel">
            <div class="carousel-indicators">
                <#-- button type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide-to="0" class="active" aria-current="true" aria-label="Slide 1"></button-->
                <#list article.medias?sequence as media>
                    <button type="button"
                            <#if media?index == 0>class="active" aria-current="true"</#if> 
                            data-bs-target="#carouselExampleIndicators" 
                            data-bs-slide-to="${media?index}" 
                            aria-label="Slide ${media?index}">
                    </button>
                </#list>
            </div>
            <div class="carousel-inner">
                <#list article.medias?sequence as media>
                    <div class="carousel-item <#if media?index == 0>active</#if>" data-bs-interval="30000">
                        <img src="media/${media.id}?w=380&h=300" class="d-block w-100" alt="${media.name}">
                    </div>
                </#list>
            </div>
            <button class="carousel-control-prev" type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Previous</span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Next</span>
            </button>
        </div>
    </#if>

    <@c.editable msg="article.${article.id}">
        ${article.content}
    </@c.editable>
</@page.layout>