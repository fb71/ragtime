<#--
  The main page layout. 
-->
<#macro layout title pageclass>
<html>
<head>
    <title>${title} - ${config.page.title}</title>
    <meta charset="iso-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="bs5.3.3/bootstrap.min.css" rel="stylesheet"/>
    <#-- Styles specific to the common template -->
    <link href="css/common.css" rel="stylesheet"/>
    <#-- Styles from TemplateConfigEntity -->
    <link href="config.css" rel="stylesheet"/>
    <script src="bs5.3.3/bootstrap.bundle.min.js" type="text/javascript"></script>
</head>

<body class="${pageclass}">
    <#-- Page header with logo and tagline-->
    <header class="CHeader border-bottom">
        <div class="container">
            <!--div class="text-center my-5"-->
                <h1>${config.page.title}</h1>
                <p class="lead mb-0">${config.page.title2}</p>
            <!--/div-->
        </div>
    </header>
    
    <#-- Responsive navbar-->
    <nav class="CNavbar navbar navbar-expand-md">
        <div class="container">
            <a class="navbar-brand" href="#!"></a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                <ul class="navbar-nav mb-lg-0">
                    <#list config.navItems?sequence?sort_by("order") as item>
                        <li class="nav-item"><a class="nav-link" href="${item.href}">${item.title}</a></li>
                    </#list>

                    <#-- li class="nav-item"><a class="nav-link" href="frontpage">Home</a></li>
                    <li class="nav-item"><a class="nav-link" href="kontakt">Kontakt</a></li>
                    <li class="nav-item"><a class="nav-link" href="datenschutz">Datenschutz</a></li>
                    <li class="nav-item"><a class="nav-link active" aria-current="page" href="impressum">Impressum</a></li-->
                </ul>
            </div>
        </div>
    </nav>
    
    <div class="CContent container">
    <#nested>
    </div>

    <#-- Footer-->
    <footer class="CFooter">
        <div class="container">
            <p class="m-0 small">
            <nobr>${config.page.footer}</nobr>&nbsp;&nbsp;-&nbsp;
            <#list config.footerNavItems?sequence?sort_by("order") as item>
                <nobr><a href="${item.href}">${item.title}</a></nobr>&nbsp;&nbsp;-&nbsp;
            </#list>
            <nobr>Made by <a target="_blank" href="https://fb71.org/">Wizard & Crew</a></nobr>
            </p>
        </div>
    </footer>    
</body>
</html>
</#macro>
