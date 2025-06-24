<#import "/page.ftl" as page>
<#import "/commons.ftl" as c>

<#setting datetime_format="dd.MM.yyyy '|' HH:mm">
<#setting locale="de_DE">
<#setting url_escaping_charset='ISO-8859-1'>

<#--
  Calendar
-->
<@page.layout title="${topic.title}">
     <#-- periods -->
     
     <#-- articles -->
     <#list period?sequence as article>
         <div class="col ICalendarEvent">
             <@c.editable msg="article.${article.entity.id}">
                 <p class="ICalendarEventTime">${article.start}</p>
                 ${article.content}
                 <a class="btn btn-secondary" href="mailto:${article.email}?subject=Termin%20${article.start?url}">Termin buchen</a>
             </@c.editable>
         </div>
         
         <#if article?counter < period?sequence?size>
            <hr/>
         </#if>
     </#list>
     
     <#-- Send iframe event -->
     <#if params.edit == "true">
        <script type="text/javascript">
            window.top.postMessage( 'topic.${topic.id}:loaded', '*' );
        </script>
    </#if>
</@page.layout>
