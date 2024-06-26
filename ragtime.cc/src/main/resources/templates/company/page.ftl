<#--
  Company
-->
<#import "/commons.ftl" as c>

<#--
  The main page layout. 
-->
<#macro layout title pageClass="IHome" hideTopicBio=false>
<html>
<head>
    <title>${title} - ${config.page.title}</title>
    <meta charset="iso-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <link href="common/bs5.3.3/bootstrap.min.css" rel="stylesheet"/>
    <#-- Template styles -->
    <link href="css/common.css" rel="stylesheet"/>
    <link href="topic/css/topic.css" rel="stylesheet"/>
    <link href="company/css/company.css" rel="stylesheet"/>
    <#-- TemplateConfigEntity styles-->
    <link href="config.css" rel="stylesheet"/>

    <script src="common/bs5.3.3/bootstrap.bundle.min.js" type="text/javascript"></script>
    <script src="common/js/common.js" type="text/javascript"></script>
</head>

<body class="${pageClass}">
    <#-- Page header -->
    <header class="CHeader border-bottom">
        <div class="container">
            <div class="row row-cols-2">
                <#-- Title-->
                <div class="col">
                    <@c.editable msg="page.title">
                        <h1>${config.page.title}</h1>
                    </@c.editable>
                </div>
        
                <#-- navbar-->
                <div class="col-6">
                    <nav class="CNavbar navbar navbar-expand-md">
                        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                        <div class="collapse navbar-collapse" id="navbarSupportedContent">
                            <ul class="navbar-nav mb-lg-0">
                                <#list topics?sequence?sort_by("order") as topic>
                                    <li class="nav-item"><a class="nav-link" href="${topic.urlPart}">${topic.title}</a></li>
                                </#list>
                                <#list config.navItems?sequence?sort_by("order") as item>
                                    <#if item.title == "Login">
                                        <li class="nav-item"><a class="btn" href="${item.href}">${item.title}</a></li>
                                    <#else>
                                        <li class="nav-item"><a class="nav-link" href="${item.href}">${item.title}</a></li>
                                    </#if>
                                </#list>
                            </ul>
                        </div>
                    </nav>
                </div>

                <#-- div class="col-auto">
                    <button type="button" class="btn btn-secondary">Secondary</button>
                </div -->
            </div>
        </div>
    </header>

    <#if !hideTopicBio>
        <@c.editable msg="topic.${topic.id}">
          <div class="ITopicBio CSection container">
            ${topic.description}
          </div>
        </@c.editable>
    </#if>
    
    <#-- Content -->
    <div class="IContent CSection container">
        <#nested>
    </div>

    <#-- Footer -->
    <footer class="CFooter CSection">
        <div class="container">
            <p class="m-0 small">
            <@c.editable msg="page.footer">
                <nobr>${config.page.footer}</nobr>&nbsp;&nbsp;-&nbsp;
            </@c.editable>
            <@c.editable msg="page.navigation">
                <#list config.footerNavItems?sequence?sort_by("order") as item>
                    <nobr><a href="${item.href}">${item.title}</a></nobr>&nbsp;&nbsp;-&nbsp;
                </#list>
            </@c.editable>
            <nobr>Made by <a target="_blank" href="https://fb71.org/">Wizard & Crew</a></nobr>
            </p>
        </div>
    </footer>
</body>
</html>
</#macro>
