<#assign host = "${params.X\\-Forwarded\\-Host}">
<#if host == "">
    <#assign host = "${params.Host}">
</#if>
<#list config.navItems as item>
https://${host}/${item.href}
</#list>
