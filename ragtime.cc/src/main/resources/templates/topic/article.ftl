<#--
  
-->
<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<@page.layout title="${article.title}" hideTopicBio=true>
    <@c.editable msg="article.${article.id}">
        ${content}
    </@c.editable>
    
    <#if params.edit == "true">
        <script type="text/javascript">
            window.top.postMessage( 'article.${article.id}:loaded', '*' );
        </script>
    </#if>

</@page.layout>