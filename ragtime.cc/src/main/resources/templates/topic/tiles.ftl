<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#--
  Page/Body 
 -->
<@page.layout title="${topic.title}">
    <div class="ITiles row row-cols-3">
        <#list articles?sequence as article>
            <@c.editable msg="article.${article.entity.id}">
            <div class="ITile IClickable col" onclick="location.href='${article.entity.permName}';">
                <#if article.entity.medias?sequence?size == 0>
                    ${article.content}
                <#else>
                    <#assign media = article.entity.medias?first>
                    <img src="media/${media.id}?w=310&h=310" class="img-fluid" alt="${media.name}"/>
                </#if>
            </div>
            </@c.editable>
        </#list>
    </div>
    
    <#if params.edit == "true">
        <script type="text/javascript">
            window.top.postMessage( 'topic.${topic.id}:loaded', '*' );
        </script>
    </#if>

</@page.layout>
