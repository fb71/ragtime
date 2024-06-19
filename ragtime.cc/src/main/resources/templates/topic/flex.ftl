<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#--
  Flex 
-->
<@page.layout title="${topic.title}">
     <#-- rows -->
     <#list rows as row>
         <div class="row row-cols-1 row-cols-md-${row?sequence?size}">
             <#-- articles -->
             <#list row?sequence as article>
                 <div class="col">
                     <@c.editable msg="article.${article.entity.id}">
                         ${article.content}
                     </@c.editable>
                 </div>
             </#list>
         </div>
     </#list>
</@page.layout>
