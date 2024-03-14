User-agent: *
Allow: /
<#assign host="${params.Host}">
<#if params.X\-Forwarded\-Host != "">
    <#assign host="${params.X\\-Forwarded\\-Host}">
</#if>
Sitemap: https://${host}/sitemap.txt