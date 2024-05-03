<#--
  fb71 
-->
<#import "/commons.ftl" as c>

<#--
  The main page layout. 
-->
<#macro layout title pageclass>
<html>
<head>
    <title>${title} - ${config.page.title}</title>
    <meta charset="iso-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="common/bs5.3.3/bootstrap.min.css" rel="stylesheet"/>
    <#-- Template styles -->
    <link href="css/common.css" rel="stylesheet"/>
    <link href="css/first.css" rel="stylesheet"/>
    <link href="css/fb71.css" rel="stylesheet"/>
    <#-- TemplateConfigEntity styles-->
    <link href="config.css" rel="stylesheet"/>
    <script src="common/bs5.3.3/bootstrap.bundle.min.js" type="text/javascript"></script>
</head>

<body class="${pageclass}">
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
                        <div class="container">
                            <a class="navbar-brand" href="#!"></a>
                            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                                <span class="navbar-toggler-icon"></span>
                            </button>
                            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                                <ul class="navbar-nav mb-lg-0">
                                    <#list config.navItems?sequence?sort_by("order") as item>
                                        <#if item.title == "Login">
                                            <li class="nav-item"><a class="btn" href="${item.href}">${item.title}</a></li>
                                        <#else>
                                            <li class="nav-item"><a class="nav-link" href="${item.href}">${item.title}</a></li>
                                        </#if>
                                    </#list>
                                </ul>
                            </div>
                        </div>
                    </nav>
                </div>

                <#-- div class="col-auto">
                    <button type="button" class="btn btn-secondary">Secondary</button>
                </div -->
            </div>
        </div>

        <#-- Actions... -->
    </header>
    
    <#-- Content -->
    <div class="CContent container">
    <#nested>
    </div>

    <#-- Footer -->
    <footer class="CFooter">
        <div class="container">
            <p class="m-0 small">
            <@c.editable msg="page.footer">
                <nobr>${config.page.footer}</nobr>&nbsp;&nbsp;-&nbsp;
            </@c.editable>
            <#list config.footerNavItems?sequence?sort_by("order") as item>
                <@c.editable msg="page.navigation">
                    <nobr><a href="${item.href}">${item.title}</a></nobr>&nbsp;&nbsp;-&nbsp;
                </@c.editable>
            </#list>
            <nobr>Made by <a target="_blank" href="https://fb71.org/">Wizard & Crew</a></nobr>
            </p>
        </div>
    </footer>    
</body>
</html>
</#macro>
