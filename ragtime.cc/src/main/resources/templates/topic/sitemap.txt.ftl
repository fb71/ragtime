<#assign host = "${params.X\\-Forwarded\\-Host}">
<#if host == "">
    <#assign host = "${params.Host}">
</#if>
<#list topics?sequence?sort_by("order") as topic>
https://${host}/${topic.permName}
</#list>
